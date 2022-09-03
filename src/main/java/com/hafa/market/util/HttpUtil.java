package com.hafa.market.util;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author heavytiger
 * @version 1.0
 * @description Http请求工具封装
 * @date 2022/4/19 13:50
 */
public class HttpUtil {
    /**
     * @param url 请求的地址
     * @param param 附加的请求
     * @return 返回得到的结果
     */
    public static String doGet(String url, Map<String, Object> param) {
        //创建默认的httpclient实例
        CloseableHttpClient httpClient = HttpClients.createDefault();
        //响应对象
        CloseableHttpResponse response = null;
        //返回结果
        String resultStr = "";
        try {
            URIBuilder builder = new URIBuilder(url);
            if (param != null) {
                for (String key : param.keySet()) {
                    builder.addParameter(key, String.valueOf(param.get(key)));
                }
            }
            URI uri = builder.build();
            HttpGet httpGet = new HttpGet(uri);
            response = httpClient.execute(httpGet);

            if (response != null && response.getStatusLine().getStatusCode() == 200) {
                HttpEntity httpEntity = response.getEntity();
                resultStr = EntityUtils.toString(httpEntity, "UTF-8");
            }

        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
                httpClient.close();

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return resultStr;
    }

    /**
     * Post请求
     *
     * @param url 请求的地址
     * @param param 附加的请求
     * @return 返回得到的结果
     */
    public static String doPost(String url, Map<String, Object> param, Map<String, Object> headData) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        String resultString = "";

        try {
            HttpPost httpPost = new HttpPost(url);
            if (param != null) {
                List<NameValuePair> formParams = new ArrayList<>();
                for (String key : param.keySet()) {
                    formParams.add(new BasicNameValuePair(key, String.valueOf(param.get(key))));
                }
                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formParams, "UTF-8");
                httpPost.setEntity(entity);
            }
            //携带head数据
            if (headData != null && headData.size() > 0) {
                for (String key : headData.keySet()) {
                    httpPost.setHeader(key, (String) headData.get(key));
                }
            }

            //执行post请求
            response = httpClient.execute(httpPost);

            if (response != null && response.getStatusLine().getStatusCode() == 200) {
                resultString = EntityUtils.toString(response.getEntity(), "UTF-8");
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
                httpClient.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return resultString;
    }
}
