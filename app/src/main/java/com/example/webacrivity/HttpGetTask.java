package com.example.webacrivity;

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
    private TextView mTextView;
    private Activity mParentActivity;
    private ProgressDialog mDialog = null;
    //実行するphpのURL
    private String mUri = "https://maps.googleapis.com/maps/api/directions/json?origin=Tokyo+Station&destination=Shibuya+Station&key=";
    public HttpGetTask(Activity parentActivity, TextView textView) {
        this.mParentActivity = parentActivity;
        this.mTextView = textView;
    }
    //タスク開始時
    @Override
    protected void onPreExecute() {
        mDialog = new ProgressDialog(mParentActivity);
        mDialog.setMessage("");
        mDialog.show();
    }
    //メイン処理
    @Override
    protected String doInBackground(Void... voids) {
        return exec_get();
    }
    //タスク終了時
    @Override
    protected void onPostExecute (String string) {
        mDialog.dismiss();
        this.mTextView.setText(string);
    }


    private String exec_get() {
        HttpURLConnection http = null;
        BufferedReader reader = null;
        InputStream in = null;
        String src = "";
        try {
            URL url = new URL(mUri);
            http = (HttpURLConnection)url.openConnection();
            http.setRequestMethod("GET");
            http.connect();
//            in = http.getInputStream();
//            byte[] line = new byte[1024];
//            int size;
//            while (true) {
//                size = in.read(line);
//                if (size <= 0) {
//                    break;
//                }
//                src += new String(line);
//            }
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
                JSONObject data = new JSONObject(response.toString());

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
            return src;
        }
    }
}

