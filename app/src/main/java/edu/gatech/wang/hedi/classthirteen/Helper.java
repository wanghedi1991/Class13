package edu.gatech.wang.hedi.classthirteen;

import org.apache.http.NameValuePair;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Hedi Wang on 2015/8/1.
 */
public class Helper {
    public static String convertToUTF8(String str) {
        try {
            byte[] converttoBytes = str.getBytes("UTF-8");
            String s2 = new String(converttoBytes, "UTF-8");
            return s2;
        } catch (UnsupportedEncodingException e) {
            return str;
        }
    }

    public static String getQuery(List<NameValuePair> params) throws UnsupportedEncodingException
    {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (NameValuePair pair : params)
        {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(pair.getName(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
        }

        return result.toString();
    }
}
