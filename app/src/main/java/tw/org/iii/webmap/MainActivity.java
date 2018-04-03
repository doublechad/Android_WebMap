package tw.org.iii.webmap;

import android.app.ActionBar;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
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
    private ArrayList<ArrayList<WayPoint>> travelRoute;
    private MyHandler myHandler;
    private ViewPager pager;
    private FragmentManager fm;
    private String[] destinations;
    private MyFragmentAdpater fragmentAdpater;
    private ArrayList<Fragment> hintsFrahgment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        destinations = new String[]{"淡水","101大樓","漁人碼頭"};
        init();
        initMap();

    }
    private void init(){
        map = findViewById(R.id.map);
        myHandler =new MyHandler();
        hintsFrahgment= new ArrayList<>();
        travelRoute=new ArrayList<>();
        for(int i=0;i<destinations.length;i++){
            HintFragment hf =new HintFragment();
            hintsFrahgment.add(hf);
        }
        fm=getSupportFragmentManager();
        fragmentAdpater = new MyFragmentAdpater(fm);
        pager =findViewById(R.id.pager);
        pager.setAdapter(fragmentAdpater);
    }


    private void initMap(){
//        Log.v("chad","initMap");
        map.loadUrl("file:///android_asset/map.html");
        map.setWebViewClient(new myWebViewClient());
        WebSettings settings = map.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setAllowFileAccess(true);
        map.addJavascriptInterface(new JsInterface(this),"JsInterface");

    }

    public void calRoute(String nowPostion,String target) {
        StringBuffer sb = new StringBuffer();
        sb.append("'[");
//        for(int i =0;i<destinations.length ;i++){
//            if(i==0) {
//                sb.append("\""+destinations[i]+"\"");
//            }else{
//                sb.append(","+"\""+destinations[i]+"\"");
//            }
//        }
        sb.append("\""+nowPostion+"\"");
        sb.append(","+"\""+target+"\"");
        sb.append("]'");

        Log.v("chad",sb.toString());
        map.loadUrl("javascript:callFromAndroid("+sb.toString()+")");
//        new MyThread(nowPostion,target).start();


    }

    public void start(View view) {

        Log.v("chad",pager.getCurrentItem()+"");
        calRoute("台北車站",destinations[pager.getCurrentItem()]);
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
                    ArrayList<WayPoint> wayPoints =new ArrayList<>();
                    JSONArray array =(JSONArray)jsonArray.get(i);
                        for(int y=0;y<array.length();y++){
                        JSONObject object = (JSONObject)array.getJSONObject(y);
                        String speak = object.get("notify").toString().replace("<div style=\"font-size:0.9em\">","");
                        String speak2 = speak.replace("<span class=\"location\">","");
                        String lat = object.get("lat").toString();
                        String lng = object.get("lng").toString();
                        String time = object.get("time").toString();
                        Log.v("chad",time);
                        wayPoints.add(new WayPoint(lat,lng,speak2,time));
                    }
                    travelRoute.add(wayPoints);
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
//            for(ArrayList<WayPoint> route :travelRoute) {
//                int index =travelRoute.indexOf(route);
            Log.v("chad",pager.getCurrentItem()+"handler");
                HintFragment hf = (HintFragment) hintsFrahgment.get(pager.getCurrentItem());
                for (WayPoint wp : travelRoute.get(0)) {
                    hf.writeHints(wp.notify +wp.time+ "\r\n");
//                    showSteps.append(wp.notify +wp.time+ "\r\n");
                }
            }
//        }
    }
    private class MyThread extends  Thread{
        private String nowPostion;
        private String target;
        MyThread(String nowPostion,String target){
            this.nowPostion =nowPostion;
            this.target=target;
        }
        @Override
        public void run() {


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
