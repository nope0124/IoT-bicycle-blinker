package com.example.webactivity;

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


public class HttpGetTask extends AsyncTask <String, Void, String> {
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

    private final String GEOCODING_API = "https://maps.googleapis.com/maps/api/geocode";
    private final String DIRECTIONS_API = "https://maps.googleapis.com/maps/api/directions";

    private static JSONArray sResult = new JSONArray();
    public static JSONArray getResult() {
        return sResult;
    }


    private final String API_KEY = "";
    public HttpGetTask(Activity parentActivity, TextView routesTextView, EditText destinationEditText, TextView destinationLatitudeTextView, TextView destinationLongitudeTextView, double originLatitude, double originLongitude) {
        // Activity
        this.mParentActivity = parentActivity;

        // TextView
        this.mRoutesTextView = routesTextView;
        this.mDestinationLatitudeTextView = destinationLatitudeTextView;
        this.mDestinationLongitudeTextView = destinationLongitudeTextView;

        // EditText
        this.mDestinationEditText = destinationEditText;

        // 現在地情報
        this.mOriginLatitude = originLatitude;
        this.mOriginLongitude = originLongitude;
    }

    //タスク開始時
    @Override
    protected void onPreExecute() {
//        mDialog = new ProgressDialog(this.mParentActivity);
//        mDialog.setMessage("");
//        mDialog.show();
    }

    //メイン処理
    @Override
    protected String doInBackground(String... params) {
        String param = params[0];
        if(param == "Routes") {
            // 目的地の緯度経度を取得
            double[] destinationLatitudeLongitude = getLatitudeLongitudeFromGeocodingAPI(this.mDestinationEditText.getText().toString());
            this.mDestinationLatitude = destinationLatitudeLongitude[0];
            this.mDestinationLongitude = destinationLatitudeLongitude[1];

            JSONArray data = getRoutesFromDirectionsAPI(this.mDestinationLatitude, this.mDestinationLongitude);

            sResult = data;
        }else if(param == "RaspberryPi") {
            String lr = params[1];
            String length = params[2];
            String uri = String.format("http://172.20.10.8/~pi/navi2.php?lr=%s&length=%s", lr, length);
            System.out.println(uri);

            HttpURLConnection http = null;
            InputStream in = null;
            String src = new String();

            try {
                URL url = new URL(uri);
                http = (HttpURLConnection) url.openConnection();
                http.setRequestMethod("GET");
                http.connect();
                in = http.getInputStream();
                byte[] line = new byte[1024];
                int size;
                while (true) {
                    size = in.read(line);
                    if (size <= 0) break;
                    src += new String(line);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (http != null) http.disconnect();
                    if (in != null) in.close();
                } catch (Exception e) {
                }
            }
        }
        return "";
    }

    //タスク終了時
    @Override
    protected void onPostExecute (String string) {
//        mDialog.dismiss();
//        this.mDestinationLatitudeTextView.setText("目的地緯度: " + Double.toString(this.mDestinationLatitude));
//        this.mDestinationLongitudeTextView.setText("目的地経度: " + Double.toString(this.mDestinationLongitude));
    }


    private JSONArray getRoutesFromDirectionsAPI(double destinationLatitude, double destinationLongitude) {
        String uri = String.format("%s/json?language=ja&origin=%.7f,%.7f&destination=%.7f,%.7f&key=%s", DIRECTIONS_API, mOriginLatitude, mOriginLongitude, destinationLatitude, destinationLongitude, API_KEY);

        JSONArray src = new JSONArray();

        JSONObject data = getDataFromUri(uri);
        try {
            // 経路のステップを取得
            JSONArray routes = data.getJSONArray("routes");
            JSONObject route = routes.getJSONObject(0);
            JSONArray legs = route.getJSONArray("legs");
            JSONObject leg = legs.getJSONObject(0);
            JSONArray steps = leg.getJSONArray("steps");

            src = steps;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return src;
    }

    public double[] getLatitudeLongitudeFromGeocodingAPI(String destination) {
        String uri = String.format("%s/json?address=%s&key=%s", GEOCODING_API, destination, API_KEY);

        // 初期化
        double latitude = 0.0;
        double longitude = 0.0;

        JSONObject data = getDataFromUri(uri);

        try {
            // 経路のステップを取得
            JSONArray results = data.getJSONArray("results");
            JSONObject result = results.getJSONObject(0);
            JSONObject geometry = result.getJSONObject("geometry");
            JSONObject location = geometry.getJSONObject("location");

            // 緯度経度を取得
            latitude  = location.getDouble("lat");
            longitude = location.getDouble("lng");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new double[] { latitude, longitude };
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
}

