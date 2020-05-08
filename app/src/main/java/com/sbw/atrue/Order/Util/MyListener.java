package com.sbw.atrue.Order.Util;


import org.json.JSONException;

public interface MyListener<T> {
    void onSuccess(T data) throws JSONException;
}
