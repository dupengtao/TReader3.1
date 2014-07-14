package com.net;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;
import com.android.volley.*;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.net.listener.INetClientBaseListener;
import com.net.listener.INetClientJsonListener;
import com.net.listener.INetClientStrListener;
import com.net.utils.LogHelper;
import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * 联网
 * Created by dupengtao on 2014/6/13.
 */
public class NetClient {

    private static final String TAG = NetClient.class.getSimpleName();
    private static volatile NetClient mNetClient;
    private final DefaultRequestFactory mRequestFactory;
    private static VolleyController mVolleyController;

    private NetClient() {
        mRequestFactory = new DefaultRequestFactory();
    }

    public static NetClient getInstance(VolleyController volleyController) {
        if (mNetClient == null) {
            synchronized (NetClient.class) {
                if (mNetClient == null) {
                    mNetClient = new NetClient();
                    mVolleyController = volleyController;
                }
            }
        }
        return mNetClient;
    }

    public void executeRequest(Request request, String tag) {
        mVolleyController.addToRequestQueue(request, tag);
    }

    public void executeRequest(Request request) {
        mVolleyController.addToRequestQueue(request);
    }

    /**
     * @param method 请求方式
     * @param url 地址
     * @param headParams 请求头
     * @param postParams post请求参数
     * @param isShouldCache 是否加入缓存
     * @param tag request标志
     * @param listener 成功的监听
     */
    public void executeRequest(int method, String url, Map<String, String> headParams, Map<String, String> postParams, boolean isShouldCache, String tag, INetClientStrListener listener) {
        StringRequest strRequest = mRequestFactory.produceStrRequest(method, url, headParams, postParams, makeStrListener(listener), makeErrorListener(listener));
        if (isShouldCache) {
            setNoCache(strRequest);
        }
        executeRequest(strRequest, tag);
    }

    /**
     * a simple get string request
     */
    public void executeRequest(String url, INetClientStrListener listener) {
        StringRequest strRequest = mRequestFactory.produceStrRequest(url, makeStrListener(listener), makeErrorListener(listener));
        executeRequest(strRequest);
    }

    /**
     * a simple get string request with headParams
     */
    public void executeRequest(String url, Map<String, String> headParams, INetClientStrListener listener) {
        StringRequest strRequest = mRequestFactory.produceStrRequest(url, headParams, makeStrListener(listener), makeErrorListener(listener));
        executeRequest(strRequest);
    }

    /**
     * a simple post string request
     */
    public void executePostStrRequset(String url, Map<String, String> headParams, Map<String, String> postParams, INetClientStrListener listener) {
        StringRequest strRequest = mRequestFactory.producePostStrRequest(url, headParams, postParams, makeStrListener(listener), makeErrorListener(listener));
        executeRequest(strRequest);
    }

    // json
    public void executeJsonRequest(int method, String url, Map<String, String> headParams, Map<String, String> postParams, boolean isShouldCache, String tag, INetClientJsonListener listener) {
        JsonObjectRequest jsonObjectRequest = mRequestFactory.produceJsonRequest(method, url, headParams, postParams, makeJsonListener(listener), makeErrorListener(listener));
        if (isShouldCache) {
            setNoCache(jsonObjectRequest);
        }
        executeRequest(jsonObjectRequest, tag);
    }

    /**
     * a simple get json request
     */
    public void executeJsonRequest(String url, INetClientJsonListener listener) {
        JsonObjectRequest jsonObjectRequest = mRequestFactory.produceJsonRequest(url, makeJsonListener(listener), makeErrorListener(listener));
        executeRequest(jsonObjectRequest);
    }

    /**
     * a simple get json request with headParams
     */
    public void excuteJsonRequest(String url, Map<String, String> headParams, INetClientJsonListener listener) {
        JsonObjectRequest jsonObjectRequest = mRequestFactory.produceJsonRequest(url, headParams, makeJsonListener(listener), makeErrorListener(listener));
        executeRequest(jsonObjectRequest);
    }

    /**
     * a simple post json request
     */
    public void excutePostJsonRequset(String url, Map<String, String> headParams, Map<String, String> postParams, INetClientJsonListener listener) {
        CustomRequest customRequest = mRequestFactory.producePostJsonRequest(url, headParams, postParams, makeJsonListener(listener), makeErrorListener(listener));
        executeRequest(customRequest);
    }

    /**
     * 设置request不使用缓存
     */
    private static Request setNoCache(Request request) {
        request.setShouldCache(false);
        return request;
    }

