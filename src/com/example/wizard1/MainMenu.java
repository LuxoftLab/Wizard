package com.example.wizard1;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class MainMenu extends Activity {
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*Fullscreen*/
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.main_menu);
    }
    public void goToGameCreate(View view) {
       // if (isBluetoothOn())
            startActivity(new Intent(this, WizardFight.class));
    }
    public void goToListOfBluetooth(View view){
//        startActivity(new Intent(this,listOfBluetooth.class));
    }

    public void goToHelp(View view) {
//        startActivity(new Intent(this, Spellbook.class));
    }

    public void goToSpellbook(View view) {
        startActivity(new Intent(this, Spellbook.class));
    }

    public void Exit(View view) {
        finish();
    }

    private boolean isBluetoothOn() {
        BluetoothAdapter a = BluetoothAdapter.getDefaultAdapter();
        if ((a == null) || (!a.isEnabled())) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, 2);
        }
        if ((a == null) || (!a.isEnabled())) {
            //findViewById(R.id.textErrorBluetooth).setVisibility(View.VISIBLE);
            return false;
        }
        //findViewById(R.id.textErrorBluetooth).setVisibility(View.INVISIBLE);
        return true;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {

            }
        }
    }
}
