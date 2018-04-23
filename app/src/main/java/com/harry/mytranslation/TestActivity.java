package com.harry.mytranslation;

import android.os.AsyncTask;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class TestActivity extends AppCompatActivity implements OnClickListener{

    private TextView mTextView;
    private EditText mEditText;
    private Button mButton;
    private String a, b;
    private final int SUCCESS = 1;
    private final int FAILURE = 0;
    private final int ERRORCODE = 2;
    private static final String TAG="TestActivity";


    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SUCCESS:
                    /**
                     * 获取信息成功后，对该信息进行JSON解析，得到所需要的信息，然后在textView上展示出来。
                     */
                    JSONAnalysis(msg.obj.toString());
                    Toast.makeText(TestActivity.this, "获取数据成功", Toast.LENGTH_SHORT)
                            .show();
                    break;

                case FAILURE:
                    Toast.makeText(TestActivity.this, "获取数据失败", Toast.LENGTH_SHORT)
                            .show();
                    break;

                case ERRORCODE:
                    Toast.makeText(TestActivity.this, "获取的CODE码不为200！",
                            Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }

        ;
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        //new FetchItemsTask().execute();

        mTextView = (TextView) findViewById(R.id.test_read);
        mEditText = (EditText) findViewById(R.id.test_write);
        mButton = (Button) findViewById(R.id.go_button);
        //chao();
        mButton.setOnClickListener(this);
    }

    protected void JSONAnalysis(String string) {
        JSONObject object = null;
        try {
            object = new JSONObject(string);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        /**
         * 在你获取的string这个JSON对象中，提取你所需要的信息。
         */
        //JSONObject ObjectInfo = object.optJSONObject();
        String city = object.optString("translation");
        mTextView.setText(city);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.go_button:

                new Thread() {
                    public void run() {
                        int code;
                        try {
                            a = mEditText.getText().toString();
                            Log.i("TestActivity", "ssssss: " + a);
                            //b=new FlickrFechr().fetchItems(a);
                            //mTextView.setText(b);
                            String path = "http://fanyi.youdao.com/openapi.do?keyfrom=fadabvaa&key=522071532&type=data&doctype=json&version=1.1&q=" + a;
                            URL url = new URL(path);
                            /**
                             * 这里网络请求使用的是类HttpURLConnection，另外一种可以选择使用类HttpClient。
                             */
                            HttpURLConnection conn = (HttpURLConnection) url
                                    .openConnection();
                            conn.setRequestMethod("GET");//使用GET方法获取
                            conn.setConnectTimeout(5000);
                            code = conn.getResponseCode();
                            if (code == 200) {
                                /**
                                 * 如果获取的code为200，则证明数据获取是正确的。
                                 */
                                InputStream is = conn.getInputStream();
                                String result = HttpUtils.readMyInputStream(is);
                                Log.i(TAG,"Received JSON: "+result);

                                /**
                                 * 子线程发送消息到主线程，并将获取的结果带到主线程，让主线程来更新UI。
                                 */
                                Message msg = new Message();
                                msg.obj = result;
                                msg.what = SUCCESS;
                                handler.sendMessage(msg);

                            } else {

                                Message msg = new Message();
                                msg.what = ERRORCODE;
                                handler.sendMessage(msg);
                            }
                        } catch (Exception e) {

                            e.printStackTrace();
                            /**
                             * 如果获取失败，或出现异常，那么子线程发送失败的消息（FAILURE）到主线程，主线程显示Toast，来告诉使用者，数据获取是失败。
                             */
                            Message msg = new Message();
                            msg.what = FAILURE;
                            handler.sendMessage(msg);
                        }
                    };
                }.start();
                break;

            default:
                break;
        }
    }
}

//    public class FetchItemsTask extends AsyncTask<Void,Void,Void> {
//
//        @Override
//        protected Void doInBackground(Void... params) {
////            try {
////                String result =new FlickrFechr()
////                        .getUrlString("https://www.bignerdranch.com");
////                Log.i(TAG,"Fetched contents of URL: "+result);
////            }catch (IOException ioe){
////                Log.e(TAG,"Failed to fetch URL: ",ioe);
////            }
//
//            new FlickrFechr().fetchItems();
//            return null;
//        }
//
//    }

