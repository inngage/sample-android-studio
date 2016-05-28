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

import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;


public class NotificationActivity extends AppCompatActivity {

    private static final String TAG = "NotificationActivity";

    String notifyID, url, message, image;
    JSONObject jsonBody;
    Utilities utils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        notifyID = bundle.getString("notifyID");
        message = bundle.getString("message");
        image = bundle.getString("image");

        Log.i(TAG, "NotificationActivity called, getExtras()");

        utils = new Utilities();
        jsonBody = new JSONObject();
        jsonBody = utils.createNotificationCallback(notifyID);
        utils.doPost(jsonBody, IntegrationConstants.API_CALLBACK);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        TextView textView = (TextView) findViewById(R.id.textView);
        ImageView imageView = (ImageView) findViewById(R.id.imageView);



        url = bundle.getString("url");

        if (url != null) {

            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(browserIntent);
        }
        if (image != null) {

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
        if (message != null) {

            textView.setText(message);
        }
        Log.i(TAG, "notifyID: " + notifyID + ", URL: " + url);
    }
}