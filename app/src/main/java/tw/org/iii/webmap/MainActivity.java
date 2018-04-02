package tw.org.iii.webmap;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
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
    private TextView showSteps;
    private ArrayList<ArrayList<WayPoint>> travelRoute;
    private MyHandler myHandler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        map = findViewById(R.id.map);
        myHandler =new MyHandler();
        showSteps = findViewById(R.id.showSteps);
        travelRoute=new ArrayList<>();
        initMap();

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

    public void calRoute() {
//        map.loadUrl("javascript:calcRoute("+"'台北車站'"+","+"'北投'"+")");
        String[]  destinations = new String[]{"台北車站","自然科學博物館","彰化火車站"};
        StringBuffer sb = new StringBuffer();
        sb.append("'[");
        for(int i =0;i<destinations.length ;i++){
            if(i==0) {
                sb.append("\""+destinations[i]+"\"");
            }else{
                sb.append(","+"\""+destinations[i]+"\"");
            }
        }
        sb.append("]'");
        Log.v("chad",sb.toString());
        map.loadUrl("javascript:callFromAndroid("+sb.toString()+")");

    }

    private class myWebViewClient extends WebViewClient{
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
//            map.loadUrl("javascript:calcRoute("+"'台北車站'"+","+"'北投'"+")");
            super.onPageFinished(view, url);
            calRoute();

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
            try {
                JSONArray jsonArray = new JSONArray(st3);
                Log.v("chad",jsonArray.length()+"");
                for(int i=0;i<jsonArray.length();i++){
                    ArrayList<WayPoint> wayPoints =new ArrayList<>();
                    JSONArray array =(JSONArray)jsonArray.get(i);
//                    Log.v("chad",array.length()+"");
                        for(int y=0;y<array.length();y++){
                        JSONObject object = (JSONObject)array.getJSONObject(y);
                        String speak = object.get("notify").toString().replace("<div style=\"font-size:0.9em\">","");
                        String lat = object.get("lat").toString();
                        String lng = object.get("lng").toString();
                        String time = object.get("time").toString();
                        Log.v("chad",time);
                        wayPoints.add(new WayPoint(lat,lng,speak,time));
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
            for(ArrayList<WayPoint> route :travelRoute) {
                for (WayPoint wp : route) {
                    showSteps.append(wp.notify +wp.time+ "\r\n");
                }
            }
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