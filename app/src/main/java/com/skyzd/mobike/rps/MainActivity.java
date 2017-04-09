package com.skyzd.mobike.rps;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity {
    private final String TAG = "MainActivity";
    private LocationClient mLocationClient;
    private BDLocationListener mBDLocationListener;
    private TextView locationInfoTextView = null;
    private EditText timeoutEditText = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        locationInfoTextView = (TextView) this.findViewById(R.id.tv_loc_info);
        locationInfoTextView.setMovementMethod(ScrollingMovementMethod.getInstance());
        timeoutEditText = (EditText) this.findViewById(R.id.timeout);
        timeoutEditText.setText("3");
        // 声明LocationClient类
        mLocationClient = new LocationClient(getApplicationContext());
        mBDLocationListener = new MyBDLocationListener();
        // 注册监听
        mLocationClient.registerLocationListener(mBDLocationListener);

    }

    /**
     * 获得所在位置经纬度及详细地址
     */
    public void getLocation(View view) {
        locationInfoTextView.setText("开始定位。。。");
        // 声明定位参数  
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationMode.Hight_Accuracy);// 设置定位模式 高精度
        option.setCoorType("bd09mc");// 设置返回定位结果是百度经纬度 默认gcj02
        option.setScanSpan(5000);// 设置发起定位请求的时间间隔 单位ms  
        option.setIsNeedAddress(true);// 设置定位结果包含地址信息  
        option.setNeedDeviceDirect(true);// 设置定位结果包含手机机头 的方向  
        mLocationClient.setLocOption(option);

        List<String> pses = new ArrayList<String>();
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED) {
            pses.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED) {
            pses.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (pses.size() > 0) {
            Log.d("TTTT", "弹出提示");
            ActivityCompat.requestPermissions(MainActivity.this, pses.toArray(new String[pses.size()]), 1);
            return;
        } else {
            mLocationClient.start();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mLocationClient != null) {
            mLocationClient.unRegisterLocationListener(mBDLocationListener);
        }
    }

    private class MyBDLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            // 非空判断  
            if (location != null) {
                // 根据BDLocation 对象获得经纬度以及详细地址信息  
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                String address = location.getAddrStr();
                Log.i(TAG, "address:" + address + " latitude:" + latitude
                        + " longitude:" + longitude + "---");
                getRps(location);
                if (mLocationClient.isStarted()) {
                    // 获得位置之后停止定位
                }
                mLocationClient.stop();
            }
        }

        @Override
        public void onConnectHotSpotMessage(String s, int i) {

        }
    }

    private void getRps(BDLocation location) {
        sendMsg("start request nearby");
        HttpParams params =new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(params, 1000);
        try {
            HttpConnectionParams.setSoTimeout(params, Integer.parseInt(timeoutEditText.getText().toString()) * 1000);
        } catch (Exception e) {
            HttpConnectionParams.setSoTimeout(params, 3000);
        }
        System.out.println("==========" + timeoutEditText.getText().toString());
        HttpClient client = new DefaultHttpClient(params);
        try {
            String uri="https://mwx.mobike.com/mobike-api/rent/nearbyBikesInfo.do";
            HttpPost httpPost = new HttpPost(uri);
            List<NameValuePair> parameters = new ArrayList<NameValuePair>();
            parameters.add(new BasicNameValuePair("latitude", ""+location.getLatitude()));
            parameters.add(new BasicNameValuePair("longitude", ""+location.getLongitude()));
//            parameters.add(new BasicNameValuePair("citycode", "021"));
            parameters.add(new BasicNameValuePair("accuracy", ""+location.getRadius()));
            parameters.add(new BasicNameValuePair("speed", "0"));
            parameters.add(new BasicNameValuePair("errMsg", "getLocation%3Aok"));
            Log.d("parameters", parameters.toString());
            httpPost.addHeader("charset", "utf-8");
            httpPost.addHeader("platform", "4");
            httpPost.addHeader("eption", "ad333");
            httpPost.addHeader("open_src", "list");
            httpPost.addHeader("content-type", "application/x-www-form-urlencoded");
            httpPost.addHeader("lang", "zh");
            httpPost.addHeader("referer", "https://servicewechat.com");

            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(parameters, "UTF-8");//设置传递参数的编码
            httpPost.setEntity(entity);
            HttpResponse response = client.execute(httpPost); //HttpUriRequest的后代对象 //在浏览器中敲一下回车
            //4. 读 response
            if(response.getStatusLine().getStatusCode()==200){//判断状态码
                String rspTxt = EntityUtils.toString(response.getEntity());
                Log.d("response", rspTxt);
                JSONObject jsonObject = new JSONObject(rspTxt);
                JSONArray bikes = jsonObject.getJSONArray("object");
                StringBuilder bikeIds = new StringBuilder();
                int count = 0;
                for (int i = 0; i < bikes.length(); i++) {
                    JSONObject bikeInfo = bikes.getJSONObject(i);
                    if (bikeInfo.getInt("biketype") == 999) {
                        bikeIds.append(bikeInfo.getString("distId") + "     @" + bikeInfo.get("distance") + "m@" + "\n");
                        count++;
                    }
                }
                bikeIds.insert(0, "一共（" + count + "）红包车" + "\n");
                bikeIds.insert(0, location.getAddrStr() + "\n");
                sendMsg(bikeIds.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendMsg("error: timeout");
        }finally{
            client.getConnectionManager().shutdown();
        }
        Log.d("request", "结束");
    }

    @TargetApi(23)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {

            // requestCode即所声明的权限获取码，在checkSelfPermission时传入
            case 1:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationClient.start();
                } else {
                    Log.d("TTTT", "啊偶，被拒绝了，少年不哭，站起来撸");
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode,
                        permissions, grantResults);
                break;

        }
    }

    private Handler handler = new Handler()  {
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            switch (msg.what) {
                case 0:
                    locationInfoTextView.setText(bundle.getString("default"));
                    break;
                default:
                    locationInfoTextView.setText("unknown error");
            }
        }
    };

    private void sendMsg(String msg) {
        Message message = new Message();
        Bundle bundle = new Bundle();
        message.what = 0;
        bundle.putString("default", msg);
        message.setData(bundle);
        handler.sendMessage(message);
    }
}