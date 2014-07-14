package com.net.listener;

/**
 * 自动解析json为TO,并在 {@link com.cyou.cmall.net.listener.AbNetClientTOListener#onSuccess(Object, String[])} 中返回
 * Created by dupengtao on 2014/6/16.
 */
public abstract class DefaultTOListener<T> extends AbNetClientTOListener<T> {

    public DefaultTOListener(Class<T> clazz) {
        super(clazz);
    }

    @Override
    public void onFailure(Throwable e, String content) {

    }

    @Override
    public void onStart() {

    }

    @Override
    public void onFinish() {

    }

    @Override
    public void onNotNetwork() {

    }
}
