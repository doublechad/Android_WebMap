package tw.org.iii.webmap;

import android.Manifest;
import android.app.ActionBar;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private WebView map;
//    private ArrayList<WayPoint> travelRoute;
    private ArrayList<WayPoint> wayPoints;
    private MyHandler myHandler;
    private ViewPager pager;
    private FragmentManager fm;
    private String[] destinations;
    private MyFragmentAdpater fragmentAdpater;
    private ArrayList<Fragment> hintsFrahgment;
    private LocationManager lmgr;
    private MyLocationListener listener;
    // 感應器管理
    private SensorManager mSensorManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},
                    123);
        } else {
            init();
            initMap();
        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        lmgr.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 1000, 0, listener);

    }

    @Override
    protected void onPause() {
        super.onPause();
        lmgr.removeUpdates(listener);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            init();
            initMap();
        } else {
            finish();
        }
    }

    private void init() {
        lmgr = (LocationManager) getSystemService(LOCATION_SERVICE);
        listener = new MyLocationListener();
        destinations = new String[]{"淡水", "101大樓", "漁人碼頭"};
        map = findViewById(R.id.map);
        myHandler = new MyHandler();
        hintsFrahgment = new ArrayList<>();
//        travelRoute = new ArrayList<>();
        for (int i = 0; i < destinations.length; i++) {
            HintFragment hf = new HintFragment();
            hintsFrahgment.add(hf);
        }
        fm = getSupportFragmentManager();
        fragmentAdpater = new MyFragmentAdpater(fm);
        pager = findViewById(R.id.pager);
        pager.setAdapter(fragmentAdpater);
    }


    private void initMap() {
//        Log.v("chad","initMap");
        map.loadUrl("file:///android_asset/map.html");
        map.setWebViewClient(new myWebViewClient());
        WebSettings settings = map.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setAllowFileAccess(true);
        map.addJavascriptInterface(new JsInterface(this), "JsInterface");

    }

    public void calRoute(String target) {
        StringBuffer sb = new StringBuffer();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

        }
        if(lmgr.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            Location location =lmgr.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if(location!=null) {
                double lat = location.getLatitude();
                double lng = location.getLongitude();
                Log.v("chad", lat + ":" + lng);
                sb.append("'[");
                sb.append("\"" + lat + "\"");
                sb.append("," + "\"" + lng + "\"");
                sb.append("," + "\"" + target + "\"");
                sb.append("]'");
                Log.v("chad", sb.toString());
                map.loadUrl("javascript:callFromAndroid(" + sb.toString() + ")");
            }else {
                Log.v("chad","室內");
            }
        }else{
            Toast.makeText(this,"請開啟定位",Toast.LENGTH_SHORT).show();
        }



    }

    public void start(View view) {

        Log.v("chad",pager.getCurrentItem()+"");
        calRoute(destinations[pager.getCurrentItem()]);
    }

    private class myWebViewClient extends WebViewClient{
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);


        }
    }

    private class JsInterface {
        private Context mContext;
        public JsInterface(Context context) {
            this.mContext = context;
        }//在js中调用window.AndroidWebView.showInfoFromJs(name)，便会触发此方法。
        @JavascriptInterface
        public void showInfoFromJs(String name) {
            Log.v("chad",name);
            String st1 =name.replace("<b>"," ");
            String st2 =st1.replace("</b>"," ");
            String st3 =st2.replace("</div>","");
            String st4 =st3.replace("</span>","");
            try {
                JSONArray jsonArray = new JSONArray(st4);
                Log.v("chad",jsonArray.length()+"");
                for(int i=0;i<jsonArray.length();i++){
                    wayPoints =new ArrayList<>();
                    JSONArray array =(JSONArray)jsonArray.get(i);
                        for(int y=0;y<array.length();y++){
                        String speak2;
                        JSONObject object = (JSONObject)array.getJSONObject(y);
                        if(!object.equals("undefined")) {
                            String speak = object.get("notify").toString().replace("<div style=\"font-size:0.9em\">", "");
                            speak2 = speak.replace("<span class=\"location\">", "");
                        }else{
                            speak2="";
                        }
                        Log.v("chad","第"+y+"個"+speak2);
                        String lat = object.get("lat").toString();
                        String lng = object.get("lng").toString();
                        String time = object.get("time").toString();
                        Log.v("chad",time);
                        wayPoints.add(new WayPoint(lat,lng,speak2,time));
                    }
//                    travelRoute.add(wayPoints);
                }
            } catch (Exception e) {
                Log.v("chad",e.toString());
            }

            myHandler.sendEmptyMessage(0);

        }
    }
    private class MyHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            Log.v("chad",pager.getCurrentItem()+"handler");
                HintFragment hf = (HintFragment) hintsFrahgment.get(pager.getCurrentItem());
                hf.clearText();
                for (WayPoint wp : wayPoints) {
                    hf.writeHints(wp.notify +wp.time+ "\r\n");
                }
            }

    }
    private class MyFragmentAdpater extends FragmentPagerAdapter{

        public MyFragmentAdpater(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return hintsFrahgment.get(position);
        }

        @Override
        public int getCount() {
            return (destinations.length);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return destinations[position];
        }
    }
    private class MyLocationListener implements LocationListener{
        @Override
        public void onLocationChanged(Location location) {
            double lat =location.getLatitude();
            double lng =location.getLongitude();
            map.loadUrl("javascript:changeMark("+lat+","+lng+",0)");

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

            Log.v("chad","provider : "+provider+" status : "+status);
        }

        @Override
        public void onProviderEnabled(String provider) {

            Log.v("chad","onProviderEnabled");

        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.v("chad","onProviderDisabled: "+provider);


        }
    }
}
class WayPoint{
    String notify;
    String lat;
    String lng;
    String time;
    WayPoint(String lat,String lng,String notify,String time){
        this.time =time;
        this.notify=notify;
        this.lat=lat;
        this.lng=lng;
    }
}
