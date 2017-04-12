package com.example.administrator.asynctasktest;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String URL = "http://www.imooc.com/api/teacher?type=4&num=30";
    private ListView lvMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lvMain = (ListView) findViewById(R.id.lv_main);
        new NewsAsyncTask().execute(URL);
    }

    /**
     *
     * @param url
     * @return
     *
     * 将url对应的json格式数据转化为封装的NewsBean
     */
    private List<NewsBean> getJsonData(String url) {
        List<NewsBean> newsBeenList = new ArrayList<>();
        try {
            String jsonString = readStream(new URL(url).openStream()); /* 与url.openConnection().getInputStream()相同，
                                                                        可根据url直接联网获取网络数据，返回值类型为InputStream*/
            JSONObject jsonObject;
            NewsBean newsBean;

            try {
                jsonObject = new JSONObject(jsonString);
                JSONArray jsonArray = jsonObject.getJSONArray("data");
                for (int i = 0; i <jsonArray.length() ; i++) {
                    jsonObject = jsonArray.getJSONObject(i);
                    newsBean = new NewsBean();
                    newsBean.newsIconUrl = jsonObject.getString("picSmall");
                    newsBean.newsContent = jsonObject.getString("description");
                    newsBean.newsTitle = jsonObject.getString("name");

                    newsBeenList.add(newsBean);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }


        } catch (IOException e) {
            e.printStackTrace();
        }

        return newsBeenList;
    }

    //使用InputStream读取网络信息
    private String readStream(InputStream is) {

        InputStreamReader isr;
        String result = "";

        try {
            String line;
            isr = new InputStreamReader(is, "utf-8"); //字节流转化为字符流

            BufferedReader br = new BufferedReader(isr); //读取字符流

            while ((line = br.readLine()) != null) {
                result += line;
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return result;
    }

    /**
     * 实现网络的异步访问
     */

     class NewsAsyncTask extends AsyncTask<String, Void, List<NewsBean>> {

        @Override
        protected List<NewsBean> doInBackground(String... params) {
            return getJsonData(params[0]); //请求网址
        }

        @Override
        protected void onPostExecute(List<NewsBean> newsBeen) {
            super.onPostExecute(newsBeen);
            NewsAdapter adapter = new NewsAdapter(MainActivity.this,newsBeen,lvMain);
            lvMain.setAdapter(adapter);
        }
    }
}
