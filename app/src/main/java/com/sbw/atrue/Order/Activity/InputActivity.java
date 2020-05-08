package com.sbw.atrue.Order.Activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.sbw.atrue.Order.R;
import com.sbw.atrue.Order.Entity.Product;
import com.sbw.atrue.Order.Util.HttpUtil;
import com.sbw.atrue.Order.Util.MyListener;
import com.sbw.atrue.Order.Util.ShareUtils;
import com.yanzhenjie.nohttp.NoHttp;
import com.yanzhenjie.nohttp.RequestMethod;
import com.yanzhenjie.nohttp.rest.OnResponseListener;
import com.yanzhenjie.nohttp.rest.Request;
import com.yanzhenjie.nohttp.rest.RequestQueue;
import com.yanzhenjie.nohttp.rest.Response;

import org.json.JSONException;
import org.json.JSONObject;

public class InputActivity extends Activity {
    private EditText etname; //姓名输入框
    private EditText etPhone; //手机号输入框
    private EditText ettable; //桌号输入框
    private Button btnInputCompleted; //输入完成按钮
    private ArrayList<Product> selectedProducts = new ArrayList<Product>(); //被选择的的商品数据


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input);
        initViews(); //初始化页面控件
        initEvents(); //初始化控件事件
    }

    //初始化控件事件
    private void initEvents() {
        Intent otherIntent = getIntent(); //获取信息输入界面传来的意图
        selectedProducts = otherIntent.getParcelableArrayListExtra("selectedProducts"); //从意图中获取被选择的的商品数据
        etPhone.setSelection(etPhone.getText().length()); //将光标移动到电话输入框最后面
        etname.setText(ShareUtils.getString(InputActivity.this, "user_true_name", ""));//从本地读取客户姓名
        etPhone.setText(ShareUtils.getString(InputActivity.this, "user_true_phone", ""));//从本地读取客户电话
        //给输入完成按钮设置点击事件
        btnInputCompleted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String address = ettable.getText().toString(); //获取输入框中的桌号
                final String name = etname.getText().toString(); //获取输入框中的客户姓名
                final String phone = etPhone.getText().toString(); //获取输入框中的手机号
                if (!TextUtils.isEmpty(phone)
                        && !TextUtils.isEmpty(address)
                        && phone.length() >= 7) { //手机号和地址不为空且手机号大于等于7位


                    addOrder(selectedProducts, name, phone, address, new MyListener<String>() {
                        @Override
                        public void onSuccess(String data) {
                            Intent intent = new Intent(getApplicationContext(), SuccessActivity.class); //新建意图
                            intent.putParcelableArrayListExtra("selectedProducts", selectedProducts); //给意图添加被选择的的商品数据
                            intent.putExtra("name", name); //给意图设置客户姓名
                            intent.putExtra("phone", phone); //给意图设置联系电话
                            intent.putExtra("orderId", data); //给意图设置用餐桌号
                            intent.putExtra("table", address); //给意图设置用餐桌号
                            startActivity(intent); //跳转到购买成功页面
                            ShoppingActivity.mInstance.finish(); //结束购物界面
                            InputActivity.this.finish(); //结束信息输入界面
                        }
                    });

                } else { //输入手机号或地址不正确
                    //Toast.makeText(InputActivity.this, "手机号或地址输入不正确，手机号至少7位", Toast.LENGTH_SHORT).show();
                    showDialog("手机号或地址输入不正确，手机号至少7位!");
                }
            }
        });
    }

    private void addOrder(List<Product> productList, String name, String phone, String address, final MyListener<String> literer) {
        String postUrl = HttpUtil.HOST + "api/order/addOrder";
        //1.创建一个队列
        RequestQueue queue = NoHttp.newRequestQueue();
        //2.创建消息请求   参数1:String字符串,传网址  参数2:请求方式
        final Request<JSONObject> request = NoHttp.createJsonObjectRequest(postUrl, RequestMethod.POST);
        //3.利用队列去添加消息请求
        //使用request对象添加上传的对象添加键与值,post方式添加上传的数据
        request.add("userId", ShareUtils.getInt(getApplicationContext(), "user_id", 0));
        request.add("name", name);
        request.add("phone", phone);
        request.add("tableId", address);
        request.add("productList", new Gson().toJson(productList));

        queue.add(1, request, new OnResponseListener<JSONObject>() {
            @Override
            public void onStart(int what) {

            }

            @Override
            public void onSucceed(int what, Response<JSONObject> response) {
                JSONObject res = response.get();
                try {
                    if (res.getInt("status") == 0) {
                        literer.onSuccess(res.getString("data"));
                    } else {
                        showDialog(res.getString("msg"));
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

    //初始化页面控件
    private void initViews() {
        etname = (EditText) findViewById(R.id.et_name);
        etPhone = (EditText) findViewById(R.id.et_phone);
        ettable = (EditText) findViewById(R.id.et_table);
        btnInputCompleted = (Button) findViewById(R.id.btn_input_completed);
    }

    /**
     * 为了方便，定义一个弹框控件的函数
     *
     * @param msg 要显示的提示信息
     */
    private void showDialog(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(msg)
                .setCancelable(false)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }
}
