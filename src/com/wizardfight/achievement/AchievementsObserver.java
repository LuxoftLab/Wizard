package com.wizardfight.achievement;

import java.util.*;

import android.app.Activity;
import android.app.Dialog;
import android.content.IntentSender;
import android.os.Bundle;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.wizardfight.R;
import com.wizardfight.achievement.achievementsTypes.Achievement;
import com.wizardfight.fight.FightCore;


public class AchievementsObserver extends Observable implements Observer, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    /**
     * Singleton
     */
    private static AchievementsObserver instance;
    private final GoogleApiClient mGoogleApiClient;
    /**
     * Activity for show isAchievementsLoadFailed labels.
     * <img src="https://developers.google.com/games/services/images/achievementIncremental.png" />
     */
    private final Activity mActivity;
    private final Set<Achievement> achievements=new HashSet<Achievement>();
    /**
     * Random number
     */
    private final int init_code=568790;

    private AchievementsObserver(Activity activity) {
        this.mActivity = activity;
        mGoogleApiClient = new GoogleApiClient.Builder(activity)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        loadAchievements();
    }

    /**
     *
     * @param activity {@link #mActivity}
     * @return {@link #getInstance()}
     */
    public static AchievementsObserver getInstance(Activity activity) {
        if (instance == null)
            instance = new AchievementsObserver(activity);
        return getInstance();
    }

    /**
     * @return Returns the AchievementsObserve object associated with the current application.
     */
    private static AchievementsObserver getInstance() {
        return instance;
    }

    private void loadAchievements() {
        AchievementsFactory.parse(mActivity.getResources().openRawResource(R.raw.achievements),
                mGoogleApiClient, achievements);
    }

    @Override
    public void update(Observable o, Object action) {
        for(Achievement achievement : achievements){
            achievement.update((FightCore)o,(FightCore.CoreAction)action);
        }
    }

    /**
     * This method is called on every success connection.
     * @param connectionHint Bundle of data provided to clients by Google Play services.
     *                       May be null if no content is provided by the service.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
    }

    /**
     * This method performs reconnection when connection has been interrupted.
     * @param cause Reason code of interruption.
     */
    @Override
    public void onConnectionSuspended(int cause) {
        mGoogleApiClient.connect();
    }

    /**
     * Called when the connection requires additional action from user.
     * @param result Reason of fail.
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        //try to resolve
        if (result.hasResolution()) {
            try {
                result.startResolutionForResult(mActivity, init_code);
            } catch (IntentSender.SendIntentException e) {
                mGoogleApiClient.connect();
            }
        } else {
            //show dialog with requesting installation Play Games
            //TODO: change dialog.
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(),
                    mActivity, init_code);
            if (dialog != null) {
                dialog.show();
            }
        }
    }

    /**
     * Checks if the client is currently connected to the service, so that requests to other methods will succeed.
     * @return Returns true if the specified listener is currently registered to receive connection events.
     */
    private boolean isConnected() {
        return mGoogleApiClient.isConnected();
    }

    /**
     * Connects the client to Google Play services. This method returns immediately,
     * and connects to the service in the background.If the client is already connected
     * or connecting, this method does nothing.
     */
    public void connect(){
        mGoogleApiClient.connect();
    }

    /**
     * The request for the opening of a window with a list of isAchievementsLoadFailed the game.
     * @param activity Parent activity
     * @param requestCode The code for the registration of the application for the opening of the window.
     */
    public void showAchievements(final Activity activity, final int requestCode) {
        if (!isConnected()) {
            mGoogleApiClient.registerConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                @Override
                public void onConnected(Bundle bundle) {
                    showAchievementsActivity(activity, requestCode);
                    mGoogleApiClient.unregisterConnectionCallbacks(this);
                }
                @Override
                public void onConnectionSuspended(int i) {}
            });
            connect();
        }else {
            showAchievementsActivity(activity, requestCode);
        }
    }

    private void showAchievementsActivity(final Activity activity, final int requestCode){
        activity.startActivityForResult(Games.Achievements.getAchievementsIntent(mGoogleApiClient), requestCode);
    }

    /**
     * @param resultCode code of activity show result.
     * @return Returns true if failed to load achievements list.
     */
    public boolean isAchievementsLoadFailed(int resultCode) {
        if (resultCode == Activity.RESULT_OK) {
            mGoogleApiClient.connect();
        }
        return resultCode != Activity.RESULT_OK;
    }
}
