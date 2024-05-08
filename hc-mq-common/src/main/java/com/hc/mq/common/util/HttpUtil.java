package com.hc.mq.common.util;


import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.net.ssl.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


/**
 * http 工具类
 *
 * @Author hc
 */
public class HttpUtil {
    private static Logger logger = LoggerFactory.getLogger(HttpUtil.class);

    /**
     * 请求超时时间
     */
    private static Integer TIMEOUT = 3000;

    static {
        try {
            trustAllHttpsCertificates();
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                public boolean verify(String urlHostName, SSLSession session) {
                    return true;
                }
            });
        } catch (Exception e) {
        }
    }

    private static void trustAllHttpsCertificates() throws NoSuchAlgorithmException, KeyManagementException {
        TrustManager[] trustAllCerts = new TrustManager[1];
        trustAllCerts[0] = new TrustAllManager();
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, null);
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
    }

    public static class TrustAllManager implements X509TrustManager {
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        public void checkServerTrusted(X509Certificate[] certs, String authType) throws CertificateException {
        }

        public void checkClientTrusted(X509Certificate[] certs, String authType) throws CertificateException {
        }
    }

    private static byte[] readInputStream(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[1024];
        int len = 0;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        while ((len = inputStream.read(buffer)) != -1) {
            bos.write(buffer, 0, len);
        }
        bos.close();
        return bos.toByteArray();
    }

    /**
     * 下载网络图片
     *
     * @param imageUrl
     * @return
     */
    public static BufferedImage getImage(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            // 设置超时间为240秒
            conn.setConnectTimeout(240 * 1000);
            // 防止屏蔽程序抓取而返回403错误
            conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
            // 得到输入流
            InputStream inputStream = conn.getInputStream();
            // 获取自己数组
            byte[] getData = readInputStream(inputStream);
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(getData));
            return image;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * doJsonPostFull请求
     *
     * @param @param  postUrl
     * @param @param  param
     * @param @param  jsonData
     * @param @return
     * @return String
     * @throws IOException
     * @throws
     */
    public static String doJsonPostFull(String postUrl, String jsonData, Map<String, Object> property) throws Exception {
        // 封装发送的请求参数
        URL url = new URL(postUrl);
        URLConnection urlConnection = url.openConnection();
        HttpURLConnection httpUrlConnection = (HttpURLConnection) urlConnection;
        httpUrlConnection.setConnectTimeout(TIMEOUT);
        httpUrlConnection.setUseCaches(false);// 设置不要缓存
        httpUrlConnection.setRequestMethod("POST");
        // 设置请求头属性参数
        httpUrlConnection.setRequestProperty("charset", "UTF-8");
        httpUrlConnection.setRequestProperty("Content-Type", "application/json");
        httpUrlConnection.setRequestProperty("connection", "Keep-Alive");
        httpUrlConnection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
        httpUrlConnection.setRequestProperty("accept", "*/*");
        if (property != null) {
            for (Entry<String, Object> map : property.entrySet()) {
                httpUrlConnection.setRequestProperty(map.getKey(), map.getValue().toString());
            }
        }
        // 发送POST请求必须设置如下两行
        httpUrlConnection.setDoOutput(true);
        httpUrlConnection.setDoInput(true);
        String response = "";// 响应内容
        String status = "";// 响应状态
        OutputStream out = null;
        BufferedReader in = null;
        try {
            // 获取URLConnection对象对应的输出流
            out = httpUrlConnection.getOutputStream();
            // 发送Json请求参数
            out.write(jsonData.getBytes());
            // flush输出流的缓冲
            out.flush();
            httpUrlConnection.connect();
            // 定义BufferedReader输入流来读取URL的响应数据
            in = new BufferedReader(new InputStreamReader(httpUrlConnection.getInputStream(), StandardCharsets.UTF_8));
            String line;
            while ((line = in.readLine()) != null) {
                response += line;
            }
            // 获得URL的响应状态码
            status = Integer.toString(httpUrlConnection.getResponseCode());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                throw ex;
            }
        }
        if (!status.startsWith("2")) {
            throw new Exception("请求结果错误");
        }
        return response;
    }

    /**
     * JsonPost请求
     *
     * @param @param  postUrl
     * @param @param  param
     * @param @return
     * @return String
     * @throws IOException
     * @throws
     */
    public static String doJsonPost(String postUrl, String jsonData, Map<String, Object> property) throws Exception {
        // 封装发送的请求参数
        URL url = new URL(postUrl);
        URLConnection urlConnection = url.openConnection();
        HttpURLConnection httpUrlConnection = (HttpURLConnection) urlConnection;
        httpUrlConnection.setConnectTimeout(TIMEOUT);
        httpUrlConnection.setUseCaches(false);// 设置不要缓存
        httpUrlConnection.setRequestMethod("POST");
        // 设置请求头属性参数
        httpUrlConnection.setRequestProperty("charset", "UTF-8");
        httpUrlConnection.setRequestProperty("Content-Type", "application/json");
        httpUrlConnection.setRequestProperty("connection", "Keep-Alive");
        httpUrlConnection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
        httpUrlConnection.setRequestProperty("accept", "*/*");
        if (property != null) {
            for (Entry<String, Object> map : property.entrySet()) {
                httpUrlConnection.setRequestProperty(map.getKey(), map.getValue().toString());
            }
        }
        // 发送POST请求必须设置如下两行
        httpUrlConnection.setDoOutput(true);
        httpUrlConnection.setDoInput(true);
        String response = "";// 响应内容
        String status = "";// 响应状态
        OutputStream out = null;
        BufferedReader in = null;
        try {
            // 获取URLConnection对象对应的输出流
            out = httpUrlConnection.getOutputStream();
            // 发送Json请求参数
            out.write(jsonData.getBytes());
            // flush输出流的缓冲
            out.flush();
            httpUrlConnection.connect();
            // 定义BufferedReader输入流来读取URL的响应数据
            in = new BufferedReader(new InputStreamReader(httpUrlConnection.getInputStream(), StandardCharsets.UTF_8));
            String line;
            while ((line = in.readLine()) != null) {
                response += line;
            }
            // 获得URL的响应状态码
            status = Integer.toString(httpUrlConnection.getResponseCode());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                throw ex;
            }
        }
        if (!status.startsWith("2")) {
            throw new Exception("请求结果错误");
        }
        return response;
    }

    /**
     * EntityPost请求
     *
     * @param @param  postUrl
     * @param @param  param
     * @param @return
     * @return String
     * @throws IOException
     * @throws
     */
    public static String doEntityPost(String postUrl, Map<String, Object> param, Map<String, Object> property) throws Exception {
        List<NameValuePair> parmeters = new ArrayList<NameValuePair>();
        // 封装发送的请求参数
        if (param != null) {
            for (Entry<String, Object> map : param.entrySet()) {
                parmeters.add(new BasicNameValuePair(map.getKey(), map.getValue().toString()));
            }
        }
        CloseableHttpClient httpClient = HttpClients.createDefault();
        URI uri = new URIBuilder(postUrl).build();
        HttpPost httpPost = new HttpPost(uri);
        if (property != null) {
            for (Entry<String, Object> map : property.entrySet()) {
                httpPost.addHeader(map.getKey(), map.getValue().toString());
            }
        }
        httpPost.addHeader("charset", "UTF-8");
        httpPost.setHeader("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
        httpPost.addHeader("connection", "Keep-Alive");
        httpPost.addHeader("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
        httpPost.addHeader("accept", "*/*");
        RequestConfig config = RequestConfig
                .custom()
                .setConnectTimeout(TIMEOUT)
                .setConnectionRequestTimeout(TIMEOUT)
                .setSocketTimeout(TIMEOUT)
                .build();
        httpPost.setConfig(config);
        // 定义一个Post表单
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(parmeters, "utf-8");
        // 将表单放入到httpPost中
        httpPost.setEntity(entity);
        // 使用httpClient发送httppost请求
        CloseableHttpResponse response = null;
        try {
            // 请求后获得响应结果
            response = httpClient.execute(httpPost);
            // 如果请求返回状态码是200 说明请求成功
            if ((response.getStatusLine().getStatusCode() + "").startsWith("2")) {
                // 从response中取得数据
                String json = EntityUtils.toString(response.getEntity(), "utf-8");
                return json;
            } else {
                throw new Exception("请求结果错误");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 如果响应不等于null就关流
            if (response != null) {
                response.close();
            }
            // 关闭http请求
            httpClient.close();
        }
        return "";
    }

    /**
     * Post请求
     *
     * @param @param  postUrl
     * @param @param  param
     * @param @return
     * @return String
     * @throws IOException
     * @throws
     */
    public static String doPost(String postUrl, Map<String, Object> param, Map<String, Object> property) throws Exception {
        // 封装发送的请求参数
        StringBuffer buffer = new StringBuffer();
        if (param != null) {
            int x = 0;
            for (Entry<String, Object> map : param.entrySet()) {
                buffer.append(map.getKey()).append("=").append(map.getValue().toString());
                if (x != param.size() - 1) {
                    buffer.append("&");
                }
                x++;
            }
        }
        URL url = new URL(postUrl);
        URLConnection urlConnection = url.openConnection();
        HttpURLConnection httpUrlConnection = (HttpURLConnection) urlConnection;
        httpUrlConnection.setConnectTimeout(TIMEOUT);
        httpUrlConnection.setUseCaches(false);// 设置不要缓存
        httpUrlConnection.setRequestMethod("POST");
        // 设置请求头属性参数
        httpUrlConnection.setRequestProperty("charset", "UTF-8");
        httpUrlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        // httpUrlConnection.setRequestProperty("Content-Type","application/json");
        httpUrlConnection.setRequestProperty("connection", "Keep-Alive");
        httpUrlConnection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
        httpUrlConnection.setRequestProperty("accept", "*/*");
        if (property != null) {
            for (Entry<String, Object> map : property.entrySet()) {
                httpUrlConnection.setRequestProperty(map.getKey(), map.getValue().toString());
            }
        }
        // 发送POST请求必须设置如下两行
        httpUrlConnection.setDoOutput(true);
        httpUrlConnection.setDoInput(true);
        StringBuilder response = new StringBuilder();// 响应内容
        String status = "";// 响应状态
        PrintWriter out = null;
        BufferedReader in = null;
        try {
            // 获取URLConnection对象对应的输出流
            out = new PrintWriter(httpUrlConnection.getOutputStream());
            // 发送请求参数
            if (buffer.toString().length() > 0) {
                out.print(buffer);
            }
            // flush输出流的缓冲
            out.flush();
            httpUrlConnection.connect();
            // 定义BufferedReader输入流来读取URL的响应数据
            in = new BufferedReader(new InputStreamReader(httpUrlConnection.getInputStream(), StandardCharsets.UTF_8));
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            // 获得URL的响应状态码
            status = Integer.toString(httpUrlConnection.getResponseCode());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                throw ex;
            }
        }
        if (!status.startsWith("2")) {
            throw new Exception("请求结果错误");
        }
        return response.toString();
    }

    /**
     * Get请求
     *
     * @param getUrl
     * @param param
     * @param property
     * @return
     * @throws Exception
     */
    public static String doGet(String getUrl, Map<String, Object> param, Map<String, Object> property) throws Exception {
        System.out.println("request Url : " + getUrl);
        System.out.println("request Params : " + (param == null ? "{}" : param.toString()));
        // 封装发送的请求参数
        StringBuilder buffer = new StringBuilder();
        if (param != null) {
            buffer.append("?");
            int x = 0;
            for (Entry<String, Object> map : param.entrySet()) {
                buffer.append(map.getKey()).append("=").append(URLEncoder.encode(map.getValue().toString(), "UTF-8"));
                if (x != param.size() - 1) {
                    buffer.append("&");
                }
                x++;
            }
        }
        String urlPath = getUrl + buffer.toString();
        URL url = new URL(urlPath);
        URLConnection urlConnection = url.openConnection();
        HttpURLConnection httpUrlConnection = (HttpURLConnection) urlConnection;
        httpUrlConnection.setConnectTimeout(TIMEOUT);
        httpUrlConnection.setUseCaches(false);// 设置不要缓存
        httpUrlConnection.setRequestMethod("GET");
        // 设置请求头属性参数
        httpUrlConnection.setRequestProperty("charset", "UTF-8");
        httpUrlConnection.setRequestProperty("Content-Type", "application/json");
        httpUrlConnection.setRequestProperty("connection", "Keep-Alive");
        httpUrlConnection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
        httpUrlConnection.setRequestProperty("accept", "*/*");
        if (property != null) {
            for (Entry<String, Object> map : property.entrySet()) {
                httpUrlConnection.setRequestProperty(map.getKey(), map.getValue().toString());
            }
        }
        StringBuilder response = new StringBuilder();// 响应内容
        String status = "";// 响应状态6
        PrintWriter out = null;
        BufferedReader in = null;
        try {
            // httpUrlConnection.connect();
            // 定义BufferedReader输入流来读取URL的响应数据
            in = new BufferedReader(new InputStreamReader(httpUrlConnection.getInputStream(), StandardCharsets.UTF_8));
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            // 获得URL的响应状态码
            status = Integer.toString(httpUrlConnection.getResponseCode());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        if (!status.startsWith("2")) {
            throw new Exception("请求结果错误");
        }
        return response.toString();
    }


}
