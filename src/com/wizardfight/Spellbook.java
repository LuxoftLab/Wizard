package com.wizardfight;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.*;
import android.widget.*;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;

/*
 * Shows spell book (reads data from XML)
 */
public class Spellbook extends Activity {
	/**
	 * Called when the activity is first created.
	 */
    private LinearLayout mCardLayout;
	private int mDisplayWidth;
    private int mDisplayHeight;
	private final ArrayList<RelativeLayout> mCards = new ArrayList<RelativeLayout>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.spellbook);
		mCardLayout = (LinearLayout) findViewById(R.id.layoutForCards);
		Display display = getWindowManager().getDefaultDisplay();

		mDisplayWidth = display.getWidth();
		mDisplayHeight = display.getHeight();
		String name = "";
		String desc = "";
		String img = "";
		int t = 0;
		try {
			XmlPullParser xpp = getResources().getXml(R.xml.spells);
			while (xpp.getEventType() != XmlPullParser.END_DOCUMENT) {
				switch (xpp.getEventType()) {
				case XmlPullParser.START_TAG:
					if (xpp.getName().equals("name"))
						t = 1;
					else if (xpp.getName().equals("description"))
						t = 2;
					else if (xpp.getName().equals("img"))
						t = 3;
					break;
				case XmlPullParser.END_TAG:
					if (xpp.getName().equals("spell"))
						addSpellCard(name, desc, img);
					t = 0;
					break;
				case XmlPullParser.TEXT:
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

	void addSpellCard(String name, String description, String img) {
		// scale card
		int w = mDisplayWidth;
		int h = (int) (mDisplayWidth * 1.5);
		if (h > mDisplayHeight) {
			h = mDisplayHeight;
			w = (int) (h / 1.5);
		}

		RelativeLayout r = new RelativeLayout(this);
		RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(w, h);

		int textColor = Color.argb(200, 102, 60, 22);

		// spell name
		DisplayMetrics metrics = getResources().getDisplayMetrics();
		TextView nameV = new TextView(this);
		nameV.setText(name);
		nameV.setTextSize(metrics.widthPixels/15  / (metrics.xdpi/160));
		nameV.setTextColor(textColor);
		
		// scroll with description
		ScrollView textScroll = new ScrollView(this);
		textScroll.setLayoutParams(new ViewGroup.LayoutParams((int) (w * 0.88),
				(int) (h * 0.87)));
		textScroll.setPadding((int) (w * 0.12), 4 * h / 7, 0, 0);

		TextView descriptionV = new TextView(this);
		descriptionV.setText(description);
		descriptionV.setLayoutParams(new ViewGroup.LayoutParams(
				ViewGroup.LayoutParams.FILL_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT));
		descriptionV.setTextSize(metrics.widthPixels/20 / (metrics.xdpi/160));
		descriptionV.setTextColor(textColor);
		textScroll.addView(descriptionV);

		// spell shape
		ImageView imShape = new ImageView(this);
		imShape.setLayoutParams(new ViewGroup.LayoutParams((int) (w * 0.8),
				(int) (w * 0.65)));
		imShape.setPadding((int) (w * 0.2), (int) (w * 0.2), 0, 0);
		StringBuilder sb = new StringBuilder(img);
		for (int i = 0; i < sb.length(); i++) {
			if ((sb.charAt(i) == ' ') || (sb.charAt(i) == '\n')) {
				sb.deleteCharAt(i);
				i = -1;
			}
		}
		img = sb.toString();
		try {
			imShape.setImageBitmap(BitmapFactory
					.decodeStream(getAssets().open(img)));
		} catch (IOException e) {
            Log.e("Wizard Fight","SpellBookError",e);
		}
		
		//spell icon 
		ImageView imIcon = new ImageView(this);
		imIcon.setLayoutParams(new ViewGroup.LayoutParams((int) (0.17*w), (int) (0.15*w)));
		imIcon.setPadding(0, 0, (int) (0.03*w), 0);
		try {
			imIcon.setImageBitmap(BitmapFactory
					.decodeStream(getAssets().open("icons/" + img)));
		} catch (IOException e) {
            Log.e("Wizard Fight","SpellBookError",e);
		}
		
		LinearLayout title = new LinearLayout(this);
		title.addView(imIcon);
		title.addView(nameV);
		title.setLayoutParams(new ViewGroup.LayoutParams(
				ViewGroup.LayoutParams.FILL_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT));
		title.setPadding(0, (int) (h / 2.18), 0, 0);
		title.setGravity(Gravity.CENTER);
		
		r.setBackgroundResource(R.drawable.card);
		r.setLayoutParams(rlp);

		r.addView(title);
		r.addView(textScroll);
		r.addView(imShape);
		mCards.add(r);
		mCardLayout.addView(r);
	}
}
