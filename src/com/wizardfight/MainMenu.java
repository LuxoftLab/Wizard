package com.wizardfight;

import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.*;
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
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesActivityResultCodes;
import com.wizardfight.cast.AcceleratorThread;
import com.wizardfight.remote.WifiService;

/*
 * Main menu. Checks Bluetooth availability and 
 * checks default screen orientation (for sensor data purposes)
 */
public class MainMenu extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    //google APIs object
    private GoogleApiClient mGoogleApiClient;

    // Are we currently resolving a connection failure?
    private boolean mResolvingConnectionFailure = false;

    // Has the user clicked the sign-in button?
    private boolean mSignInClicked = false;

    // Set to true to automatically start the sign in flow when the Activity starts.
    // Set to false to require the user to click the button in order to sign in.
    private boolean mAutoStartSignInFlow = true;



    // Intent request codes
    // BT - Bluetooth, GA - GoogleApi
    enum Requests {
        BT_CREATE_GAME, BT_JOIN_GAME, GA_RESOLVE_ERROR, GA_REQUEST_ACHIEVEMENTS
    }

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
            // if no player name - set as BT name
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

        //init googke play Api
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
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
        if(mGoogleApiClient.isConnected()){
            startActivityForResult(Games.Achievements.getAchievementsIntent(mGoogleApiClient), Requests.GA_REQUEST_ACHIEVEMENTS.ordinal());
            Games.Achievements.increment(mGoogleApiClient, "CgkI2YKzhoMbEAIQAQ", 1);
        }else {
            mGoogleApiClient.connect();
            mSignInClicked = true;
        }
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
            // When the request to enable Bluetooth returns


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
                case GA_RESOLVE_ERROR:
                    Log.d("123", "onActivityResult with requestCode == RC_SIGN_IN, responseCode="
                            + resultCode + ", intent=" + data);
                    mSignInClicked = false;
                    mResolvingConnectionFailure = false;
                    if (resultCode == RESULT_OK) {
                        mGoogleApiClient.connect();
                    } else {
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

    @Override
    public void onConnected(Bundle connectionHint) {
        // Connected to Google Play services!
        // The good stuff goes here.
        Toast.makeText(this,"Connected",Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // The connection has been interrupted.
        // Disable any UI components that depend on Google APIs
        // until onConnected() is called.
        Toast.makeText(this," onConnectionSuspended",Toast.LENGTH_LONG).show();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // This callback is important for handling errors that
        // may occur while attempting to connect with Google.
        Toast.makeText(this," onConnectionFailed",Toast.LENGTH_LONG).show();
        if ((!mResolvingConnectionFailure)&&(mSignInClicked || mAutoStartSignInFlow)) {
            mAutoStartSignInFlow = false;
            mSignInClicked = false;
            mResolvingConnectionFailure = resolveConnectionFailure(this, mGoogleApiClient,
                    result, Requests.GA_RESOLVE_ERROR.ordinal(),"Все плохо");
        }
    }
    public static boolean resolveConnectionFailure(Activity activity,
                                                   GoogleApiClient client, ConnectionResult result, int requestCode,
                                                   String fallbackErrorMessage) {
        if (result.hasResolution()) {
            try {
                result.startResolutionForResult(activity, requestCode);
                return true;
            } catch (IntentSender.SendIntentException e) {
                // The intent was canceled before it was sent.  Return to the default
                // state and attempt to connect to get an updated ConnectionResult.
                client.connect();
                return false;
            }
        } else {
            // not resolvable... so show an error message
            int errorCode = result.getErrorCode();
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(errorCode,
                    activity, requestCode);
            if (dialog != null) {
                dialog.show();
            } else {
                // no built-in dialog: show the fallback error message
                Toast.makeText(activity, fallbackErrorMessage, Toast.LENGTH_SHORT)
                        .show();
            }
            return false;
        }
    }

    public static void showActivityResultError(Activity activity, int requestCode, int actResp) {
        if (activity == null) {
            Log.e("BaseGameUtils", "*** No Activity. Can't show failure dialog!");
            return;
        }
        Toast errorDialog;
        switch (actResp) {
            case GamesActivityResultCodes.RESULT_APP_MISCONFIGURED:
                errorDialog = Toast.makeText(activity,
                        "app_misconfigured", Toast.LENGTH_SHORT);
                break;
            case GamesActivityResultCodes.RESULT_SIGN_IN_FAILED:
                errorDialog = Toast.makeText(activity,
                        "R.string.sign_in_failed", Toast.LENGTH_SHORT);
                break;
            case GamesActivityResultCodes.RESULT_LICENSE_FAILED:
                errorDialog = Toast.makeText(activity,
                        "R.string.license_failed", Toast.LENGTH_SHORT);
                break;
            default:
                // No meaningful Activity response code, so generate default Google
                // Play services dialog
                final int errorCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity);
                GooglePlayServicesUtil.getErrorDialog(errorCode, activity, requestCode, null).show();
                Log.e("BaseGamesUtils",
                            "No standard error dialog available. Making fallback dialog.");
                errorDialog = Toast.makeText(activity, "errorDescription",Toast.LENGTH_SHORT);
        }
        errorDialog.show();
    }
}
