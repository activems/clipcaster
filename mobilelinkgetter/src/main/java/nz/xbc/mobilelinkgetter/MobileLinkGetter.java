package nz.xbc.mobilelinkgetter;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by xiao on 23/12/14.
 */
public class MobileLinkGetter {
    private final Context mContext;

    public MobileLinkGetter(Context context){
        mContext = context;
    }

    void loadUrls(String[] seedUrls, LinkHandler linkHandler){
        new MultipleUrlManager(seedUrls,linkHandler).start();
    }

    void loadUrl(String seedUrl, LinkHandler linkHandler){
        loadUrl(makeWebView(mContext), seedUrl, linkHandler);
    }

    void loadUrl(WebView webView, String seedUrl, LinkHandler linkHandler){
        webView.setWebViewClient(new LinkGetterWebViewClient(linkHandler, seedUrl));
        webView.loadUrl("http://" + seedUrl);
    }

    @TargetApi(Build.VERSION_CODES.ECLAIR_MR1)
    private static WebView makeWebView(Context context){
        WebView webView = new WebView(context);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.setWillNotDraw(true);

        return webView;
    }
    class MultipleUrlManager implements LinkHandler {
        String[] mUrls;
        int currentIdx = 0;
        WebView mWebView = makeWebView(mContext);
        private LinkHandler mLinkHandler;

        MultipleUrlManager(String[] urls, LinkHandler linkHandler) {
            mUrls = urls;
            mLinkHandler = linkHandler;
        }

        void start(){
            currentIdx = 0;
            processCurrentUrl();
        }

        void processCurrentUrl(){
            mWebView.setWebViewClient(new LinkGetterWebViewClient(this,"http://" + mUrls[currentIdx]));
        }

        @Override
        public void handleLink(String originalUrl, String newUrl) {
            mLinkHandler.handleLink(originalUrl,newUrl);
        }

        @Override
        public void onFinish(String originalUrl) {
            currentIdx++;
            if(currentIdx >= mUrls.length) {
                mLinkHandler.onFinish(originalUrl);
            } else {
                processCurrentUrl();
            }

        }
    }
    class LinkGetterWebViewClient extends WebViewClient {
        boolean loadingFinished = true;
        boolean redirect = false;
        boolean mDone = false;

        final LinkHandler mLinkHandler;
        final String mOriginalUrl;

        LinkGetterWebViewClient(LinkHandler linkHandler, String originalUrl) {
            mLinkHandler = linkHandler;
            mOriginalUrl = originalUrl;
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String urlNewString) {
            if (!loadingFinished) {
                redirect = true;
            }

            loadingFinished = false;
            view.loadUrl(urlNewString);
            mLinkHandler.handleLink(mOriginalUrl,urlNewString);
            return true;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            loadingFinished = false;
            //SHOW LOADING IF IT ISNT ALREADY VISIBLE
            mLinkHandler.handleLink(mOriginalUrl,url);
        }

        @Override
        public synchronized void onPageFinished(WebView view, String url) {
            if(!redirect){
                loadingFinished = true;
            }
            mLinkHandler.handleLink(mOriginalUrl,url);
            if(loadingFinished && !redirect){
                //HIDE LOADING IT HAS FINISHED
                if(!mDone) {
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            mLinkHandler.onFinish(mOriginalUrl);
                        }
                    }, 5000);
                    mDone = true;
                }
            } else{
                redirect = false;
            }

        }

    }
}
