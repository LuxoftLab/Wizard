package com.wizardfight;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.*;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.wizardfight.FightActivity.AppMessage;
import com.wizardfight.views.*;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;

public class Tutorial extends CastActivity implements WizardDialDelegate {
	class SpellData {
		String name = "";
		ArrayList<Double> pointsX = new ArrayList<Double>();//todo one list of points
		ArrayList<Double> pointsY = new ArrayList<Double>();
		boolean round = false;
		boolean rotate = false;
		Shape shape = Shape.NONE;

		SpellData(String name, ArrayList<Double> pointsX,
				ArrayList<Double> pointsY, boolean round, boolean rotate,
				String shape) {
			this.name = name;
			this.pointsX = pointsX;
			this.pointsY = pointsY;
			this.round = round;
			this.rotate = rotate;
			this.shape = Shape.getShapeFromString(shape);
		}
	}

	private final ArrayList<SpellData> mSpellDatas = new ArrayList<SpellData>();

	private WizardDial mWizardDial;
	private SpellAnimation mSpellAnim;
	private SpellPicture mCastResult;
	private TextView mSpellName;
	private TextView mSpellCounter;

	private int mPartCounter = 0;
	private int mSpellCount = -1;
	private final int mSpellRepeat = 2;

	private final ArrayList<ArrayList<WizardDialContent>> mChapters = new ArrayList<ArrayList<WizardDialContent>>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tutorial);
		

		readSpellXml();
		readTutotialXml();

		// Init on touch listener
		RelativeLayout rootLayout = (RelativeLayout) findViewById(R.id.tutorial_layout);
		rootLayout.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (mIsCastAbilityBlocked)
                    return true;
				int action = event.getAction();
				if (action == MotionEvent.ACTION_UP
						|| action == MotionEvent.ACTION_DOWN) {
					if (mWizardDial.isEnabled()) {
						if (mWizardDial.isOnPause())
							mWizardDial.goNext();
					} else {
						if (mLastTouchAction == action) {
							return true;
						}
						buttonClick();
						mLastTouchAction = action;
					}
					return true;
				}
				return false;
			}
		});

		mWizardDial = new WizardDial(this);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT);
		mWizardDial.setContent(mChapters.get(0));
		mWizardDial.setLayoutParams(params);

		mCastResult = (SpellPicture) findViewById(R.id.tutorial_cast_result);

		mSpellName = (TextView) findViewById(R.id.spell_name);
		mSpellName.setText("");

		mSpellAnim = (SpellAnimation) findViewById(R.id.spell_anim_view);

		mSpellCounter = (TextView) findViewById(R.id.number_correct_spell);
		mSpellCounter.setText("");
		addSpellCounter();

		((RelativeLayout) findViewById(R.id.tutorial_layout)).addView(mWizardDial);
		mWizardDial.showQuick();
	}



	protected void initHandler() {
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				AppMessage type = AppMessage.values()[msg.what];
				switch (type) {
				case MESSAGE_FROM_SELF:
					FightMessage fMsg = (FightMessage) msg.obj;
					Shape shape = FightMessage.getShapeFromMessage(fMsg);
					if (mSensorAndSoundThread != null) {
						mSensorAndSoundThread.playShapeSound(shape);
					}

					if (shape==mSpellDatas.get(mPartCounter).shape) {
						addSpellCounter();
						mCastResult.setPictureAndFade(R.drawable.result_ok);
					} else {
						mCastResult.setPictureAndFade(R.drawable.result_bad);
					}
					Log.e("Wizard Fight", shape.toString());
					mIsCastAbilityBlocked = false;
					break;
				default:
				}
			}
		};
	}
	
	public void open(View view) {
		mWizardDial.show();
	}

	public void replay(View view) {
		mSpellAnim.startAnimation();
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
						mChapters.add(p);
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
						mSpellDatas.add(new SpellData(name, pointX, pointY,
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

	void setupSpellScreen() {
		if (mPartCounter >= mSpellDatas.size()) {
			finish();
			return;
		}
		SpellData sd = mSpellDatas.get(mPartCounter);
		mSpellName.setText(sd.name);
		ArrayList<Double[]> t = new ArrayList<Double[]>();
		for (int i = 0; (i < sd.pointsX.size()) && (i < sd.pointsY.size()); i++) {
			t.add(new Double[] { sd.pointsX.get(i), sd.pointsY.get(i) });
		}
		mSpellAnim.setTrajectory(t, sd.rotate, sd.round);
		mSpellAnim.startAnimation();
	}

	void addSpellCounter() {
		mSpellCount++;
		if (mSpellCount == mSpellRepeat) {
			mSpellCount = 0;
			mPartCounter++;
			mWizardDial.setContent(mChapters.get(mPartCounter));
			mWizardDial.show();
		}
		mSpellCounter.setText(mSpellCount + "/" + mSpellRepeat);
	}
}
