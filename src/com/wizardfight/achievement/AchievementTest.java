package com.wizardfight.achievement;

import android.app.Activity;
import android.app.Dialog;
import android.content.IntentSender;
import android.os.Bundle;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.wizardfight.R;
import com.wizardfight.achievement.achievementsTypes.AchievementSpell;
import com.wizardfight.fight.FightActivity;
import com.wizardfight.fight.FightCore;
import com.wizardfight.fight.FightCore.CoreAction;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class AchievementTest implements Observer, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    public static final boolean D = true;
    public static AchievementTest instance;
    private int mLastHP = FightActivity.PLAYER_HP;
    private int mLastMP = FightActivity.PLAYER_MANA;
    private int mLastEnemyHP = FightActivity.PLAYER_HP;
    private GoogleApiClient mGoogleApiClient;
    // Are we currently resolving a connection failure?
    private boolean mResolvingConnectionFailure = false;
    private final Activity activity;
    private final List<AchievementSpell> achievementSpellList =new ArrayList<AchievementSpell>();

    private AchievementTest(Activity activity) {
        this.activity = activity;
        mGoogleApiClient = new GoogleApiClient.Builder(activity.getApplicationContext())
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
        achievementSpellList.clear();
        AchievementParser.parse(activity.getResources().openRawResource(R.raw.achievements),achievementSpellList,mGoogleApiClient);
        Toast.makeText(activity,Integer.toString(achievementSpellList.size()),Toast.LENGTH_LONG);
    }

    @Override
    public void update(Observable o, Object action) {
        CoreAction ca = (CoreAction) action;
        FightCore c = (FightCore) o;
         switch (ca) {
            case CM_BT_STATE_CHANGE:
                break;
            case CM_CONNECTION_FAIL:
                break;
            case CM_COUNTDOWN_END:
                break;
            case CM_DEVICE_NAME:
                break;
            case CM_ENEMY_CAST:
                break;
            case CM_ENEMY_HEALTH_MANA:
               /* int enemyHP = c.getEnemyState().getHealth();
                if (enemyHP < mLastEnemyHP) {
                    mCausedDamage += mLastEnemyHP - enemyHP;
                }
                mLastEnemyHP = enemyHP;*/
                break;
            case CM_ENEMY_NEW_BUFF:
                break;
            case CM_ENEMY_READY:
                break;
            case CM_ENEMY_REMOVED_BUFF:
                break;
            case CM_FIGHT_END:
               /* mLastHP = FightActivity.PLAYER_HP;
                mLastMP = FightActivity.PLAYER_MANA;
                Target winner = c.getData().getWinner();
                Log.e("Wizard Fight", "[STAT] WINNER: " + winner);
                if (winner == Target.SELF) {
                    mWins++;
                } else {
                    mLosses++;
                }*/
                break;
            case CM_FIGHT_START:
                break;
            case CM_SELF_HEALTH_CHANGED:
                /*int hp = c.getSelfState().getHealth();
                if (hp > mLastHP) {
                    mRestoredHealth += hp - mLastHP;
                } else {
                    mReceivedDamage += mLastHP - hp;
                }
                mLastHP = hp;*/
                break;
            case CM_INFO_STRING:
                break;
            case CM_SELF_MANA_CHANGED:
               /* int mp = c.getSelfState().getHealth();
                if (mp < mLastMP) {
                    mWastedMana += mLastMP - mp;
                }
                mLastMP = mp;*/
                break;
            case CM_MESSAGE_TO_SEND:
                break;
            case CM_SELF_NEW_BUFF:
                break;
            case CM_SELF_REMOVED_BUFF:
                break;
            case CM_SELF_CAST_NOMANA:
                break;
            case CM_SELF_CAST_SUCCESS:
                for(AchievementSpell achievementSpell:achievementSpellList) {
                    achievementSpell.update(c.getData().getSelfShape());
                }
                break;
            case CM_SELF_SHIELD_BLOCK:
                break;
            default:
                break;
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
        // The connection has been interrupted.
        // Disable any UI components that depend on Google APIs
        // until onConnected() is called.
        Toast.makeText(activity," onConnectionSuspended",Toast.LENGTH_LONG).show();
        mGoogleApiClient.connect();
    }
    public int init_code=567890;

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // This callback is important for handling errors that
        // may occur while attempting to connect with Google.
      //   Toast.makeText(activity," onConnectionFailed",Toast.LENGTH_LONG).show();
        mResolvingConnectionFailure = resolveConnectionFailure(mGoogleApiClient,
                    result, init_code);
    }

    private boolean resolveConnectionFailure(GoogleApiClient client, ConnectionResult result, int requestCode) {
        if (result.hasResolution()) {
            try {
                result.startResolutionForResult(activity, requestCode);
            } catch (IntentSender.SendIntentException e) {
                // The intent was canceled before it was sent.  Return to the default
                // state and attempt to connect to get an updated ConnectionResult.
                Toast.makeText(activity,"got it",Toast.LENGTH_LONG).show();
                client.connect();
                return false;
            }
            return true;
        } else {
            // not resolvable... so show an error message
            int errorCode = result.getErrorCode();
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(errorCode,
                    activity, requestCode);
            if (dialog != null) {
                dialog.show();
            }
            return false;
        }
    }

    public boolean isConnected() {
        return mGoogleApiClient.isConnected();
    }

    public void connect(){
        mGoogleApiClient.connect();
    }

    public void showAchievements(Activity a, int requestCode) {
        if (!isConnected()) {
            connect();
        }
        a.startActivityForResult(Games.Achievements.getAchievementsIntent(mGoogleApiClient), requestCode);
    }

    public boolean Achievements(int resultCode) {
        mResolvingConnectionFailure = false;
        if (resultCode == Activity.RESULT_OK) {
            mGoogleApiClient.connect();
        }

        return resultCode != Activity.RESULT_OK;
    }

}
