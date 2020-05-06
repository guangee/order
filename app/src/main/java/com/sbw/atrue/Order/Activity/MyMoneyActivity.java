package com.sbw.atrue.Order.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.sbw.atrue.Order.R;
import com.sbw.atrue.Order.Util.HttpUtil;
import com.sbw.atrue.Order.Util.ShareUtils;
import com.yanzhenjie.nohttp.NoHttp;
import com.yanzhenjie.nohttp.RequestMethod;
import com.yanzhenjie.nohttp.rest.OnResponseListener;
import com.yanzhenjie.nohttp.rest.Request;
import com.yanzhenjie.nohttp.rest.RequestQueue;
import com.yanzhenjie.nohttp.rest.Response;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * FileName: MyMoneyActivity <br>
 * Description:  <br>
 * Author: 沈滨伟-13042299081 <br>
 * Date: 2019/4/12 16:51
 */
public class MyMoneyActivity extends Activity {
    //账户余额显示文字控件
    private TextView myMoney;
    private Button btnSure;
    private Button btnsaveMoney;
    private int nowMoney;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_money);
        myMoney = (TextView) findViewById(R.id.my_money);
        btnSure = (Button) findViewById(R.id.btn_Sure);
        btnsaveMoney = (Button) findViewById(R.id.btn_SaveMoney);
        //读取本地文件中的账户余额
        nowMoney = ShareUtils.getInt(MyMoneyActivity.this, "money", 1);
        myMoney.setText(String.valueOf(nowMoney));

        //给确定按钮设置点击事件
        btnSure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //结束当前界面，将自动返回系统上一级未被销毁的活动！比如结账活动或者主页面活动
                MyMoneyActivity.this.finish();
            }
        });

        //给充值按钮设置点击事件
        btnsaveMoney.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                String postUrl = HttpUtil.HOST + "api/user/addMoney";
                //1.创建一个队列
                RequestQueue queue = NoHttp.newRequestQueue();
                //2.创建消息请求   参数1:String字符串,传网址  参数2:请求方式
                final Request<JSONObject> request = NoHttp.createJsonObjectRequest(postUrl, RequestMethod.POST);
                //3.利用队列去添加消息请求
                //使用request对象添加上传的对象添加键与值,post方式添加上传的数据
                request.add("userId", ShareUtils.getInt(getApplicationContext(), "user_id", 0));

                queue.add(1, request, new OnResponseListener<JSONObject>() {
                    @Override
                    public void onStart(int what) {

                    }

                    @Override
                    public void onSucceed(int what, Response<JSONObject> response) {
                        JSONObject res = response.get();
                        try {
                            if (res.getInt("status") == 0) {
                                int data = res.getInt("data");
                                ShareUtils.putInt(MyMoneyActivity.this, "money", data);
                                myMoney.setText(String.valueOf(data));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailed(int what, Response<JSONObject> response) {
                    }

                    @Override
                    public void onFinish(int what) {

                    }
                });

            }
        });
    }
}
