package com.example.webacrivity;

import android.location.LocationManager;
import android.widget.EditText;
import android.widget.TextView;
import android.os.AsyncTask;
import android.app.ProgressDialog;
import android.app.Activity;
import java.net.HttpURLConnection;
import java.io.InputStream;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.json.JSONArray;
import org.json.JSONObject;


public class HttpGetTask extends AsyncTask <Void, Void, String> {
    private TextView mRoutesTextView;
    private TextView mDestinationLatitudeTextView;
    private TextView mDestinationLongitudeTextView;
    private Activity mParentActivity;
    private EditText mDestinationEditText;
    private double mOriginLatitude = 0.0;
    private double mOriginLongitude = 0.0;
    private double mDestinationLatitude = 0.0;
    private double mDestinationLongitude = 0.0;
    private ProgressDialog mDialog = null;


    private String API_KEY = "";
    public HttpGetTask(Activity parentActivity, TextView routesTextView, EditText destinationEditText, TextView destinationLatitudeTextView, TextView destinationLongitudeTextView, double originLatitude, double originLongitude) {
        this.mParentActivity = parentActivity;
        this.mRoutesTextView = routesTextView;
        this.mDestinationEditText = destinationEditText;
        this.mDestinationLatitudeTextView = destinationLatitudeTextView;
        this.mDestinationLongitudeTextView = destinationLongitudeTextView;
        this.mOriginLatitude = originLatitude;
        this.mOriginLongitude = originLongitude;
        System.out.println("origin_latitude");
        System.out.println(this.mOriginLatitude);
        System.out.println("origin_longitude");
        System.out.println(this.mOriginLongitude);

    }
    //タスク開始時
    @Override
    protected void onPreExecute() {
        mDialog = new ProgressDialog(this.mParentActivity);
        mDialog.setMessage("");
        mDialog.show();
    }
    //メイン処理
    @Override
    protected String doInBackground(Void... voids) {
        // 目的地の緯度経度を取得
        double[] destinationLatitudeLongitude = getLatitudeLongitude(this.mDestinationEditText.getText().toString());
        this.mDestinationLatitude = destinationLatitudeLongitude[0];
        this.mDestinationLongitude = destinationLatitudeLongitude[1];



        System.out.println("destination_latitude");
        System.out.println(this.mDestinationLatitude);
        System.out.println("destination_longitude");
        System.out.println(this.mDestinationLongitude);

        String res = getRoutes(this.mDestinationLatitude, this.mDestinationLongitude);
        return res;
    }
    //タスク終了時
    @Override
    protected void onPostExecute (String string) {
        mDialog.dismiss();
        this.mDestinationLatitudeTextView.setText("目的地緯度: " + Double.toString(this.mDestinationLatitude));
        this.mDestinationLongitudeTextView.setText("目的地経度: " + Double.toString(this.mDestinationLongitude));
        this.mRoutesTextView.setText(string);
    }

    public JSONObject getDataFromUri(String uri) {
        HttpURLConnection http = null;
        BufferedReader reader = null;
        InputStream in = null;

        // JSONデータをパース
        JSONObject data = new JSONObject();

        try {
            URL url = new URL(uri);
            http = (HttpURLConnection)url.openConnection();
            http.setRequestMethod("GET");
            http.connect();

            // レスポンスのステータスコードを確認
            int statusCode = http.getResponseCode();
            if (statusCode == HttpURLConnection.HTTP_OK) {
                // レスポンスの入力ストリームを取得
                reader = new BufferedReader(new InputStreamReader(http.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                // レスポンスの内容を読み取り
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                // JSONデータをパース
                data = new JSONObject(response.toString());

            } else {
                System.out.println("APIリクエストが失敗しました。ステータスコード: " + statusCode);

            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (Exception ignored) {
            }
        }
        return data;
    }


    private String getRoutes(double destination_latitude, double destination_longitude) {
        String uri = String.format("https://maps.googleapis.com/maps/api/directions/json?mode=bicycling&origin=%.7f,%.7f&destination=%.7f,%.7f&key=%s", mOriginLatitude, mOriginLongitude, destination_latitude, destination_longitude, API_KEY);
        String src = "";

        JSONObject data = getDataFromUri(uri);
        try {
            // 経路のステップを取得
            JSONArray routes = data.getJSONArray("routes");
            JSONObject route = routes.getJSONObject(0);
            JSONArray legs = route.getJSONArray("legs");
            JSONObject leg = legs.getJSONObject(0);
            JSONArray steps = leg.getJSONArray("steps");

            // 各ステップの指示を表示
            for (int i = 0; i < steps.length(); i++) {
                JSONObject step = steps.getJSONObject(i);
                String instruction = step.getString("html_instructions");
                System.out.println(instruction);
                src += new String(instruction);
                src += new String("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return src;
    }

    public double[] getLatitudeLongitude(String destination) {

        String uri = String.format("https://maps.googleapis.com/maps/api/geocode/json?address=%s&key=%s", destination, API_KEY);

        double latitude = 0.0;
        double longitude = 0.0;

        JSONObject data = getDataFromUri(uri);

        try {
            // 経路のステップを取得
            JSONArray results = data.getJSONArray("results");
            JSONObject result = results.getJSONObject(0);
            JSONObject geometry = result.getJSONObject("geometry");
            JSONObject location = geometry.getJSONObject("location");
            latitude  = location.getDouble("lat");
            longitude = location.getDouble("lng");
        } catch (Exception e) {
            e.printStackTrace();
        }
        String str = Double.toString(latitude) + Double.toString(longitude);
//        return str;
        return new double[] { latitude, longitude };
    }
}

