package com.sbw.atrue.Order.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

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
 * FileName: SettingActivity <br>
 * Description:  <br>
 * Author: 沈滨伟-13042299081 <br>
 * Date: 2019/4/12 20:55
 */
public class SettingActivity extends Activity {
    private EditText etname; //姓名输入框
    private EditText etPhone; //手机号输入框
    private EditText etMail; //邮箱输入框
    private Button btnInputCompleted; //输入完成按钮

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.person_setting);
        etname = (EditText) findViewById(R.id.et_name);
        etPhone = (EditText) findViewById(R.id.et_phone);
        etMail = (EditText) findViewById(R.id.et_mail);
        btnInputCompleted = (Button) findViewById(R.id.btn_input_completed);
        //登录时默认自动在本地保存了一份客户信息，将其读取出来并显示
        etname.setText(ShareUtils.getString(this, "user_true_name", ""));
        etPhone.setText(ShareUtils.getString(this, "user_true_phone", ""));
        etMail.setText(ShareUtils.getString(this, "user_true_mail", ""));
        btnInputCompleted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                String postUrl = HttpUtil.HOST + "api/user/updateUser";
                //1.创建一个队列
                RequestQueue queue = NoHttp.newRequestQueue();
                //2.创建消息请求   参数1:String字符串,传网址  参数2:请求方式
                final Request<JSONObject> request = NoHttp.createJsonObjectRequest(postUrl, RequestMethod.POST);
                //3.利用队列去添加消息请求
                //使用request对象添加上传的对象添加键与值,post方式添加上传的数据
                request.add("id", ShareUtils.getInt(getApplicationContext(), "user_id", 0));
                request.add("nickname", etname.getText().toString());
                request.add("phone", etPhone.getText().toString());
                request.add("email", etMail.getText().toString());

                queue.add(1, request, new OnResponseListener<JSONObject>() {
                    @Override
                    public void onStart(int what) {

                    }

                    @Override
                    public void onSucceed(int what, Response<JSONObject> response) {
                        JSONObject res = response.get();
                        try {
                            if (res.getInt("status") == 0) {
                                //保存用户新设置的客户个人信息
                                ShareUtils.putString(SettingActivity.this, "user_true_name", etname.getText().toString());
                                ShareUtils.putString(SettingActivity.this, "user_true_phone", etPhone.getText().toString());
                                ShareUtils.putString(SettingActivity.this, "user_true_mail", etMail.getText().toString());
                                //登录成功，跳转到菜单展示的主页面
                                Intent intent = new Intent(SettingActivity.this, OrderActivity.class);
                                startActivity(intent);
                                finish();
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
