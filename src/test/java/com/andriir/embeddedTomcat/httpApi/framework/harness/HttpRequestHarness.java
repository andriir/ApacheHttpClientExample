package au.com.clarity.test.httpApi.framework.harness;

import au.com.clarity.integration.dto.globalaccount.GlobalAccount;
import  au.com.clarity.test.httpApi.framework.data.HttpTestUser;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Andrew Redko on 12/1/15.
 */
public class HttpRequestHarness {
    public List<NameValuePair> getLoginParams(HttpTestUser user)
            throws UnsupportedEncodingException {

        //username
        List<NameValuePair> paramList = new ArrayList<NameValuePair>();
        paramList.add(new BasicNameValuePair("username", user.getLogin()));
        paramList.add(new BasicNameValuePair("password", user.getPassword()));
        return paramList;
    }

    public List<NameValuePair> getRoleParams(List<String> permissions, boolean allowed) {
        List<NameValuePair> paramList = new ArrayList<NameValuePair>();
        paramList.add(new BasicNameValuePair("username", ""));
        return paramList;
    }

    public List<NameValuePair> getLoginParams(GlobalAccount globalAccount) {
        List<NameValuePair> paramList = new ArrayList<NameValuePair>();
        paramList.add(new BasicNameValuePair("username", globalAccount.getEmailAddress()));
        paramList.add(new BasicNameValuePair("password", globalAccount.getPassword()));
        return paramList;
    }
}