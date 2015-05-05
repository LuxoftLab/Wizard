package com.wizardfight;

import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.games.GamesActivityResultCodes;
import com.wizardfight.achievement.AchievementsObserver;
import com.wizardfight.cast.AcceleratorThread;
import com.wizardfight.remote.WifiService;

/*
 * Main menu. Checks Bluetooth availability and 
 * checks default screen orientation (for sensor data purposes)
 */
public class MainMenu extends Activity {
    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    // Intent request codes
    // BT - Bluetooth, GA - GoogleApi
    enum Requests {
        BT_CREATE_GAME, BT_JOIN_GAME, GA_REQUEST_ACHIEVEMENTS
    }

    AchievementsObserver achievementTest;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /* Full screen */
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.main_menu);

        // send context to WifiService to read player name
        WifiService.setContext(getBaseContext());

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null) {
            // If the adapter is null, then Bluetooth is not supported
            Toast.makeText(this, R.string.bt_not_available, Toast.LENGTH_LONG)
                    .show();
            findViewById(R.id.buttonCreateGame).setVisibility(View.GONE);
            findViewById(R.id.buttonJoinGame).setVisibility(View.GONE);
            findViewById(R.id.buttonDesktopConnection).setVisibility(View.GONE);

        } else {
            // if no player name - set as BT name//todo get name from GooglePlay
            String bluetoothName = mBluetoothAdapter.getName();
            if (bluetoothName != null) {
                SharedPreferences appPrefs = PreferenceManager
                        .getDefaultSharedPreferences(getBaseContext());
                String pName = appPrefs.getString("player_name", "");
                if (pName.equals("")) {
                    SharedPreferences.Editor editor = appPrefs.edit();
                    editor.putString("player_name", bluetoothName);
                    editor.commit();
                }
            }
        }

        //init sound
        new Thread() {
            @Override
            public void run() {
                Sound.init(getApplicationContext());
            }
        }.start();


        // volume buttons control multimedia volume
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        // get default device orientation
        int screenOrientation = getDeviceDefaultOrientation();
        AcceleratorThread.ORIENTATION_HORIZONTAL =
                (screenOrientation == Configuration.ORIENTATION_LANDSCAPE);
        achievementTest= AchievementsObserver.getInstance(this);
        SharedPreferences appPrefs = PreferenceManager
                .getDefaultSharedPreferences(getBaseContext());
        boolean autoConnectPlayGames = appPrefs.getBoolean("autoconnect_google",true);
        if(autoConnectPlayGames) {
            achievementTest.connect();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public void onDestroy() {
        Sound.release();
        super.onDestroy();
    }

    public void goToCreateGame(View view) {
        if (!mBluetoothAdapter.isEnabled()) {
            requestBluetooth(Requests.BT_CREATE_GAME);
        } else {
            BluetoothService btService = BluetoothService.getInstance();
            btService.init();
            btService.setAsServer();
            startActivity(new Intent(this, BtFightActivity.class));
        }
    }

    public void goToJoinGame(View view) {
        if (!mBluetoothAdapter.isEnabled()) {
            requestBluetooth(Requests.BT_JOIN_GAME);
        } else {
            startActivity(new Intent(this, DeviceListActivity.class));
        }
    }

    public void goToTestMode(View view) {
        startActivity(new Intent(this, TestFightActivity.class));
    }

    public void goToHelp(View view) {
        startActivity(new Intent(this, Tutorial.class));
    }

    public void goToSpellbook(View view) {
        startActivity(new Intent(this, Spellbook.class));
    }

    public void goToDesktopConnection(View view) {
        startActivity(new Intent(this, DesktopConnection.class));
    }

    public void goToAchievements(View view) {
        achievementTest.showAchievements(this, Requests.GA_REQUEST_ACHIEVEMENTS.ordinal());
    }

    public void goToSettings(View view) {
        startActivity(new Intent(this, WPreferences.class));
    }

    public void exit(View view) {
        BluetoothService.getInstance().release();
        finish();
    }

    void requestBluetooth(Requests r) {
        Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableIntent, r.ordinal());
    }

    private final int getDeviceDefaultOrientation() {
        WindowManager windowManager = ((WindowManager) getSystemService(Context.WINDOW_SERVICE));

        Configuration config = getResources().getConfiguration();

        int rotation = windowManager.getDefaultDisplay().getRotation();

        if (((rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180) && config.orientation == Configuration.ORIENTATION_LANDSCAPE)
                || ((rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) && config.orientation == Configuration.ORIENTATION_PORTRAIT)) {
            return Configuration.ORIENTATION_LANDSCAPE;
        } else {
            return Configuration.ORIENTATION_PORTRAIT;
        }
    }

    @Override
    public void onBackPressed() {
        exit(null);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (Requests.values().length > requestCode) {
            Requests request = Requests.values()[requestCode];
            switch (request) {
                case BT_CREATE_GAME:
                    if(isBtOn(resultCode)) {
                        BluetoothService btService = BluetoothService.getInstance();
                        btService.init();
                        btService.setAsServer();
                        startActivity(new Intent(this, BtFightActivity.class));
                    }
                    break;
                case BT_JOIN_GAME:
                    if(isBtOn(resultCode)) {
                        startActivity(new Intent(this, DeviceListActivity.class));
                    }
                    break;
                case GA_REQUEST_ACHIEVEMENTS:
                    Log.d("123", "onActivityResult with requestCode == RC_SIGN_IN, responseCode="
                            + resultCode + ", intent=" + data);
                    if(achievementTest.isAchievementsLoadFailed(resultCode)) {
                        showActivityResultError(this, requestCode, resultCode);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private boolean isBtOn(int resultCode){
        if (resultCode != RESULT_OK) {
            // User did not enable Bluetooth or an error occured
            Toast.makeText(this, R.string.bt_not_enabled, Toast.LENGTH_SHORT)
                    .show();

        }
        return resultCode == RESULT_OK;
    }


    public static void showActivityResultError(Activity activity, int requestCode, int actResp) {
        if (activity == null) {
            Log.e("BaseGameUtils", "*** No Activity. Can't show failure dialog!");
            return;
        }
        Toast errorToast = null;
        switch (actResp) {
            case GamesActivityResultCodes.RESULT_APP_MISCONFIGURED:
                errorToast = Toast.makeText(activity,
                        "app_misconfigured", Toast.LENGTH_SHORT);
                break;
            case GamesActivityResultCodes.RESULT_SIGN_IN_FAILED:
                errorToast = Toast.makeText(activity,
                        "R.string.sign_in_failed", Toast.LENGTH_SHORT);
                break;
            case GamesActivityResultCodes.RESULT_LICENSE_FAILED:
                errorToast = Toast.makeText(activity,
                        "R.string.license_failed", Toast.LENGTH_SHORT);
                break;
            default:
                // No meaningful Activity response code, so generate default Google
                // Play services dialog
                final int errorCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity);
                Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(errorCode, activity, requestCode, null);
                if(errorDialog != null) errorDialog.show();
        }
        if (errorToast != null) {
            errorToast.show();
        }
    }
}
