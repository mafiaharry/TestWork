package com.harry.mytranslation;

import android.net.Uri;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by ha on 2018/4/15.
 */

public class FlickrFechr {

    private static final String TAG="FlickrFetchr";
    private static final String API_KEY="522071532";
    private String jsonString;

    public byte[] getUrlBytes(String urlSpec) throws IOException {
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException(connection.getResponseMessage() +
                        ": with " +
                        urlSpec);
            }
            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while ((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            return out.toByteArray();
        } finally {
            connection.disconnect();
        }
    }
    public String getUrlString(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }

    public String fetchItems(String word){
        try{
            String url= Uri.parse("http://fanyi.youdao.com/openapi.do")
                    .buildUpon()
                    .appendQueryParameter("keyfrom","fadabvaa")
                    .appendQueryParameter("key",API_KEY)
                    .appendQueryParameter("type","data")
                    .appendQueryParameter("doctype","json")
                    .appendQueryParameter("version","1.1")
                    .appendQueryParameter("q",word)
                    .build().toString();
            jsonString=getUrlString(url);

            Log.i(TAG,"Received JSON: "+jsonString);
        }catch (IOException ioe){
            Log.e(TAG,"Failed to fetch items",ioe);
        }
        return jsonString;
    }

}
