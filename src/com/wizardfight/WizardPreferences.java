package com.wizardfight;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class WizardPreferences extends PreferenceActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.prefs);
	}
}
