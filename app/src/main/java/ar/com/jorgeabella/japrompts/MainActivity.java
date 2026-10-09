package ar.com.jorgeabella.japrompts;

import android.app.Activity;
import android.os.Bundle;
import android.os.Build;
import android.content.Intent;
import android.content.ContentValues;
import android.graphics.Color;
import android.net.Uri;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.ValueCallback;
import android.view.Window;
import androidx.webkit.WebViewAssetLoader;
import java.util.UUID;

public class MainActivity extends Activity {
    private static final int PICK_PHOTO = 741;
    private WebView webView;
    private ValueCallback<Uri[]> pendingFile;
    private Uri captureUri;
    private WebViewAssetLoader assetLoader;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        Window w=getWindow();
        w.setStatusBarColor(Color.rgb(25,99,40));
        w.setNavigationBarColor(Color.rgb(15,56,26));
        assetLoader = new WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this))
            .build();
        webView=new WebView(this);
        webView.setBackgroundColor(Color.WHITE);
        WebSettings s=webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setAllowFileAccess(false);
        s.setAllowContentAccess(true);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        s.setBuiltInZoomControls(false);
        s.setDisplayZoomControls(false);
        webView.setWebViewClient(new WebViewClient(){
            @Override public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                return assetLoader.shouldInterceptRequest(request.getUrl());
            }
        });
        webView.setWebChromeClient(new WebChromeClient(){
            @Override public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> callback, FileChooserParams params) {
                if (pendingFile!=null) pendingFile.onReceiveValue(null);
                pendingFile=callback;
                Intent gallery=new Intent(Intent.ACTION_GET_CONTENT);
                gallery.addCategory(Intent.CATEGORY_OPENABLE);
                gallery.setType("image/*");
                Intent camera=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                captureUri=null;
                ContentValues vals=new ContentValues();
                vals.put(MediaStore.Images.Media.DISPLAY_NAME,"keto_"+UUID.randomUUID()+".jpg");
                vals.put(MediaStore.Images.Media.MIME_TYPE,"image/jpeg");
                if (Build.VERSION.SDK_INT>=29) vals.put(MediaStore.Images.Media.RELATIVE_PATH,"Pictures/KetoTucuman");
                try {
                    captureUri=getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,vals);
                    if (captureUri!=null) camera.putExtra(MediaStore.EXTRA_OUTPUT,captureUri);
                } catch(Exception ignored) { captureUri=null; }
                Intent chooser=Intent.createChooser(gallery,"Seleccionar foto de comida");
                if(captureUri!=null) chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS,new Parcelable[]{camera});
                try { startActivityForResult(chooser,PICK_PHOTO); return true; }
                catch(Exception ex){ pendingFile=null; callback.onReceiveValue(null); return false; }
            }
        });
        setContentView(webView);
        webView.loadUrl("https://appassets.androidplatform.net/assets/index.html");
    }

    @Override protected void onActivityResult(int req,int result,Intent data){
        super.onActivityResult(req,result,data);
        if(req==PICK_PHOTO && pendingFile!=null){
            Uri photo=null;
            if(result==RESULT_OK){
                if(data!=null && data.getData()!=null) photo=data.getData();
                else photo=captureUri;
            }
            pendingFile.onReceiveValue(photo==null?null:new Uri[]{photo});
            pendingFile=null; captureUri=null;
        }
    }

    @Override public void onBackPressed(){
        if(webView!=null && webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }
    @Override protected void onDestroy(){
        if(pendingFile!=null){pendingFile.onReceiveValue(null);pendingFile=null;}
        if(webView!=null){webView.destroy();webView=null;}
        super.onDestroy();
    }
}
