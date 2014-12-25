package nz.xbc.mobilelinkgetter;

/**
 * Created by xiao on 23/12/14.
 */
public interface LinkHandler {
    void handleLink(String originalUrl, String newUrl);

    void onFinish(String originalUrl);
}
