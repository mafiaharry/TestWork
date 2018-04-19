package com.harry.mytranslation;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

public class TestActivity extends AppCompatActivity {

    private TextView mTextView;
    private EditText mEditText;
    private String a;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        new FetchItemsTask().execute();

        mTextView=(TextView)findViewById(R.id.test_read);
        mEditText=(EditText)findViewById(R.id.test_write);
        a=mEditText.getText().toString();
        Log.i("TestActivity","ssssss: "+a);
    }


    public class FetchItemsTask extends AsyncTask<Void,Void,Void> {

        @Override
        protected Void doInBackground(Void... params) {
//            try {
//                String result =new FlickrFechr()
//                        .getUrlString("https://www.bignerdranch.com");
//                Log.i(TAG,"Fetched contents of URL: "+result);
//            }catch (IOException ioe){
//                Log.e(TAG,"Failed to fetch URL: ",ioe);
//            }

            new FlickrFechr().fetchItems();
            return null;
        }

    }
}
