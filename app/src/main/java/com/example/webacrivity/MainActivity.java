package com.example.webacrivity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Button;
import android.util.Log;
import android.view.View;
import com.example.webacrivity.HttpGetTask;



public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView mReturnTextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mReturnTextView = (TextView) findViewById(R.id.text_view_return);
        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        Log.d("WebActivity", "wlog onClick()");
        if (view.getId() == R.id.button) {
            HttpGetTask task = new HttpGetTask(this, mReturnTextView);
            task.execute();
        }
    }


}