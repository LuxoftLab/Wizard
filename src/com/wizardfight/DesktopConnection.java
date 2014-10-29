package com.wizardfight;

import com.wizardfight.remote.WifiService;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class DesktopConnection extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.connection);
	}
	
	public void connect(View view) {
		EditText text = (EditText) findViewById(R.id.ip);
		WifiService.init(text.getText().toString());
	} 
}