    private Response.Listener<String> makeStrListener(final INetClientStrListener strListener) {
        Response.Listener<String> listener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                strListener.onSuccess(response, null);
                strListener.onFinish();
            }
        };
        return listener;
    }

    private Response.Listener<JSONObject> makeJsonListener(final INetClientJsonListener jsonListener) {
        Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                jsonListener.onSuccess(jsonObject, null);
                jsonListener.onFinish();
            }
        };
        return listener;
    }

    private Response.ErrorListener makeErrorListener(final INetClientBaseListener listener) {
        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                processError(error, listener);
            }
        };
        return errorListener;
    }

    private void processError(VolleyError error, INetClientBaseListener listener) {
        String message = "";
        try {
            message = VolleyErrorHelper.getMessage(error);
            if (VolleyErrorHelper.ERROR_NO_INTERNET == message) {
                //没有网络的回调
                listener.onNotNetwork();
            } else {
                if (error.networkResponse != null) {
                    message += "statusCode" + error.networkResponse.statusCode;
                }
                //请求失败的回调
                listener.onFailure(error, message);
            }
            LogHelper.e(TAG, message);
        } catch (Exception e) {
            e.printStackTrace();
            listener.onFailure(error, message);
        } finally {
            //请求完成的回调
            listener.onFinish();
        }
    }

    //image
    public static ImageLoader.ImageContainer loadImage(String url, ImageView imageView, int loadingResId, int errorResId) {
        return loadImage(url, imageView, loadingResId, errorResId, 0, 0);
    }

    public static ImageLoader.ImageContainer loadImage(String url, ImageView imageView, int loadingResId, int errorResId, int maxWidth, int maxHeight) {
        ImageLoader imageLoader = mVolleyController.getImageLoader();
        return imageLoader.get(url, ImageLoader.getImageListener(imageView, loadingResId, errorResId), maxWidth, maxHeight);
    }

    /**
     * if do not use xml anim , you should use{@link com.android.volley.toolbox.ImageLoader#get(String, com.android.volley.toolbox.ImageLoader.ImageListener)}
     * eg.
     *
     * @param loadingResId if loadingResId is 0 ,ImageView will not loading image
     * @param animResId    anim in xml
     */
    public static void loadImageWithAnim(Context context, String url, ImageView imageView, int loadingResId, int errorResId, final int animResId) {
        mVolleyController.getImageLoader().get(url, new AbAnimImageListener(context, imageView, errorResId, loadingResId) {
            @Override
            public int getAnimResId() {
                if (animResId < 1) {
                    return 0;
                }
                return animResId;
            }
        });
    }

    /**
     * @param isShouldCache if false will not in cache
     */
    public static ImageRequest loadImage(String url, final ImageView imageView, int loadingResId, final int errorResId, int maxWidth, int maxHeight, boolean isShouldCache, Bitmap.Config decodeConfig, String tag) {
        ImageRequest imgRequest = getImageRequest(url, imageView, loadingResId, errorResId, maxWidth, maxHeight, isShouldCache, decodeConfig);
        mVolleyController.addToRequestQueue(imgRequest, tag);
        return imgRequest;
    }

    private static ImageRequest getImageRequest(final String url, final ImageView imageView, int loadingResId, final int errorResId, int maxWidth, int maxHeight, final boolean isShouldCache, Bitmap.Config decodeConfig) {
        if (loadingResId > 0) {
            imageView.setImageResource(loadingResId);
        }
        if (maxWidth < 1) {
            maxWidth = 0;
        }
        if (maxHeight < 1) {
            maxHeight = 0;
        }
        final int finalMaxHeight = maxHeight;
        final int finalMaxWidth = maxWidth;
        ImageRequest imgRequest = new ImageRequest(url, new Response.Listener<Bitmap>() {
            @Override
            public void onResponse(Bitmap response) {
                imageView.setImageBitmap(response);
                if (!isShouldCache) {
                    mVolleyController.getLruBitmapCache().putBitmap(getCacheKey(url, finalMaxWidth, finalMaxHeight), response);
                }
            }
        }, maxWidth, maxHeight, decodeConfig, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                imageView.setImageResource(errorResId);
            }
        });
        if (!isShouldCache) {
            setNoCache(imgRequest);
        }
        return imgRequest;
    }

    public static ImageRequest loadImageByRequest(String url, ImageView imageView, boolean isShouldCache, int loadingResId, int errorResId) {
        return loadImage(url, imageView, loadingResId, errorResId, 0, 0, isShouldCache, Bitmap.Config.RGB_565, null);
    }

    public static Bitmap loadImageInCache(String url, int maxWidth, int maxHeight) {
        Cache.Entry entry = mVolleyController.getRequestQueue().getCache().get(getCacheKey(url, maxHeight, maxWidth));
        if (entry.data.length != 0) {
            return BitmapFactory.decodeByteArray(entry.data, 0, entry.data.length);
        } else {
            return null;
        }
    }

    public static String loadInCache(String url) {
        Cache.Entry entry = mVolleyController.getRequestQueue().getCache().get(url);
        String data = null;
        if (entry == null) {
            return data;
        }
        try {
            data = new String(entry.data, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return data;
    }

    public static void cacheRemove(String url) {
        mVolleyController.getRequestQueue().getCache().remove(url);
    }

    public static void cacheClear() {
        mVolleyController.getRequestQueue().getCache().clear();
    }

    public static void cancelSingleRequest(String reqTag) {
        mVolleyController.getRequestQueue().cancelAll(reqTag);
    }

    public static void cancelAllRequests() {
        mVolleyController.getRequestQueue().cancelAll(new RequestQueue.RequestFilter() {
            @Override
            public boolean apply(Request<?> request) {
                return true;
            }
        });
    }

    /**
     * count cache size
     */
    public static long getCacheSize(Context context) {
        File cacheDir = new File(context.getCacheDir(), "volley");
        return cacheDir.length();
    }

    private static String getCacheKey(String url, int maxWidth, int maxHeight) {
        return new StringBuilder(url.length() + 12).append("#W").append(maxWidth)
                .append("#H").append(maxHeight).append(url).toString();
    }

}
