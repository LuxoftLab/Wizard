package com.wizardfight;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.*;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.wizardfight.FightActivity.AppMessage;
import com.wizardfight.components.Vector3d;
import com.wizardfight.recognition.Recognizer;
import com.wizardfight.views.*;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;

public class Tutorial extends Activity implements WizardDialDelegate {
	class SpellData {
		String name = "";
		ArrayList<Double> pointsX = new ArrayList<Double>();
		ArrayList<Double> pointsY = new ArrayList<Double>();
		boolean round = false;
		boolean rotate = false;
		String shape = "";

		SpellData(String name, ArrayList<Double> pointsX,
				ArrayList<Double> pointsY, boolean round, boolean rotate,
				String shape) {
			this.name = name;
			this.pointsX = pointsX;
			this.pointsY = pointsY;
			this.round = round;
			this.rotate = rotate;
			this.shape = shape;
		}
	}

	private final ArrayList<SpellData> spellDatas = new ArrayList<SpellData>();

	private WizardDial wd;
	private SpellAnimation sa;
	private SpellPicture castResult;
	private TextView spellName;
	private TextView spellCounter;


	private int partCounter = 0;
	private int spellCount = -1;
	private int lastTouchAction = -1;
	private final int spellRepeat = 2;

	private boolean isCastAbilityBlocked = false;
	private boolean isInCast = false;
	private SensorAndSoundThread mSensorAndSoundThread = null;
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			AppMessage type = AppMessage.values()[msg.what];
			switch (type) {
			case MESSAGE_FROM_SELF:
				FightMessage fMsg = (FightMessage) msg.obj;
				Shape s = FightMessage.getShapeFromMessage(fMsg);
				mSensorAndSoundThread.playShapeSound(s);
				String shape = s + "";
				// todo uncomment
				if (shape.equals(spellDatas.get(partCounter).shape)) {
					addSpellCounter();
					castResult.setPictureAndFade(R.drawable.result_ok);
				} else {
					castResult.setPictureAndFade(R.drawable.result_bad);
				}
				Log.e("Wizard Fight", shape);
				isCastAbilityBlocked = false;
				break;
			default:
			}
		}
	};
	private Sensor mAccelerometer = null;
	private SensorManager mSensorManager = null;

	private final ArrayList<ArrayList<WizardDialContent>> parts = new ArrayList<ArrayList<WizardDialContent>>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tutorial);
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		
		readSpellXml();
		readTutotialXml();
		
		// Init recognition resources
		SharedPreferences appPrefs = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());
		String rType = appPrefs.getString("recognition_type", "");
		Log.e("Wizard Fight", rType);
		Recognizer.init(getResources(), rType);

		// Init on touch listener
		RelativeLayout rootLayout = (RelativeLayout) findViewById(R.id.tutorial_layout_root);
		rootLayout.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				int action = event.getAction();
				if (action == MotionEvent.ACTION_UP
						|| action == MotionEvent.ACTION_DOWN) {
					if (wd.isEnabled()) {
						if (wd.isOnPause())
							wd.goNext();
					} else {
						if (lastTouchAction == action) {
							return true;
						}
						buttonClick();
						lastTouchAction = action;
					}
					return true;
				}
				return false;
			}
		});

		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mAccelerometer = mSensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

		wd = new WizardDial(this);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT);
		wd.setContent(parts.get(0));
		wd.setLayoutParams(params);

		castResult = (SpellPicture) findViewById(R.id.tutorial_cast_result);

		spellName = (TextView) findViewById(R.id.spell_name);
		spellName.setText("");

		sa = (SpellAnimation) findViewById(R.id.spell_anim_view);

		spellCounter = (TextView) findViewById(R.id.number_correct_spell);
		spellCounter.setText("");
		addSpellCounter();

		((RelativeLayout) findViewById(R.id.tutorial_layout)).addView(wd);
		wd.showQuick();
	}

	@Override
	protected void onResume() {
		super.onResume();

		mSensorAndSoundThread = new SensorAndSoundThread(this, mSensorManager,
				mAccelerometer);
		mSensorAndSoundThread.start();
	}

	@Override
	protected void onPause() {
		super.onPause();
		// stop cast if its started
		if (isInCast)
			buttonClick();
		// unregister accelerator listener and end its event loop
		if (mSensorAndSoundThread != null) {
			Log.e("Wizard Fight", "accelerator thread try to stop loop");
			mSensorAndSoundThread.stopLoop();
			mSensorAndSoundThread = null;
		}
	}

	public void open(View view) {
		wd.show();
	}

	public void replay(View view) {
		sa.startAnimation();
	}

	private void readTutotialXml() {
		int t = 0;
		int pause = -1;
		ArrayList<String> text = new ArrayList<String>();
		int ui = -1;
		int uih = -1;
		int uim = -1;
		try {
			XmlPullParser xpp = getResources().getXml(R.xml.tutorial_text);
			while (xpp.getEventType() != XmlPullParser.END_DOCUMENT) {
				switch (xpp.getEventType()) {
				case XmlPullParser.START_TAG:
					if (xpp.getName().equals("screen"))
						t = 1;
					else if (xpp.getName().equals("pause"))
						t = 2;
					else if (xpp.getName().equals("ui"))
						t = 3;
					else if (xpp.getName().equals("uih"))
						t = 4;
					else if (xpp.getName().equals("uim"))
						t = 5;
					break;
				case XmlPullParser.END_TAG:
					if (xpp.getName().equals("part")) {
						ArrayList<WizardDialContent> p = new ArrayList<WizardDialContent>();
						for (int i = 0; i < text.size(); i++) {
							WizardDialContent a = new WizardDialContent();
							a.setText(text.get(i));
							if (pause == i + 1)
								a.setPause(true);
							if (ui == i + 1)
								a.setUi(true);
							if (uih == i + 1)
								a.setHealth(true);
							if (uim == i + 1)
								a.setMana(true);
							p.add(a);
						}
						parts.add(p);
						ui = -1;
						uih = -1;
						uim = -1;
						pause = -1;
						text = new ArrayList<String>();
					}
					break;
				case XmlPullParser.TEXT:
					switch (t) {
					case 1:
						text.add(xpp.getText());
						break;
					case 2:
						pause = Integer.parseInt(xpp.getText());
						break;
					case 3:
						ui = Integer.parseInt(xpp.getText());
						break;
					case 4:
						uih = Integer.parseInt(xpp.getText());
						break;
					case 5:
						uim = Integer.parseInt(xpp.getText());
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
		String shape = "";
		ArrayList<Double> pointX = new ArrayList<Double>();
		ArrayList<Double> pointY = new ArrayList<Double>();
		boolean rotate = false;
		boolean round = false;
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
					else if (xpp.getName().equals("shape"))
						t = 6;
					break;
				case XmlPullParser.END_TAG:
					if (xpp.getName().equals("spell")) {
						spellDatas.add(new SpellData(name, pointX, pointY,
								round, rotate, shape));
						pointX = new ArrayList<Double>();
						pointY = new ArrayList<Double>();
						rotate = false;
						round = false;
						shape = "";
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
						rotate = Boolean.parseBoolean(xpp.getText());
						break;
					case 5:
						round = Boolean.parseBoolean(xpp.getText());
						break;
					case 6:
						shape = xpp.getText();
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

	void buttonClick() {
		if (isCastAbilityBlocked)
			return;

		if (!isInCast) {
			mSensorAndSoundThread.startGettingData();
			isInCast = true;

		} else {
			isCastAbilityBlocked = true;

			ArrayList<Vector3d> records = mSensorAndSoundThread.stopAndGetResult();
			isInCast = false;

			if (records.size() > 10) {
				new RecognitionThread(mHandler, records).start();
			} else {
				// if shord record - don`t recognize & unblock
				isCastAbilityBlocked = false;
			}
		}
	}

	void setupSpellScreen() {
		if (partCounter >= spellDatas.size()) {
			finish();
			return;
		}
		SpellData sd = spellDatas.get(partCounter);
		spellName.setText(sd.name);
		ArrayList<Double[]> t = new ArrayList<Double[]>();
		for (int i = 0; (i < sd.pointsX.size()) && (i < sd.pointsY.size()); i++) {
			t.add(new Double[] { sd.pointsX.get(i), sd.pointsY.get(i) });
		}
		sa.setTrajectory(t, sd.rotate, sd.round);
		sa.startAnimation();
	}

	void addSpellCounter() {
		spellCount++;
		if (spellCount == spellRepeat) {
			spellCount = 0;
			partCounter++;
			wd.setContent(parts.get(partCounter));
			wd.show();
		}
		spellCounter.setText(spellCount + "/" + spellRepeat);
	}
}
