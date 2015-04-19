package com.wizardfight.achievement;

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

import java.util.*;

public class AchievementTest implements Observer, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    public static AchievementTest instance;
    private final GoogleApiClient mGoogleApiClient;
    private final Activity mActivity;
    private final Set<Achievement> mAchievementList =new HashSet<Achievement>();
    public int init_code=567890;

    private AchievementTest(Activity activity) {
        this.mActivity = activity;
        mGoogleApiClient = new GoogleApiClient.Builder(activity)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        loadAchievements();
    }

    public static AchievementTest getInstance(Activity activity) {
        if (instance == null)
            instance = new AchievementTest(activity);
        return getInstance();
    }

    public static AchievementTest getInstance() {
        return instance;
    }

    public void loadAchievements() {
        AchievementParser.parse(mActivity.getResources().openRawResource(R.raw.achievements), mAchievementList,mGoogleApiClient);
    }

    @Override
    public void update(Observable o, Object action) {
        for(Achievement achievement: mAchievementList) {
            achievement.update((FightCore)o, (FightCore.CoreAction)action);
        }
        if(action== FightCore.CoreAction.CM_FIGHT_START) {
            for(Achievement achievement: mAchievementList) {
                achievement.onStart((FightCore) o);
            }
        }
        if(action== FightCore.CoreAction.CM_FIGHT_END) {
            for(Achievement achievement: mAchievementList) {
                achievement.onFinish((FightCore) o);
            }
        }
    }
    @Override
    public void onConnected(Bundle connectionHint) {
        // Connected to Google Play services!
        // The good stuff goes here.
        //Toast.makeText(this, "Connected", Toast.LENGTH_LONG).show();

    }

    @Override
    public void onConnectionSuspended(int cause) {
        //reconnect when connection has been interrupted.
        mGoogleApiClient.connect();
    }

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
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(),
                    mActivity, init_code);
            if (dialog != null) {
                dialog.show();
            }
        }
    }

    public boolean isConnected() {
        return mGoogleApiClient.isConnected();
    }

    public void connect(){
        mGoogleApiClient.connect();
    }

    public void showAchievements(final Activity a, final int requestCode) {
        if (!isConnected()) {
            mGoogleApiClient.registerConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                @Override
                public void onConnected(Bundle bundle) {
                    showAchievementsActivity(a, requestCode);
                    mGoogleApiClient.unregisterConnectionCallbacks(this);
                }
                @Override
                public void onConnectionSuspended(int i) {}
            });
            connect();
        }else {
            showAchievementsActivity(a, requestCode);
        }
    }

    private void showAchievementsActivity(final Activity a, final int requestCode){
        a.startActivityForResult(Games.Achievements.getAchievementsIntent(mGoogleApiClient), requestCode);
    }

    public boolean achievements(int resultCode) {
        if (resultCode == Activity.RESULT_OK) {
            mGoogleApiClient.connect();
        }
        return resultCode != Activity.RESULT_OK;
    }
}
