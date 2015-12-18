package au.com.clarity.test.httpApi.framework;

import au.com.clarity.test.httpApi.framework.harness.HttpTestInfo;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.HeaderGroup;

/**
 * Created by Andrew Redko on 12/1/15.
 */
public class BaseHttpTest {

    private final String USER_AGENT = "Mozilla/5.0";

    protected HeaderGroup getManageHeaderGroup(HttpTestInfo testInfo) {
        HeaderGroup headerGroup = new HeaderGroup();
        headerGroup.addHeader(new BasicHeader("Host", testInfo.getSiteHostURL()));
        headerGroup.addHeader(new BasicHeader("User-Agent", USER_AGENT));
        headerGroup.addHeader(new BasicHeader("Accept",
                "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"));
        headerGroup.addHeader(new BasicHeader("Accept-Language", "en-US,en;q=0.5"));
        headerGroup.addHeader(new BasicHeader("Connection", "keep-alive"));
        headerGroup.addHeader(new BasicHeader("Content-Type", "application/x-www-form-urlencoded"));
        return headerGroup;
    }

}