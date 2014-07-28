package com.example.wizard1;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.*;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.example.wizard1.components.Vector4d;
import com.example.wizard1.views.SpellAnimation;
import com.example.wizard1.views.WizardDial;
import com.example.wizard1.views.WizardDialDelegate;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;

public class Tutorial extends Activity implements WizardDialDelegate {
    class SpellData {
        String name = "";
        ArrayList<Double> pointsX = new ArrayList<Double>();
        ArrayList<Double> pointsY = new ArrayList<Double>();
        boolean round=false;
        boolean rotate=false;

        SpellData(String name, ArrayList<Double> pointsX, ArrayList<Double> pointsY,boolean round,boolean rotate) {
            this.name = name;
            this.pointsX = pointsX;
            this.pointsY = pointsY;
            this.round=round;
            this.rotate=rotate;
        }
    }

    private ArrayList<SpellData> spellDatas = new ArrayList<SpellData>();
    private WizardDial wd;
    private SpellAnimation sa;
    private TextView spellName;
    private TextView spellCounter;

    private int pause=-1;
    private int partCounter = 0;
    private int spellCount = -1;
    private int spellRepeat = 5;

    private boolean isVolumeButtonBlocked=false;
    private boolean isBetweenVolumeClicks=false;
    private AcceleratorThread mAcceleratorThread = null;
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            FightMessage fMsg = (FightMessage) msg.obj;
            String shape = FightMessage.getShapeFromMessage(fMsg) + "";
            //if(shape.equals("triangle"))
                addSpellCounter();
            Log.e("Wizard Fight",shape);
            isVolumeButtonBlocked = false;
        }
    };
    private Sensor mAccelerometer = null;
    private SensorManager mSensorManager = null;
    private double gravity=9.81;

    private ArrayList<ArrayList<String>> partsText = new ArrayList<ArrayList<String>>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        readSpellXml();
        readTutotialXml();
        try {
            Recognition.init(getResources());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            Log.e("recognition", "", e);
        }
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        setContentView(R.layout.tutorial);

        wd = new WizardDial(this);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        );
        wd.setText(partsText.get(0));
        wd.setPause(pause);
        wd.setLayoutParams(params);

        spellName = (TextView) findViewById(R.id.spell_name);
        spellName.setText("");

        sa=(SpellAnimation)findViewById(R.id.spell_anim_view);

        spellCounter = (TextView) findViewById(R.id.number_correct_spell);
        spellCounter.setText("");
        addSpellCounter();

        ((RelativeLayout) findViewById(R.id.tutorial_layout)).addView(wd);
        wd.showQuick();
    }

    @Override
	protected void onResume() {
		super.onResume();
		mAcceleratorThread = new AcceleratorThread(this, mSensorManager,
				mAccelerometer, gravity);
		mAcceleratorThread.start();
	}

	@Override
	protected void onPause() {
		super.onPause();
		// stop cast if its started
		if (isBetweenVolumeClicks)
			buttonClick();
		// unregister accelerator listener and end its event loop
		if (mAcceleratorThread != null) {
			Log.e("Wizard Fight", "accelerator thread try to stop loop");
			mAcceleratorThread.stopLoop();
			mAcceleratorThread = null;
		}
	}

    public void open(View view) {
        wd.show();
    }

    public void replay(View view) {
        sa.startAnimation();
    }

    private void readTutotialXml(){
        int t = 0;
        pause=-1;
        ArrayList<String> text = new ArrayList<String>();
        partsText=new ArrayList<ArrayList<String>>();
        try{
            XmlPullParser xpp = getResources().getXml(R.xml.tutorial_text);
            while (xpp.getEventType() != XmlPullParser.END_DOCUMENT) {
                switch (xpp.getEventType()) {
                    case XmlPullParser.START_TAG:
                        if (xpp.getName().equals("screen"))
                            t = 1;
                        else if (xpp.getName().equals("pause"))
                            t = 2;
                        break;
                    case XmlPullParser.END_TAG:
                        if (xpp.getName().equals("part")) {
                            partsText.add(text);
                            text=new ArrayList<String>();
                        }
                        break;
                    case XmlPullParser.TEXT:
                        switch (t) {
                            case 1:
                                text.add(xpp.getText());
                                break;
                            case 2:
                                pause=Integer.parseInt(xpp.getText());
                                break;
                        }
                        t = 0;
                        break;
                    default:
                        break;
                }
                xpp.next();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException ev) {
            ev.printStackTrace();
        }
    }

    private void readSpellXml() {
        int t = 0;
        String name = "";
        ArrayList<Double> pointX = new ArrayList<Double>();
        ArrayList<Double> pointY = new ArrayList<Double>();
        boolean rotate=false;
        boolean round=false;
        try {
            XmlPullParser xpp = getResources().getXml(R.xml.spells);
            while (xpp.getEventType() != XmlPullParser.END_DOCUMENT) {
                switch (xpp.getEventType()) {
                    case XmlPullParser.START_TAG:
                        if (xpp.getName().equals("name"))
                            t = 1;
                        else if (xpp.getName().equals("pointX"))
                            t = 2;
                        else if (xpp.getName().equals("pointY"))
                            t = 3;
                        else if (xpp.getName().equals("rotate"))
                            t = 4;
                        else if (xpp.getName().equals("round"))
                            t = 5;
                        break;
                    case XmlPullParser.END_TAG:
                        if (xpp.getName().equals("spell")) {
                            spellDatas.add(new SpellData(name, pointX, pointY,round,rotate));
                            pointX=new ArrayList<Double>();
                            pointY = new ArrayList<Double>();
                            rotate=false;
                            round=false;
                        }
                        break;
                    case XmlPullParser.TEXT:
                        switch (t) {
                            case 1:
                                name = xpp.getText();
                                break;
                            case 2:
                                pointX.add(Double.parseDouble(xpp.getText()));
                                break;
                            case 3:
                                pointY.add(Double.parseDouble(xpp.getText()));
                                break;
                            case 4:
                                rotate=Boolean.parseBoolean(xpp.getText());
                                break;
                            case 5:
                                round=Boolean.parseBoolean(xpp.getText());
                                break;
                        }
                        t = 0;
                        break;
                    default:
                        break;
                }
                xpp.next();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException ev) {
            ev.printStackTrace();
        }
    }

    @Override
    public void onWizardDialClose() {
        setupSpellScreen();
    }

    public void buttonClick() {
		if (isVolumeButtonBlocked)
			return;

		if (!isBetweenVolumeClicks) {
			mAcceleratorThread.startGettingData();
			isBetweenVolumeClicks = true;

		} else {
			isVolumeButtonBlocked = true;

			ArrayList<Vector4d> records = mAcceleratorThread.stopAndGetResult();
			isBetweenVolumeClicks = false;

			if (records.size() > 10) {
				new RecognitionThread(mHandler, records).start();
			} else {
				// if shord record - don`t recognize & unblock
				isVolumeButtonBlocked = false;
			}
		}
	}

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();
        switch( keyCode ) {
            case KeyEvent.KEYCODE_VOLUME_UP:
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (action == KeyEvent.ACTION_DOWN) {
                    if (wd.isEnabled())
                    {
                        if(wd.isOnPause())
                            wd.goNext();
                    }
                    else{
                        buttonClick();
                    }
                }
                return true;
            default:
                return super.dispatchKeyEvent(event);
        }
    }

    public void setupSpellScreen() {
        if (partCounter >= spellDatas.size()) {
            finish();
            return;
        }
        SpellData sd=spellDatas.get(partCounter);
        spellName.setText(sd.name);
        ArrayList<Double[]>t=new ArrayList<Double[]>();
        for(int i=0;(i<sd.pointsX.size())&&(i<sd.pointsY.size());i++) {
            t.add(new Double[]{sd.pointsX.get(i), sd.pointsY.get(i)});
        }
        sa.setTrajectory(t,sd.rotate,sd.round);
        sa.startAnimation();
    }

    public void addSpellCounter() {
        spellCount++;
        if(spellCount==spellRepeat) {
            spellCount = 0;
            partCounter++;
            wd.setText(partsText.get(partCounter));
            if (partCounter==0)
                wd.setPause(pause);
            wd.show();
        }
        spellCounter.setText(spellCount + "/" + spellRepeat);
    }
    public void addcount(View view){
        addSpellCounter();
    }
}
