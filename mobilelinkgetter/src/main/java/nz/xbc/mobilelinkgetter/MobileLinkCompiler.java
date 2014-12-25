package nz.xbc.mobilelinkgetter;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class MobileLinkCompiler implements LinkHandler {

    MobileLinkGetter mLinkGetter;
    final Activity mActivity;
    CompiledLinkHandler mCompiledLinkHandler;
    public interface CompiledLinkHandler {
        void handle(UrlEntry[] links);
    }

    public MobileLinkCompiler(Activity activity) {
        mActivity = activity;
        mLinkGetter = new MobileLinkGetter(mActivity);
    }

    public void addFacebookUrls(){
        final String FACEBOOK_PREFIX = "https://m.facebook.com/?refsrc=https%3A%2F%2F";
        final String FACEBOOK_POST = "%2F&_rdr";
    }

    public void getUrls(CompiledLinkHandler handler){
        mCompiledLinkHandler = handler;
        mLinkGetter.loadUrls(SEED_URLS, this);
    }

    @Override
    public synchronized void handleLink(String originalUrl, String newUrl) {
        Log.i("moblnkgt",originalUrl + " -> " + newUrl);
        if(originalUrl != null && newUrl != null) {
            UrlEntry urlEntry = entries.get(originalUrl);
            if (urlEntry == null) {
                urlEntry = new UrlEntry(originalUrl);
                entries.put(originalUrl, urlEntry);
            }


            urlEntry.resolvedUrls.add(newUrl);
        }
    }

    @Override
    public void onFinish(String originalUrl) {
        Log.i("moblnkgt", originalUrl +" done");
        Collection<UrlEntry> values = entries.values();
        mCompiledLinkHandler.handle(values.toArray(new UrlEntry[values.size()]));
    }


    public static class UrlEntry {
        public UrlEntry(String sourceUrl) {
            this.sourceUrl = sourceUrl;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("UrlEntry{").append('\n');
            sb.append("sourceUrl='").append(sourceUrl).append('\'')
                    .append('\n');
            sb.append(", resolvedUrls=").append(resolvedUrls)
                    .append('\n');
            sb.append('}');
            return sb.toString();
        }

        public String sourceUrl;
        public Set<String> resolvedUrls = new HashSet<>();

    }

    static final String[] SEED_URLS = new String[] {
            "twitter.com",
            "facebook.com",
    };

    static final Map<String, UrlEntry> entries = new HashMap<>();

}
