package com.harry.mytranslation;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.AsyncTask;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class TestActivity extends AppCompatActivity implements OnClickListener{

    private TextView mTextView, mTextView1;
    private EditText mEditText;
    private Button mButton;
    private ImageView mImageView,mClearView,mVoiceView;

    private String a,translation, explains;
    private final int SUCCESS = 1;
    private final int FAILURE = 0;
    private final int ERRORCODE = 2;
    private static final String TAG="TestActivity";
    private static final String REG = "[^\u4e00-\u9fa5]";

    private Bitmap baseBitmap;
    private Canvas canvas;
    private Paint paint;
    private Paint draw;
    private static int mNumberOfSides=0;
    private WindowManager mWindowManager;
    int b=1,width=0;


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
        mButton.setOnClickListener(this);
        mImageView=(ImageView)findViewById(R.id.word_view);
        mClearView=(ImageView)findViewById(R.id.clear_view);
        mClearView.setOnClickListener(this);
        mVoiceView=(ImageView)findViewById(R.id.voice_view);
        mVoiceView.setOnClickListener(this);
        mTextView1=(TextView)findViewById(R.id.test_explains);

        init();
        write();
        clear();
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

        translation = object.optString("translation");
        mTextView.setText(translation);
        if (string.indexOf("basic")!=-1){
            explains = object.optString("basic");
            explains = explains.substring(explains.indexOf("[")+1,explains.indexOf("]"));
            String[] b = explains.split(",");
            explains = null;
            for (int i = 0; i <b.length ; i++) {
                b[i] = b[i].replace("\"","");
                if (i==0){
                    explains = b[i]+"\n";
                }
                else {
                    explains = explains + b[i] + "\n";
                }
            }
            mTextView1.setText(explains);
        }
        else {
            mTextView1.setText("");
        }
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
                            String path = "http://openapi.youdao.com/api?q="+a
                                    +"&from=EN&to=zh_CHS&appKey=356f6228bdfc72cd&salt=2&sign="
                                    +md5("356f6228bdfc72cd"+a+"2z5gPhXjj83g9tifEc0H5ZvhkOI3KUl7m");

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
            case R.id.clear_view:
                clear();
                break;
            case R.id.voice_view:
                TTSUtils.getInstance().speak(translation.replace(REG,""));
                break;

            default:
                break;
        }
    }

    private void init(){
        //mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        //width = mWindowManager.getDefaultDisplay().getWidth();
        baseBitmap = Bitmap.createBitmap(1080, 1560, Bitmap.Config.ARGB_8888);
        // 创建一张画布
        canvas = new Canvas(baseBitmap);
        //画布背景为白色
        canvas.drawColor(Color.WHITE);
        //canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);//绘制透明色
        canvas.translate(500,500);
        // 创建画笔
        paint = new Paint();
        draw = new Paint();
        // 画笔颜色为黑色
        paint.setColor(Color.BLACK);
        draw.setColor(Color.WHITE);
        //画笔的形状
        paint.setStrokeCap(Paint.Cap.SQUARE);
        draw.setStrokeCap(Paint.Cap.SQUARE);
        paint.setTextSize(30);
        // 宽度
        paint.setStrokeWidth(30);
        draw.setStrokeWidth(40);
        // 先将白色背景画上
        canvas.drawBitmap(baseBitmap, new Matrix(), paint);
        mImageView.setImageBitmap(baseBitmap);
    }

    private void write(){
        //触屏
        mImageView.setOnTouchListener(new View.OnTouchListener() {
            int startX;
            int startY;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // 获取手按下时的坐标
                        startX = (int) event.getX();
                        startY = (int) event.getY();
                        //canvas.drawLine(20,20,300,20,draw);
                        //canvas.drawText("笔划起点：" + startX + "," + startY, 20, 30, paint);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        // 获取手移动后的坐标
                        int stopX = (int) event.getX();
                        int stopY = (int) event.getY();
                        //canvas.drawLine(20, 70, 300, 70, draw);
                        //canvas.drawText("笔划终点：" + startX + "," + startY, 20, 80, paint);
                        // 在开始和结束坐标间画一条线
                        canvas.drawLine(startX, startY, stopX, stopY, paint);
                        // 实时更新开始坐标
                        startX = (int) event.getX();
                        startY = (int) event.getY();
                        mImageView.setImageBitmap(baseBitmap);
                        break;
                }
                return true;
            }
        });
    }

    private void clear(){
        Paint p = new Paint();
        //清屏
        p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        canvas = new Canvas(baseBitmap);
        canvas.translate(500,500);
        canvas.drawColor(Color.WHITE);
        p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
        mImageView.setImageResource(0);
        mImageView.setImageBitmap(baseBitmap);
        b =1;
        //
        mNumberOfSides=0;
        paint.setColor(Color.BLACK);
    }

    public static String md5(String string) {
        if(string == null){
            return null;
        }
        char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'A', 'B', 'C', 'D', 'E', 'F'};

        try{
            byte[] btInput = string.getBytes("utf-8");
            /** 获得MD5摘要算法的 MessageDigest 对象 */
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            /** 使用指定的字节更新摘要 */
            mdInst.update(btInput);
            /** 获得密文 */
            byte[] md = mdInst.digest();
            /** 把密文转换成十六进制的字符串形式 */
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (byte byte0 : md) {
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str);
        }catch(NoSuchAlgorithmException | UnsupportedEncodingException e){
            return null;
        }
    }


}

