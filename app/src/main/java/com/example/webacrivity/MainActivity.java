package com.example.webacrivity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Button;
import android.util.Log;
import android.view.View;
import com.example.webacrivity.HttpGetTask;



public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView mReturnTextView;
    private EditText mEditTextName;

    private TextView mDestinationTextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mEditTextName = (EditText) findViewById(R.id.edit_text_destination);
        mReturnTextView = (TextView) findViewById(R.id.text_view_html);
        mDestinationTextView = (TextView) findViewById(R.id.text_view_destination);
        Button mHTMLButton = (Button) findViewById(R.id.button_html);
        Button mDestinationButton = (Button) findViewById(R.id.button_destination);
        mHTMLButton.setOnClickListener(this);
//        mDestinationButton.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        Log.d("WebActivity", "wlog onClick()");
        HttpGetTask task = new HttpGetTask(this, mReturnTextView, mEditTextName);

        if (view.getId() == R.id.button_html) {
            task.execute();
        }
    }


}