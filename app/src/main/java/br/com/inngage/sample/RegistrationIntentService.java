package br.com.inngage.sample;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.google.android.gms.iid.InstanceIDListenerService;

import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Created by viniciusdepaula on 17/05/16.
 */
public class RegistrationIntentService extends IntentService {

    private static final String TAG = "RegIntentService";

    Utilities utils;
    TelephonyManager telephonyManager;

    static Context appContext;
    JSONObject jsonBody, jsonObj;

    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        try {
            // [START register_for_gcm]
            // Initially this call goes out to the network to retrieve the token, subsequent calls
            // are local.
            // [START get_token]
            InstanceID instanceID = InstanceID.getInstance(this);
            String token = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            // [END get_token]
            Log.i(TAG, "GCM Registration Token: " + token);

            // TODO: Implement this method to send any registration to your app's servers.
            sendRegistrationToServer(token);

            // Subscribe to topic channels
            //subscribeTopics(token);

            // [END register_for_gcm]
        } catch (Exception e) {
            Log.d(TAG, "Failed to complete token refresh", e);
        }

        //LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }

    /**
     * Persist registration to third-party servers.
     *
     * Modify this method to associate the user's GCM registration token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token) {

        jsonBody = createSubscriberRequest(token);
        utils = new Utilities();
        utils.doPost(jsonBody, getString(R.string.api_endpoint)+"/subscription/");
    }

    public JSONObject createSubscriberRequest(String regId/*, String identifier*/) {

        jsonBody = new JSONObject();
        jsonObj = new JSONObject();

        AppInfo app = getAppInfo();

        try {

            String _MODEL = android.os.Build.MODEL;
            String _MANUFACTURER = android.os.Build.MANUFACTURER;
            String _LOCALE = getApplicationContext().getResources().getConfiguration().locale.getDisplayCountry();
            String _LANGUAGE = getApplicationContext().getResources().getConfiguration().locale.getDisplayLanguage();
            String _RELEASE = android.os.Build.VERSION.RELEASE;
            telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);

            jsonBody.put("identifier", telephonyManager.getDeviceId());
            jsonBody.put("registration", regId);
            jsonBody.put("platform", IntegrationConstants.PLATFORM);
            jsonBody.put("sdk", IntegrationConstants.SDK);
            jsonBody.put("app_token", getString(R.string.app_token));
            jsonBody.put("device_model", _MODEL);
            jsonBody.put("device_manufacturer", _MANUFACTURER);
            jsonBody.put("os_locale", _LOCALE);
            jsonBody.put("os_language", _LANGUAGE);
            jsonBody.put("os_version", _RELEASE);
            jsonBody.put("app_version", app.getVersionName());
            jsonBody.put("app_installed_in", app.getInstallationDate());
            jsonBody.put("app_updated_in", app.getInstallationDate());
            jsonObj.put("registerSubscriberRequest", jsonBody);

            Log.i(TAG, "JSON Request: " + jsonObj.toString());

        } catch (Throwable t) {

            Log.i(TAG, "Error in createSubscriberRequest: " + t);
        }
        return jsonObj;
    }

    public AppInfo getAppInfo() {

        long installed = 0;

        String packageName = getApplicationContext().getPackageName();

        String updateDate = "";
        String installationDate = "";
        String versionName = "";

        try {

            final PackageManager pm = RegistrationIntentService.this.getPackageManager();
            PackageInfo packageInfo = pm.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            installationDate = dateFormat.format(new Date(packageInfo.firstInstallTime));
            updateDate = dateFormat.format(new Date(packageInfo.lastUpdateTime));
            versionName = packageInfo.versionName;

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return new AppInfo(installationDate, updateDate, versionName);
    }
}

final class AppInfo {

    private final String installationDate;
    private final String updateDate;
    private final String versionName;

    public AppInfo(String installationDate, String updateDate, String versionName) {

        this.installationDate = installationDate;
        this.updateDate = updateDate;
        this.versionName = versionName;
    }

    public String getInstallationDate() {
        return installationDate;
    }

    public String getUpdateDate() {
        return updateDate;
    }

    public String getVersionName() {
        return versionName;
    }
}

final class IntegrationConstants {

    public static final String PLATFORM = "android";
    public static final String SDK = "1";
}

class MyInstanceIDListenerService extends InstanceIDListenerService {

    private static final String TAG = "MyInstanceIDLS";

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. This call is initiated by the
     * InstanceID provider.
     */
    // [START refresh_token]
    @Override
    public void onTokenRefresh() {
        // Fetch updated Instance ID token and notify our app's server of any changes (if applicable).
        Intent intent = new Intent(this, RegistrationIntentService.class);

        Log.i(TAG, "onTokenRefresh called..");

        startService(intent);
    }
    // [END refresh_token]
}

class Utilities {

    private static final String TAG = "Utilities";

    JSONObject jsonBody, jsonObj;

    public void doPost(JSONObject jbonBody, String endpoint) {

        OutputStream os = null;
        InputStream is = null;
        HttpURLConnection conn = null;

        try {

            URL url = new URL(endpoint);
            conn = (HttpURLConnection) url.openConnection();
            String message = jbonBody.toString();
            conn.setReadTimeout( 10000 /*milliseconds*/ );
            conn.setConnectTimeout( 15000 /* milliseconds */ );
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setFixedLengthStreamingMode(message.getBytes().length);

            //make some HTTP header nicety
            conn.setRequestProperty("Content-Type", "application/json;charset=utf-8");
            conn.setRequestProperty("X-Requested-With", "XMLHttpRequest");

            //open

            if (android.os.Build.VERSION.SDK_INT > 9) {
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);
            }

            conn.connect();

            //setup send
            os = new BufferedOutputStream(conn.getOutputStream());
            os.write(message.getBytes());
            //clean up
            os.flush();

            //do somehting with response
            is = conn.getInputStream();
            //String contentAsString = readIt(is, len);

            Log.i(TAG, "Server Response:" + convertStreamToString(is));

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            //clean up
            try {

                if (os != null) {

                    os.close();
                }

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            try {

                if (is != null) {

                    is.close();
                }
            } catch (IOException e) {

                e.printStackTrace();
            }
            if(conn != null) {

                conn.disconnect();
            }
        }
    }

    public String convertStreamToString(java.io.InputStream is) {

        try {

            return new java.util.Scanner(is).useDelimiter("\\A").next();

        } catch (java.util.NoSuchElementException e) {

            return "";
        }
    }

    /*public JSONObject createLocationRequest(String IMEI) {

        jsonBody = new JSONObject();
        jsonObj = new JSONObject();

        try {

            jsonBody.put("uuid", IMEI);
            jsonBody.put("lat", regId);
            jsonBody.put("lon", Constants.PLATFORM);

            Log.i(TAG, "JSON Request: " + jsonObj.toString());

        } catch (Throwable t) {

            Log.i(TAG, "Error in createLocationRequest: " + t);
        }
        return jsonObj;
    }*/

    public JSONObject createNotificationCallback(String $notificationId, String $appToken) {

        jsonBody = new JSONObject();
        jsonObj = new JSONObject();

        try {

            jsonBody.put("id", $notificationId);
            jsonBody.put("app_token", $appToken);
            jsonObj.put("notificationRequest", jsonBody);

            Log.i(TAG, "JSON Request: " + jsonObj.toString());

        } catch (Throwable t) {

            Log.i(TAG, "Error in createNotificationCallbackRequest: " + t);
        }
        return jsonObj;
    }
}