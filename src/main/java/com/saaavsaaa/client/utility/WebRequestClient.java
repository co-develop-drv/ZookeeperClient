package com.saaavsaaa.client.utility;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

public class WebRequestClient {
	private static CookieStore cs = new BasicCookieStore();
	private static ThreadLocal<Header[]> headerHolder = new ThreadLocal<>();
	
	public static String get(String url) throws Exception {
		return opera(url, null, true);
	}

	public static String get(String url, String loginToken, String loginUserID) throws Exception {
		setCookie(loginToken, loginUserID);
		return opera(url, null, true);
	}
	
	public static String get(String url, Map<String, String> cookieKVs, Map<String, String> headerKVs) throws Exception {
		setCookies(cookieKVs);
		setHeaders(headerKVs);
		return opera(url, null, true);
	}
	
	public static String post(String url, String loginToken, String loginUserID,
									  HttpEntity paras, Map<String, String> headerKVs) throws Exception {
		setCookie(loginToken, loginUserID);
		setHeaders(headerKVs);
		return opera(url, paras, false);
	}
	
	public static String post(String url, String loginToken, String loginUserID,
									  HttpEntity paras) throws Exception {
		setCookie(loginToken, loginUserID);
		return opera(url, paras, false);
	}
	
	public static String postWithCookie(String url, String cookieKey, String cookieValue,
									  HttpEntity paras) throws Exception {
		Cookie[] cookies = new Cookie[1];
		cookies[0] = new BasicClientCookie(cookieKey, cookieValue);
		setCookies(cookies);
		return opera(url, paras, false);
	}
	
	private static void setHeaders(Map<String, String> headerKVs){
		Header[] headers = new Header[headerKVs.size()];
		int i = 0;
		for (Map.Entry<String, String> entry: headerKVs.entrySet()) {
			headers[i] = new BasicHeader(entry.getKey(), entry.getValue());
			i++;
		}
		headerHolder.set(headers);
	}

	private static void setCookie(String loginToken, String loginUserID){
		Cookie[] cookies = new Cookie[2];
		cookies[0] = new BasicClientCookie(Properties.INSTANCE.getSessionToken(), loginToken);
		cookies[1] = new BasicClientCookie(Properties.INSTANCE.getSessionUser(), loginUserID);
		setCookies(cookies);
	}
	
	private static void setCookies(Map<String, String> cookieKVs) {
		Cookie[] cookies = new Cookie[cookieKVs.size()];
		int i = 0;
		for (Map.Entry<String, String> entry: cookieKVs.entrySet()) {
			cookies[i] = new BasicClientCookie(entry.getKey(), entry.getValue());
			i++;
		}
		setCookies(cookies);
	}
	
	private static void setCookies(Cookie[] cookies){
		HttpContext localContext = new BasicHttpContext();
		for (Cookie one : cookies) {
			cs.addCookie(one);
		}
		localContext.setAttribute(HttpClientContext.COOKIE_STORE, cs);
	}

	private static String opera(String url, HttpEntity paras, boolean isGet) throws Exception {
//		System.out.println("----------------------------------------");
//		System.out.println("----------------------------------------");

		String logPath = url;

		//DefaultHttpClient httpclient = new DefaultHttpClient(); @deprecated
		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
		cm.setMaxTotal(200);
		cm.setDefaultMaxPerRoute(20);
		CloseableHttpClient httpclient = HttpClients.custom().setConnectionManager(cm).build();

		String cookieStr = "";
		List<Cookie> list = cs.getCookies();
		for (Cookie cookie : list) {
			cookieStr += cookie.getName() + "=" + cookie.getValue() + ";";
		}

		CloseableHttpResponse response;
		if (isGet){
			response = getResponse(logPath, cookieStr, httpclient);
		} else {
			response = postResponse(logPath, cookieStr, paras, httpclient);
		}

//		response.setEntity(new UrlEncodedFormEntity(params));
		HttpEntity entity = response.getEntity();


		BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent(), "UTF-8"));
		String line = null;
		StringBuilder txt = new StringBuilder();
		while ((line = reader.readLine()) != null) {
			txt.append(line);
		}
		return txt.toString();
	}
	
	private static Header[] buildHeaders(String cookieStr) throws IOException {
		Header[] headers = headerHolder.get();
		if (headers == null) {
			headers = new Header[1];
			headers[0] = new BasicHeader("Cookie", cookieStr);
		}
		
		return headers;
	}
	
	private static HttpGet getHttpGet(String logPath, Header[] headers) throws IOException {
		// direction
		HttpGet httpget = new HttpGet(logPath);
		httpget.setHeaders(headers);
		return httpget;
	}

	private static CloseableHttpResponse getResponse(String logPath, String cookieStr, CloseableHttpClient httpclient) throws IOException {
		Header[] headers = buildHeaders(cookieStr);
		HttpGet httpget = getHttpGet(logPath, headers);
		CloseableHttpResponse response = httpclient.execute(httpget);
		return response;
	}

	private static CloseableHttpResponse postResponse(String logPath, String cookieStr, HttpEntity paras,
											 CloseableHttpClient httpclient) throws IOException {
		HttpPost httpPost = new HttpPost(logPath);
		
		Header[] headers = buildHeaders(cookieStr);
		httpPost.setHeaders(headers);
		
		if (paras != null) {
			System.out.print(paras + "\n");
			httpPost.setEntity(paras);
		}
		CloseableHttpResponse response = httpclient.execute(httpPost);
		return response;
	}
}
