package trips.sampleapp.loop.ms.tripssampleapp;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.multidex.MultiDexApplication;
import android.util.Patterns;

import com.mixpanel.android.mpmetrics.MixpanelAPI;

import org.json.JSONException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import ms.loop.loopsdk.core.ILoopSDKCallback;
import ms.loop.loopsdk.core.ISignalListener;
import ms.loop.loopsdk.core.LoopSDK;
import ms.loop.loopsdk.processors.DriveProcessor;
import ms.loop.loopsdk.processors.KnownLocationProcessor;
import ms.loop.loopsdk.processors.TripProcessor;
import ms.loop.loopsdk.profile.KnownLocation;
import ms.loop.loopsdk.providers.LoopLocation;
import ms.loop.loopsdk.providers.LoopLocationProvider;
import ms.loop.loopsdk.signal.Signal;
import ms.loop.loopsdk.signal.SignalConfig;
import ms.loop.loopsdk.util.Logger;
import ms.loop.loopsdk.util.LoopError;

/**
 * Created by on 5/24/16.
 */
public class SampleAppApplication extends MultiDexApplication implements ILoopSDKCallback {

    private static final String TAG = SampleAppApplication.class.getSimpleName();
    private static KnownLocationProcessor knownLocationProcessor ;
    private static Context applicationContext;
    private static boolean sdkInitialized = false;

    public static TripProcessor tripProcessor;
    public static DriveProcessor driveProcessor;


    @Override
    public void onCreate() {
        super.onCreate();

        SignalConfig.add(TAG, "/system", "/test", "*", SignalConfig.SIGNAL_SEND_MODE_REALTIME);

        // initialize the Loop SDK. create an account to get your appId and appToken
        String appId = "YOUR_APP_ID";
        String appToken = "YOUR_APP_TOKEN";

        String userId = "YOUR_USER_ID";
        String deviceId = "YOUR_DEVICE_ID";

        LoopSDK.initialize(this, appId, appToken, userId, deviceId);
        applicationContext = this;
    }
    @Override
    public void onInitialized() {

        // start any required Providers
        LoopLocationProvider.start(SignalConfig.SIGNAL_SEND_MODE_BATCH);

        if (!sdkInitialized) {

            tripProcessor = new TripProcessor();
            driveProcessor = new DriveProcessor();
            knownLocationProcessor = new KnownLocationProcessor();

            // initialize signal processors
            tripProcessor.initialize();
            driveProcessor.initialize();

            sdkInitialized = true;

            // register signal listeners
            LoopSDK.registerSignalListener("drives", "*", new ISignalListener() {
                @Override
                public void onSignal(Signal signal) {
                }
            });

            LoopLocationProvider.registerMainThreadCallback("location", new LoopLocationProvider.ILocationProviderCallback() {
                @Override
                public void onLocationChanged(LoopLocation location) {
                }

                @Override
                public void onModeChanged(int modeFrom, int modeTo, LoopLocation location) {}
                @Override
                public void onKnownLocationEntered(KnownLocation location) {}
                @Override
                public void onKnownLocationExited(KnownLocation location) {}
            });
        }

       // knownLocationProcessor.initialize();
        Intent i = new Intent("android.intent.action.onInitialized").putExtra("status", "initialized");
        this.sendBroadcast(i);
    }

    @Override
    public void onInitializeFailed(LoopError loopError) {}

    @Override
    public void onServiceStatusChanged(String provider, String status, Bundle bundle) {}

    @Override
    public void onDebug(String debugString) {
    }

    public static boolean isLocationTurnedOn(Context context) {
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean locationEnbaled = false;

        try {
            locationEnbaled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);

            if (locationEnbaled) {
                locationEnbaled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            }
        } catch (Exception ex) {
        }
        return locationEnbaled;
    }

    public static void openLocationServiceSettingPage(Context context)
    {
        final Intent locationIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        locationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        if (locationIntent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(locationIntent);
        }
    }
}