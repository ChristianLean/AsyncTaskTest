package com.example.administrator.asynctasktesk;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.LruCache;
import android.widget.ImageView;
import android.widget.ListView;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Administrator on 2017/4/10.
 */

public class ImageLoader {

    private ImageView mImageView;
    private String mUrl;
    private Bitmap bitmap;
    //创建Cache
    private LruCache<String, Bitmap> mCache;
    private ListView mListView;
    private Set<NewsAsyncTask> mTask; //创建集合管理所有的AsyncTask

    /**
     * 调用ImageLoader方法时获取内存，存入缓存
     */
    public ImageLoader(ListView listview) {
        mListView = listview;
        mTask = new HashSet<>();
        //获取可用最大内存
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        int cacheSize = maxMemory / 4;
        mCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                //在每次存入缓存的时候调用
                return value.getByteCount();

            }
        };
    }

    /**
     * 将Bitmap添加到缓存中
     *
     * @param url
     * @param bitmap
     */
    public void addBitmapToCache(String url, Bitmap bitmap) {
        if (getBitmapFromCache(url) == null) {
            mCache.put(url, bitmap);
        }
    }

    /**
     * 从缓存中获得Bitmap
     *
     * @param url
     * @return
     */
    public Bitmap getBitmapFromCache(String url) {
        return mCache.get(url);
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mImageView.getTag().equals(mUrl)) {
                mImageView.setImageBitmap((Bitmap) msg.obj);
            }

        }
    };

    /**
     * 1.
     * 使用线程进行加载图片操作
     *
     * @param imageView
     * @param url
     */
    public void showImageByThread(ImageView imageView, final String url) {
        mImageView = imageView;
        mUrl = url;
        new Thread() {
            @Override
            public void run() {
                super.run();
                Bitmap bitmap = getBitmapFormUrl(url);
                Message message = Message.obtain();
                message.obj = bitmap;
                mHandler.sendMessage(message);

            }
        }.start();

    }

    /**
     * 从NewsAdapter中传进的url中得到Bitmap对象
     * 无论是使用线程还是AsyncTask进行加载图片操作都必须先将url转化为bitmap然后再传入具体实现实现方法中
     *
     * @param urlString
     * @return
     */
    public Bitmap getBitmapFormUrl(String urlString) {
        Bitmap bitmap;
        InputStream is = null;
        try {
            URL url = new URL(urlString); //获得url
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();//赋值HttpURLConnection
            is = new BufferedInputStream(connection.getInputStream());
            bitmap = BitmapFactory.decodeStream(is);//inputStream
            connection.disconnect();
            //Thread.sleep(1000); 线程延迟1秒

            return bitmap;
        } catch (java.io.IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 2.
     * <p>
     * 使用AsyncTask进行加载图片操作
     * 相比较线程操作更简单一点，省略了从子线程中发送消息到主线程进行UI操作的步骤
     *
     * @param imageView
     * @param url
     */

    public void showImageByAsyncTask(ImageView imageView, String url) {

        //从缓存中取出图片
        bitmap = getBitmapFromCache(url);

        //如果缓存中没有，则开启线程下载图片==>doInBackground()
        if (bitmap == null) {
           /* new NewsAsyncTask(url).execute(url);*/
            imageView.setImageResource(R.mipmap.ic_launcher);
        } else {
            //否则将图片直接加载到imageView中
            imageView.setImageBitmap(bitmap);
        }

    }

    /**
     * 用来加载从start到end的所有图片
     * @param start
     * @param end
     */
    public void loadImages(int start, int end) {
        for (int i =start; i < end; i++) {
            String url = NewsAdapter.URLS[i];
            bitmap = getBitmapFromCache(url);

            //如果缓存中没有，则开启线程下载图片==>doInBackground()
            if (bitmap == null) {
                NewsAsyncTask task = new NewsAsyncTask(url);
                task.execute(url);
                mTask.add(task);
            } else {
                //否则将图片直接加载到imageView中
                ImageView imageView = (ImageView) mListView.findViewWithTag(url);
                imageView.setImageBitmap(bitmap);
            }
        }
    }

    public void cancelAllTask(){
        if (mTask !=null){
            for (NewsAsyncTask task : mTask){
                task.cancel(false); //取消任务
            }
        }
    }

    private class NewsAsyncTask extends AsyncTask<String, Void, Bitmap> {

        //        private ImageView mImageView;
        private String mUrl;

        public NewsAsyncTask(String url) {
//            mImageView = imageView;
            mUrl = url;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            //从网络中获取图片
            bitmap = getBitmapFormUrl(params[0]);//传入第一个参数
            if (bitmap != null) {
                //将不在缓存中的图片加入缓存
                addBitmapToCache(params[0], bitmap);
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            ImageView imageView = (ImageView) mListView.findViewWithTag(mUrl);
            if (imageView != null && bitmap != null) {
                imageView.setImageBitmap(bitmap);
            }
            mTask.remove(this);
        }
    }

}
