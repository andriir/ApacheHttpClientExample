package au.com.clarity.test.httpApi.framework.harness;

/**
 * Created by Andrew Redko on 11/30/15.
 */

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.*;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.HeaderGroup;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class HttpClientHarness {
    private static final Logger LOGGER = Logger.getLogger(HttpClientHarness.class);

    HttpTestInfo testInfo;
    HeaderGroup headerGroup;
    private String cookies;
    private CloseableHttpClient client;
    private RequestConfig requestConfig;
    private HttpContext context;

    public HttpClientHarness(HttpTestInfo testInfo, HeaderGroup headerGroup) {
        this.testInfo = testInfo;
        this.headerGroup = headerGroup;
        generateTestGuid();

        RequestConfig globalConfig = RequestConfig.custom()
                .setCookieSpec(CookieSpecs.STANDARD_STRICT)
//                .setCookieSpec(CookieSpecs.STANDARD)
//                .setCookieSpec(CookieSpecs.DEFAULT)
//                .setCookieSpec(CookieSpecs.IGNORE_COOKIES)
                .build();

        CookieStore cookieStore = new BasicCookieStore();
        client = HttpClients.custom()
                .setDefaultRequestConfig(globalConfig)
                .setDefaultCookieStore(cookieStore)
                .build();
//        requestConfig = RequestConfig.custom()
//                .setSocketTimeout(10000)
//                .setConnectTimeout(10000)
//                .build();
        context = new BasicHttpContext();
        context.setAttribute(HttpClientContext.COOKIE_STORE, cookieStore);

    }

    public Document sendPost(String url, List<NameValuePair> postParams)
            throws Exception {

        HttpPost post = new HttpPost(testInfo.getFullUrl(url));
        post.setHeaders(headerGroup.getAllHeaders());
//        post.setHeader("Cookies", getCookies());
        post.setHeader("Referer", testInfo.getFullUrl(url));
        post.setEntity(new UrlEncodedFormEntity(postParams));

//        post.setConfig(requestConfig);
        CloseableHttpResponse response = client.execute(post, context);
//        CloseableHttpResponse response = client.execute(post);
        try {
            int responseCode = response.getStatusLine().getStatusCode();
            LOGGER.info("Sending 'POST' request to URL : " + testInfo.getFullUrl(url));
            LOGGER.info("Post parameters : " + postParams);
            LOGGER.info("Response Code : " + responseCode);

            // set cookies
//        String sessionCookies = null;
//        for (Header header : response.getHeaders("Set-Cookie")) {
//            LOGGER.debug(header.toString());
//            if (header.getValue().contains("ACCLOUD_SITE_JSESSIONID")) {
//                sessionCookies = header.getValue();
//            }
//        }
//        if (sessionCookies == null) {
//            setCookies("");
//        } else {
//            setCookies(sessionCookies);
//        }

            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));

            StringBuffer result = new StringBuffer();
            String line = "";
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }

            Document doc = Jsoup.parse(result.toString());
            return doc;
        } finally {
            response.close();
        }
    }

    public Document sendGet(String url) throws Exception {
        HttpGet post = new HttpGet(testInfo.getFullUrl(url));
        post.setHeaders(headerGroup.getAllHeaders());
//        post.setHeader("Cookies", getCookies());
        post.setHeader("Referer", testInfo.getFullUrl(url));

//        post.setHeader("Accept-Encoding", "gzip, deflate, sdch");
//        post.setHeader("Cookie", "ACCLOUD_SITE_JSESSIONID=71F738220575FD0B2B3C5B85356E233F");
//        post.setHeader("Cookie", "ACCLOUD_SITE_JSESSIONID=71F738220575FD0B2B3C5B85356E233F; lang=en_US");

//        post.setConfig(requestConfig);
        CloseableHttpResponse response = client.execute(post, context);
//        CloseableHttpResponse response = client.execute(post);
        try {
            int responseCode = response.getStatusLine().getStatusCode();
            LOGGER.info("Sending 'GET' request to URL : " + testInfo.getFullUrl(url));
            LOGGER.info("Response Code : " + responseCode);

            //set cookie
//        for (Header header : response.getHeaders("Set-Cookie")) {
//            LOGGER.debug(header.toString());
//            if (header.getValue().contains("ACCLOUD_SITE_JSESSIONID")) {
//                setCookies(header.getValue());
//            }
//        }

            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));

            StringBuffer result = new StringBuffer();
            String line = "";
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }

            Document doc = Jsoup.parse(result.toString());
            return doc;
        } finally {
            response.close();
        }
    }

    public String GetPageContent(String url, HttpTestInfo testInfo) throws Exception {

        HttpGet request = new HttpGet(testInfo.getFullUrl(url));

        request.setHeader("Accept",
                "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        request.setHeader("Accept-Language", "en-US,en;q=0.5");

        HttpResponse response = client.execute(request);
        int responseCode = response.getStatusLine().getStatusCode();

        System.out.println("\nSending 'GET' request to URL : " + url);
        System.out.println("Response Code : " + responseCode);

        BufferedReader rd = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent()));

        StringBuffer result = new StringBuffer();
        String line = "";
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }

        // set cookies
        setCookies(response.getFirstHeader("Set-Cookie") == null ? "" :
                response.getFirstHeader("Set-Cookie").toString());

        return result.toString();

    }

    public String getCookies() {
        if (StringUtil.isBlank(cookies)) {
            return "lang=en_US";
        } else {
            return cookies;
        }
    }

    public void setCookies(String cookies) {
        this.cookies = cookies;
    }

    public HttpTestInfo getTestInfo() {
        return testInfo;
    }

    public void sleepDefault() {
        sleep(1000);
    }

    private void sleep(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            LOGGER.warn("Error during Thread.sleep: " + e.getMessage());
        }
    }

    public void close() throws IOException {
        client.close();
    }

    public void generateTestGuid() {
        String testGuid = generateTestGuidFromDate();
        testInfo.setTestGuid(testGuid);
    }

    /**
     * Generate 10 characters testGuid from current date with the following format: 'MMddHHmmss'
     *
     * @return
     */
    private static String generateTestGuidFromDate() {
        DateFormat df = new SimpleDateFormat("MMddHHmmss");
        Date now = Calendar.getInstance().getTime();
        return df.format(now);
    }

}