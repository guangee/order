package com.sbw.atrue.Order.Activity;


import com.yanzhenjie.nohttp.InitializationConfig;
import com.yanzhenjie.nohttp.NoHttp;

import org.litepal.LitePalApplication;

public class MyApplication extends LitePalApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        InitializationConfig config = InitializationConfig.newBuilder(this)
                .connectionTimeout(30 * 1000)
                .readTimeout(30 * 1000)
                .retry(10)
                .build();
        NoHttp.initialize(config);
    }
}
