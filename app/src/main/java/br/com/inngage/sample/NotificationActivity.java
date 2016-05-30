package br.com.inngage.sample;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;


public class NotificationActivity extends AppCompatActivity {

    private static final String TAG = "NotificationActivity";

    String notifyID, url, message, image;
    JSONObject jsonBody;
    Utilities utils;
    JSONArray  additionalData;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        TextView textView = (TextView) findViewById(R.id.textView);
        ImageView imageView = (ImageView) findViewById(R.id.imageView);

        intent = getIntent();
        Bundle bundle = intent.getExtras();

        if (bundle.getString("notifyID") != null) {

            notifyID = bundle.getString("notifyID");

            /* Start Inngage Callback API */

            utils = new Utilities();
            jsonBody = new JSONObject();
            jsonBody = utils.createNotificationCallback(notifyID, getString(R.string.app_token));
            utils.doPost(jsonBody, getString(R.string.api_endpoint)+"/notification/");

            /* End Inngage Callback API */
        }
        if (bundle.getString("message") != null) {

            message = bundle.getString("message");

            textView.setText(message);
        }
        if(bundle.getString("image") != null) {

            image = bundle.getString("image");

            if (android.os.Build.VERSION.SDK_INT > 9) {
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);
            }

            URL url = null;
            try {
                url = new URL(image);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            Bitmap bmp = null;
            try {

                bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());

            } catch (IOException e) {

                e.printStackTrace();
            }
            imageView.setImageBitmap(bmp);
        }
        if(bundle.getString("url") != null) {

            url = bundle.getString("url");

            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(browserIntent);
        }

        if(bundle.getString("additional_data") != null) {

            try {

                String tipo = "";

                additionalData = new JSONArray(bundle.getString("additional_data"));

                jsonBody = new JSONObject();
                jsonBody = additionalData.getJSONObject(0);

                if(jsonBody.getString("tipo") != null) {

                    tipo = jsonBody.getString("tipo");

                    if(tipo.equals("activity1")) {

                        intent = new Intent(NotificationActivity.this, Activity1.class);
                        startActivity(intent);
                    }

                    if(tipo.equals("activity2")) {

                        intent = new Intent(NotificationActivity.this, Activity2.class);
                        startActivity(intent);
                    }
                }

            } catch (JSONException e) {

                Log.e(TAG, "Error parsing data: " + e.toString());
            }
        }

        Log.i(TAG, "NotificationActivity called, getExtras()");
    }
}