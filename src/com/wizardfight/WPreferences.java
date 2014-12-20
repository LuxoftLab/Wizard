package com.wizardfight;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/*
 * Class needed for settings menu
 */
public class WPreferences extends PreferenceActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.prefs);
	}
}
