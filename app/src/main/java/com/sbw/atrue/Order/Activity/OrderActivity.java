package com.sbw.atrue.Order.Activity;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.sbw.atrue.Order.Entity.Food;
import com.sbw.atrue.Order.Entity.Product;
import com.sbw.atrue.Order.Util.FoodAdapter;
import com.sbw.atrue.Order.R;
import com.sbw.atrue.Order.Util.FoodFctory;
import com.sbw.atrue.Order.Util.HttpUtil;
import com.sbw.atrue.Order.Util.MyListener;
import com.sbw.atrue.Order.Util.ShareUtils;
import com.yanzhenjie.nohttp.NoHttp;
import com.yanzhenjie.nohttp.RequestMethod;
import com.yanzhenjie.nohttp.rest.OnResponseListener;
import com.yanzhenjie.nohttp.rest.Request;
import com.yanzhenjie.nohttp.rest.RequestQueue;
import com.yanzhenjie.nohttp.rest.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * ClassName: OrderActivity <br>
 * description: 用于展示菜单的活动 <br>
 * author: 沈滨伟-13042299081 <br>
 * Date: 2019/2/18 21:03
 */
public class OrderActivity extends AppCompatActivity {
    //从生产类中加载系统存储的菜单列表
//    private Food[] foods = FoodFctory.Beef;
    private List<Food> foodList = new ArrayList<>();
    private DrawerLayout mDrawerLayout;
    private FoodAdapter adapter;
    private SwipeRefreshLayout swipeRefresh;
    private int first = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);
        //导入Toolbar的自定义布局形式
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //导入滑动菜单
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView navView = (NavigationView) findViewById(R.id.nav_view);
        //获得ActionBar的实例，虽然此ActionBar具体实现是由ToolBar来完成的
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            //将ActionBar的导航按钮HomeAsUp显示出来（Toolbar标题栏最左侧的按钮图标默认为箭头，含义为返回上一级活动）
            actionBar.setDisplayHomeAsUpEnabled(true);
            //更改导航按钮HomeAsUp的默认图标（箭头改为导航键）
            //actionBar.setHomeAsUpIndicator(R.drawable.ic_comment);
        }
        //设置滑动菜单的按钮点击事件
        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                //点击任意菜单选项，则将滑动菜单关闭
                // mDrawerLayout.closeDrawers();
                menuItem.setChecked(true);
                switch (menuItem.getItemId()) {
                    case R.id.nav_Order:
                        //跳转到订单界面
                        orderDetail(new MyListener<JSONArray>() {
                            @Override
                            public void onSuccess(JSONArray array) throws JSONException {
                                StringBuilder sb = new StringBuilder();
                                for (int i = 0; i < array.length(); i++) {
                                    sb.append(getDataString(array.getJSONObject(i)));
                                }
                                Log.e("demo", "onSuccess: "+sb.toString() );
                                Intent intent = new Intent(OrderActivity.this, ReadOrderActivity.class);
                                intent.putExtra("content", sb.toString());
                                startActivity(intent);
                            }
                        });

                        break;
                    case R.id.nav_money:
                        //跳转到钱包界面
                        startActivity(new Intent(OrderActivity.this, MyMoneyActivity.class));
                        break;
                    case R.id.nav_setting:
                        //跳转到钱包界面
                        startActivity(new Intent(OrderActivity.this, SettingActivity.class));
                        break;
                    case R.id.nav_logout:
                        //跳转到登陆界面
                        startActivity(new Intent(OrderActivity.this, LoginActivity.class));
                        //结束当前界面
                        OrderActivity.this.finish();
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
        //悬浮按键的设置
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //跳转到登陆界面
                startActivity(new Intent(OrderActivity.this, ShoppingActivity.class));
            }
        });
        //显示菜单的控件逻辑
        initFruits();
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new FoodAdapter(foodList);
        recyclerView.setAdapter(adapter);
        //刷新菜单的控件逻辑
        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshfoods();
            }
        });
    }

    /**
     * 初始化页面数据
     */
    private void initDatas(String demo) {


    }

    private String getDataString(JSONObject data) throws JSONException {
        double totalPrice = 0; //购买商品总价

        ArrayList<Product> selectedProducts = new ArrayList<>();
        JSONArray array = data.getJSONArray("selectedProducts");
        for (int i = 0; i < array.length(); i++) {
            JSONObject jsonObject = array.getJSONObject(i);
            int id = 0;
            String name = jsonObject.getString("foodName");
            double price = jsonObject.getDouble("price");
//            JSONObject product=jsonObject.getJSONObject("product");

//            String picture = product.getString("picture");


            int sale = 1;
            String shopName = "";
            String detail = "";
            boolean isShowSubBtn = true;
            Product product = new Product(id, name, price, "", sale, shopName, detail, isShowSubBtn);
            product.setSelctCount(jsonObject.getInt("num"));
            selectedProducts.add(product);
        }

        String name = data.getString("userName");
        String phone = data.getString("phone"); //从意图获取手机号
        String tableId = data.getString("tableId"); //从意图获取用餐桌号
        StringBuilder orderResult = new StringBuilder(); //新建订单结果信息
        orderResult.append("\t\t\t\t\t\t\t订单信息如下\n\n");//添加文本内容
        orderResult.append("*************************\n");
        orderResult.append("商品名\t\t\t数量\t\t\t\t价格\n");
        for (int i = 0; i < selectedProducts.size(); i++) { //遍历商品列表并往文本写入商品信息
            Product product = selectedProducts.get(i);
            if (product.getName().length() == 2) {
                orderResult.append(product.getName() + "\t\t\t\t\t\t\t " + product.getSelectedCount() + " \t\t\t\t\t" + product.getPrice() * product.getSelectedCount() + "\n");
            } else {
                orderResult.append(product.getName() + "\t\t\t\t\t" + product.getSelectedCount() + "\t\t\t\t\t" + product.getPrice() * product.getSelectedCount() + "\n");
            }
            totalPrice += product.getPrice() * product.getSelectedCount(); //设置商品总价
        }

        orderResult.append("*************************");
        orderResult.append("\n共计：" + totalPrice + "元\n\n");
        orderResult.append("\t\t\t\t\t\t\t您的信息如下\n");
        orderResult.append("*************************\n");
        orderResult.append("姓名：" + name + "\n");
        orderResult.append("电话：" + phone + "\n");
        orderResult.append("桌号：" + tableId + "\n");
        //获取当前系统时间(记录下单时间)
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());
        String nowTime = simpleDateFormat.format(date);
        orderResult.append("时间：" + nowTime + "\n");
        //将订单结果信息设置到订单信息显示文字控件
        return orderResult.toString();
    }

    private void orderDetail(final MyListener<JSONArray> literer) {
        String postUrl = HttpUtil.HOST + "api/order/list";
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
                        literer.onSuccess(res.getJSONArray("data"));
                    }
                } catch (Exception e) {
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

    /**
     * 刷新菜单
     */
    private void refreshfoods() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //为了避免程序刷新太快，先让线程睡眠2秒
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //从子线程切回到主线程，从而可以更改UI
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //向菜单中增加新菜
                        if (first == 0) {
                            Food WanZi = new Food("牛肉丸", "R.drawable.wanzi", "牛肉丸是潮汕美食啊！！！");
                            foodList.add(WanZi);
                            first = 1;
                        }
                        //通知适配器数据发生了变化，用于RecycleView自动更新显示项
                        adapter.notifyDataSetChanged();
                        //刷新结束，将刷新进度条隐藏
                        swipeRefresh.setRefreshing(false);
                        //显示更新结果的弹框通知，告知用户更新情况
                        AlertDialog.Builder builder = new AlertDialog.Builder(OrderActivity.this);
                        builder.setMessage("菜单更新成功啦~")
                                .setCancelable(false)
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                    }
                                });
                        AlertDialog alert = builder.create();
                        alert.show();
                    }
                });
            }
        }).start();
    }

    /**
     * 将Food数组转换为数组列表
     */
    private void initFruits() {
        foodList.clear();
        String postUrl = HttpUtil.HOST + "api/food/list";
        //1.创建一个队列
        RequestQueue queue = NoHttp.newRequestQueue();
        //2.创建消息请求   参数1:String字符串,传网址  参数2:请求方式
        final Request<JSONObject> request = NoHttp.createJsonObjectRequest(postUrl, RequestMethod.POST);
        //3.利用队列去添加消息请求
        //使用request对象添加上传的对象添加键与值,post方式添加上传的数据

        queue.add(1, request, new OnResponseListener<JSONObject>() {
            @Override
            public void onStart(int what) {

            }

            @Override
            public void onSucceed(int what, Response<JSONObject> response) {
                JSONObject res = response.get();
                try {
                    if (res.getInt("status") == 0) {
                        JSONArray array = res.getJSONArray("data");
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject data = array.getJSONObject(i);

                            Food food = new Food(data.getString("name"), data.getString("imageId"), data.getString("description"));
                            foodList.add(food);
                        }
                        adapter.notifyDataSetChanged();
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

    /**
     * 加载toolbar.xml菜单文件
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }

    /**
     * Toolbat导航栏按键的响应事件
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //android.R.id.home是标题栏最左侧按键HomeAsUp的Id
            case android.R.id.home:
                //将滑动菜单展示出来，GravityCompat.START参数与XML文件定义的保持一致
                mDrawerLayout.openDrawer(GravityCompat.START);
                break;
            case R.id.backup:
                Toast.makeText(this, "You click Backup", Toast.LENGTH_SHORT).show();
                break;
            case R.id.delete:
                Toast.makeText(this, "You click Delete", Toast.LENGTH_SHORT).show();
                break;
            case R.id.setting:
                Toast.makeText(this, "You click Setting", Toast.LENGTH_SHORT).show();
                break;
            default:
        }
        return true;
    }
}
