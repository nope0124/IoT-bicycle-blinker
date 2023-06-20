package com.example.webacrivity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Button;
import android.util.Log;
import android.view.View;
import com.example.webacrivity.HttpGetTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
//    private TextView mReturnTextView;
//    private EditText mEditTextName;
//
//    private TextView locationText;
    private LocationManager locationManager;

    // Button
    private Button mGetRoutesButton;

    // EditText
    private EditText mDestinationEditText;

    // TextView
    private TextView mRoutesTextView;
    private TextView mOriginLatitudeTextView;
    private TextView mOriginLongitudeTextView;
    private TextView mDestinationLatitudeTextView;
    private TextView mDestinationLongitudeTextView;

    // MainActivity
    private MainActivity activity = this;

    // JSONArray
    private JSONArray steps = new JSONArray();

    // idx
    private int idx = 0;

    private int cnt = 0;

    private boolean startFlag = true;

    private static final int PERMISSIONS_REQUEST = 1;

    private TextView mDestinationTextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Button
        Button mGetRoutesButton = (Button) findViewById(R.id.button_get_routes);
        mGetRoutesButton.setOnClickListener(this);

        // EditText
        mDestinationEditText = (EditText) findViewById(R.id.edit_text_destination);

        // TextView
        mRoutesTextView = (TextView) findViewById(R.id.text_view_routes);
        mOriginLatitudeTextView = (TextView) findViewById(R.id.text_view_origin_latitude);
        mOriginLongitudeTextView = (TextView) findViewById(R.id.text_view_origin_longitude);
        mDestinationLatitudeTextView = (TextView) findViewById(R.id.text_view_destination_latitude);
        mDestinationLongitudeTextView = (TextView) findViewById(R.id.text_view_destination_longitude);

    }

    @Override
    public void onClick(View view) {
        Log.d("WebActivity", "wlog onClick()");
        getLocation();
//        if(startFlag) {
//            System.out.println(100);
//            HttpGetTask task = new HttpGetTask(activity, mRoutesTextView, mDestinationEditText, mDestinationLatitudeTextView, mDestinationLongitudeTextView, 36.1111765, 140.1004090);
//            task.execute("Routes", "", "");
//            try {
//                Thread.sleep(5000); // 5秒待つ
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            steps = HttpGetTask.getResult();
//            try {
//                JSONObject step = steps.getJSONObject(idx);
//                System.out.println(step);
//                String instruction = step.getString("html_instructions");
//                System.out.println(instruction);
//                JSONObject end_location = step.getJSONObject("end_location");
//                System.out.println(end_location);
//                double currentDestinationLatitude = end_location.getDouble("lat");
//                double currentDestinationLongitude = end_location.getDouble("lng");
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            System.out.println(steps);
//            startFlag = false;
//        }
    }

    private void getLocation() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST);
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }
    }

    private final LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            mOriginLatitudeTextView.setText("現在地緯度: " + location.getLatitude());
            mOriginLongitudeTextView.setText("現在地経度: " + location.getLongitude());
            if(startFlag) {
                System.out.println(100);
                HttpGetTask task = new HttpGetTask(activity, mRoutesTextView, mDestinationEditText, mDestinationLatitudeTextView, mDestinationLongitudeTextView, location.getLatitude(), location.getLongitude());
                task.execute("Routes", "", "");
                try {
                    Thread.sleep(5000); // 5秒待つ
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                steps = HttpGetTask.getResult();
                startFlag = false;
            }else {
                if(cnt % 5 != 0) return;
                steps = HttpGetTask.getResult();
                System.out.println(200);
                System.out.println(steps);
                try {
                    JSONObject step = steps.getJSONObject(idx);
                    System.out.println(300);
                    String instruction = step.getString("html_instructions");
                    System.out.println(400);
                    JSONObject end_location = step.getJSONObject("end_location");
                    System.out.println(500);
                    double currentDestinationLatitude = end_location.getDouble("lat");
                    double currentDestinationLongitude = end_location.getDouble("lng");
                    Location currentDestinationLocation = new Location("dummyprovider");
                    currentDestinationLocation.setLatitude(currentDestinationLatitude);
                    currentDestinationLocation.setLongitude(currentDestinationLongitude);
                    double distanceToCurrentDestination = location.distanceTo(currentDestinationLocation);
                    Integer lr = convertAndCheckDirection(instruction);
                    mRoutesTextView.setText(String.format("%s\n%s", instruction, Double.toString(distanceToCurrentDestination)));
                    HttpGetTask task = new HttpGetTask(activity, mRoutesTextView, mDestinationEditText, mDestinationLatitudeTextView, mDestinationLongitudeTextView, location.getLatitude(), location.getLongitude());
                    task.execute("RaspberryPi", Integer.toString(lr), Double.toString(distanceToCurrentDestination));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

//            this.mRoutesTextView.setText(HttpGetTask.getResult());

//
        }


        public Integer convertAndCheckDirection(String unicodeEscapedString) {
            // UTF-8文字列に変換
            String utf8String = unescapeUnicode(unicodeEscapedString);

            // HTMLタグを削除
            String text = removeHtmlTags(utf8String);

            // "右"が含まれる場合は1を返す
            if (text.contains("右")) {
                return 1;
            }

            // "左"が含まれる場合は0を返す
            if (text.contains("左")) {
                return 0;
            }

            // "右"も"左"も含まれない場合はnullを返す
            return 1;
        }

        public String unescapeUnicode(String str) {
            StringBuilder utf8String = new StringBuilder();
            Matcher matcher = Pattern.compile("\\\\u([0-9a-fA-F]{4})").matcher(str);
            while (matcher.find()) {
                int codePoint = Integer.parseInt(matcher.group(1), 16);
                utf8String.append((char) codePoint);
            }
            return utf8String.toString();
        }

        public String removeHtmlTags(String htmlString) {
            // HTMLタグを検索する正規表現パターン
            Pattern htmlTagPattern = Pattern.compile("<[^>]*>");

            // 正規表現でマッチする部分を空の文字列に置き換え
            Matcher matcher = htmlTagPattern.matcher(htmlString);
            String text = matcher.replaceAll("");

            return text;
        }

//
        public void onStatusChanged(String provider, int status, Bundle extras) { }

        public void onProviderEnabled(String provider) { }

        public void onProviderDisabled(String provider) { }
    };


}