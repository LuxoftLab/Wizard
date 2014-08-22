package com.wizardfight;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.*;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;

public class Spellbook extends Activity {
	/**
	 * Called when the activity is first created.
	 */
	LinearLayout cardLayout;
	CustomScroller scroller;
	int displayWidth, displayHeight;
	ArrayList<RelativeLayout> cards = new ArrayList<RelativeLayout>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.spellbook);
		cardLayout = (LinearLayout) findViewById(R.id.layoutForCards);
		scroller = (CustomScroller) findViewById(R.id.horizontalScrollView);
		Display display = getWindowManager().getDefaultDisplay();

		displayWidth = display.getWidth();
		displayHeight = display.getHeight();
		String name = "";
		String desc = "";
		String img = "";
		int t = 0;
		try {
			XmlPullParser xpp = getResources().getXml(R.xml.spells);
			while (xpp.getEventType() != XmlPullParser.END_DOCUMENT) {
				switch (xpp.getEventType()) {
				case XmlPullParser.START_TAG:
					Log.e("Wizard Fight", "start: " + xpp.getName());
					if (xpp.getName().equals("name"))
						t = 1;
					else if (xpp.getName().equals("description"))
						t = 2;
					else if (xpp.getName().equals("img"))
						t = 3;
					break;
				case XmlPullParser.END_TAG:
					Log.e("Wizard Fight", "end: " + xpp.getName());
					if (xpp.getName().equals("spell"))
						addSpellCard(name, desc, img);
					t = 0;
					break;
				case XmlPullParser.TEXT:
					Log.e("Wizard Fight", "[" + t + "] text: " + xpp.getText());
					switch (t) {
					case 1:
						name = xpp.getText();
						break;
					case 2:
						desc = xpp.getText();
						break;
					case 3:
						img = xpp.getText();
						break;
					}
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

	public void addSpellCard(String name, String description, String img) {
		Log.e("Wizard Fight", "IMG: " + img );
		int w = displayWidth;
		int h = (int) (displayWidth * 1.5);
		if (h > displayHeight) {
			h = displayHeight;
			w = (int) (h / 1.5);
		}

		RelativeLayout r = new RelativeLayout(this);
		RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(w, h);

		int textC = Color.argb(200, 102, 60, 22);

		TextView nameV = new TextView(this);
		nameV.setText(name);
		nameV.setLayoutParams(new ViewGroup.LayoutParams(
				ViewGroup.LayoutParams.FILL_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT));
		nameV.setTextSize(30);
		nameV.setPadding(0, (int) (h / 2.18), 0, 0);
		nameV.setTextColor(textC);
		nameV.setGravity(Gravity.CENTER);

		ScrollView textSV = new ScrollView(this);
		textSV.setLayoutParams(new ViewGroup.LayoutParams((int) (w * 0.88),
				(int) (h * 0.87)));
		textSV.setPadding((int) (w * 0.12), 4 * h / 7, 0, 0);

		TextView descriptionV = new TextView(this);
		descriptionV.setText(description);
		descriptionV.setLayoutParams(new ViewGroup.LayoutParams(
				ViewGroup.LayoutParams.FILL_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT));
		descriptionV.setTextSize(20);
		descriptionV.setTextColor(textC);
		textSV.addView(descriptionV);

		ImageView imV = new ImageView(this);
		imV.setLayoutParams(new ViewGroup.LayoutParams((int) (w * 0.8),
				(int) (w * 0.65)));
		imV.setPadding((int) (w * 0.2), (int) (w * 0.2), 0, 0);
		StringBuilder sb = new StringBuilder(img);
		for (int i = 0; i < sb.length(); i++) {
			if ((sb.charAt(i) == ' ') || (sb.charAt(i) == '\n')) {
				sb.deleteCharAt(i);
				i = -1;
			}
		}
		img = sb.toString();
		try {
			imV.setImageBitmap(BitmapFactory
					.decodeStream(getAssets().open(img)));
		} catch (IOException e) {

		}

		r.setBackgroundResource(R.drawable.card);
		r.setLayoutParams(rlp);

		r.addView(nameV);
		r.addView(textSV);
		r.addView(imV);
		cards.add(r);
		cardLayout.addView(r);
	}
}
