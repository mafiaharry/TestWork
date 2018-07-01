package com.harry.mytranslation;

/**
 * Created by ha on 2018/4/24.
 */
import android.app.Application;
import android.content.Context;



public class MyApplication extends Application {

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        TTSUtils.getInstance().init();
    }

    public static Context getContext() {
        return context;
    }
}
