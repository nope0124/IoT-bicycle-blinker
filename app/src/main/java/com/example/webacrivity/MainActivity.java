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

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
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
    private MainActivity mActivity = this;

    // JSONArray
    private JSONArray mSteps = new JSONArray();

    // int
    private int mRouteIndex = 0;
    private int mLocationChangedCount = 0;
    private final int mPerCount = 2; // 1分に30回実行

    private final int mNextDestinationBorder = 10;

    private boolean mStartFlag = true;

    private static final int PERMISSIONS_REQUEST = 1;

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
//        mOriginLatitudeTextView = (TextView) findViewById(R.id.text_view_origin_latitude);
//        mOriginLongitudeTextView = (TextView) findViewById(R.id.text_view_origin_longitude);
//        mDestinationLatitudeTextView = (TextView) findViewById(R.id.text_view_destination_latitude);
//        mDestinationLongitudeTextView = (TextView) findViewById(R.id.text_view_destination_longitude);

    }

    @Override
    public void onClick(View view) {
        Log.d("WebActivity", "wlog onClick()");
        getLocation();
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
            mLocationChangedCount++;
//            mOriginLatitudeTextView.setText("現在地緯度: " + location.getLatitude());
//            mOriginLongitudeTextView.setText("現在地経度: " + location.getLongitude());

            // １回目だけ、Google Directions APIを叩く
            if(mStartFlag == true) {
                mStartFlag = false;

                // Google Directions APIを叩き、目的地の緯度経度、までの経路情報を取得
                HttpGetTask task = new HttpGetTask(mActivity, mRoutesTextView, mDestinationEditText, mDestinationLatitudeTextView, mDestinationLongitudeTextView, location.getLatitude(), location.getLongitude());
                task.execute("Routes", "", "");

                // 直後のHttpGetTask.getResult()が失敗するため、5秒待つ
                try {
                    Thread.sleep(5000); // 5秒待つ
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // 経路情報を取得
                mSteps = HttpGetTask.getResult();
            }else if(mStartFlag == false && mLocationChangedCount % mPerCount == 0) {
                if(mRouteIndex == mSteps.length()) return;
                try {
                    JSONObject step = mSteps.getJSONObject(mRouteIndex);

                    // 経路説明
                    String instruction = step.getString("html_instructions");

                    // 一時終点までの緯度経度
                    JSONObject end_location = step.getJSONObject("end_location");
                    double currentDestinationLatitude = end_location.getDouble("lat");
                    double currentDestinationLongitude = end_location.getDouble("lng");

                    // 一時終点までの距離を求めるために一旦定義
                    Location currentDestinationLocation = new Location("dummyprovider");
                    currentDestinationLocation.setLatitude(currentDestinationLatitude);
                    currentDestinationLocation.setLongitude(currentDestinationLongitude);

                    // 一時終点までの距離
                    double distanceToCurrentDestination = location.distanceTo(currentDestinationLocation);

                    // このタイミングで経路説明と、一時終点までの距離を描画
                    String text = "";
                    for(int i = mRouteIndex; i < mSteps.length(); i++) {
                        JSONObject tmpStep = mSteps.getJSONObject(i);
                        String tmpInstruction = tmpStep.getString("html_instructions");
                        text += Integer.toString(i + 1);
                        text += "．";
                        text += removeTags(tmpInstruction);
                        text += "\n";
                        if(i == mRouteIndex) {
                            if(i == mSteps.length() - 1) text += "目的地まで　あと";
                            else text += "次の中継点まで　あと";
                            text += Integer.toString((int)distanceToCurrentDestination);
                            text += "m";
                            text += "\n";
                            text += "\n";
                        }
                    }
                    mRoutesTextView.setText(text);

                    // RaspberryPiに渡す引数
                    int lr = convertAndCheckDirection(instruction);
                    int length = (int)distanceToCurrentDestination;

                    // RaspberryPiを呼び出す
//                    HttpGetTask task = new HttpGetTask(mActivity, mRoutesTextView, mDestinationEditText, mDestinationLatitudeTextView, mDestinationLongitudeTextView, location.getLatitude(), location.getLongitude());
//                    task.execute("RaspberryPi", Integer.toString(lr), Integer.toString(length));

                    if(length <= mNextDestinationBorder) {
                        mRouteIndex++;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        public int convertAndCheckDirection(String unicodeEscapedString) {
            // UTF-8文字列に変換
            String utf8String = unescapeUnicode(unicodeEscapedString);

            // HTMLタグを削除
            String text = removeHtmlTags(utf8String);

            // "右"が含まれる場合は1を返す
            if (text.contains("右")) return 1;

            // "左"が含まれる場合は0を返す
            if (text.contains("左")) return 0;

            // "右"も"左"も含まれない場合は1を返す
            return 1;
        }

        public String removeTags(String unicodeEscapedString) {
            // HTMLタグを削除
            unicodeEscapedString = unicodeEscapedString.replaceAll("使用が制限されている道路", "　");
            return unicodeEscapedString.replaceAll("<[^>]+>", "");
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

        public void onStatusChanged(String provider, int status, Bundle extras) { }

        public void onProviderEnabled(String provider) { }

        public void onProviderDisabled(String provider) { }
    };


}