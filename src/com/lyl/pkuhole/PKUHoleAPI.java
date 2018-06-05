package com.lyl.pkuhole;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lyl.pkuhole.exception.PKUHoleException;
import com.lyl.pkuhole.model.Comment;
import com.lyl.pkuhole.model.Topic;
import com.lyl.pkuhole.model.TopicType;
import com.lyl.pkuhole.model.User;
import com.lyl.pkuhole.utils.TopicTypeDeserializer;

public class PKUHoleAPI {

	public static final String PKU_HOLE_HOST = "www.pkuhelper.com";
	private static final String PKU_HOLE_LOGIN_PATH = "/services/login/login.php";
	private static final String PKU_HOLE_API_PATH = "/services/pkuhole/api.php";
	public static final String PKU_HOLE_PIC_PATH = "/services/pkuhole/images/";
	private static final String USER_AGENT = "okhttp/3.4.1";

	private static final JsonParser parser = new JsonParser();
	private static final Gson gson = new GsonBuilder().registerTypeAdapter(TopicType.class, new TopicTypeDeserializer())
			.create();

	/**
	 * Perform HTTP GET method with PKUHole server.
	 * 
	 * @param path
	 *            URL path
	 * @param args
	 *            URL arguments
	 * @return JSON Object received
	 * @throws PKUHoleException
	 */
	private static JsonElement api(String path, List<NameValuePair> args) throws PKUHoleException {
		URL url;
		try {
			url = new URIBuilder().setScheme("http").setHost(PKU_HOLE_HOST).setPath(path).addParameters(args).build()
					.toURL();
		} catch (MalformedURLException | URISyntaxException e) {
			throw new PKUHoleException("Bad URL: " + e.getMessage());
		}
		try {

			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Host", PKU_HOLE_HOST);
			conn.setRequestProperty("User-Agent", USER_AGENT);

			int status = conn.getResponseCode();
			if (status == 200) {
				InputStreamReader response = new InputStreamReader(conn.getInputStream());
				BufferedReader reader = new BufferedReader(response);
				String line;
				StringBuilder sb = new StringBuilder();
				while ((line = reader.readLine()) != null) {
					sb.append(line);
				}
				return parser.parse(sb.toString());
			} else {
				throw new PKUHoleException(String.format("��������ʧ��(%d): %s", status, conn.getResponseMessage()));
			}

		} catch (IOException e) {
			throw new PKUHoleException("���粻����");
		}

	}

	/**
	 * Perform HTTP POST method with PKUHole server.
	 * 
	 * @param path
	 *            URL path
	 * @param args
	 *            URL arguments
	 * @param content
	 *            POST content
	 * @return JSON Object received
	 * @throws PKUHoleException
	 */
	private static JsonElement aqi(String path, List<NameValuePair> args, List<NameValuePair> content)
			throws PKUHoleException {
		URL url;
		try {
			url = new URIBuilder().setScheme("http").setHost(PKU_HOLE_HOST).setPath(path).addParameters(args).build()
					.toURL();
		} catch (MalformedURLException | URISyntaxException e) {
			throw new PKUHoleException("Bad URL: " + e.getMessage());
		}
		try {
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Host", PKU_HOLE_HOST);
			conn.setRequestProperty("User-Agent", USER_AGENT);
			conn.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
			conn.setDoOutput(true);
			conn.setUseCaches(false);

			OutputStream out = conn.getOutputStream();
			out.write(NVP2Bytes(content));
			out.flush();
			out.close();

			int status = conn.getResponseCode();
			if (status == 200) {
				InputStreamReader response = new InputStreamReader(conn.getInputStream());
				BufferedReader reader = new BufferedReader(response);
				String line;
				StringBuilder sb = new StringBuilder();
				while ((line = reader.readLine()) != null) {
					sb.append(line);
				}
				return parser.parse(sb.toString());
			} else {
				throw new PKUHoleException(String.format("��������ʧ��(%d): %s", status, conn.getResponseMessage()));
			}
		} catch (IOException e) {
			throw new PKUHoleException("���粻����");
		}
	}

	private static NameValuePair pair(String name, String value) {
		return new BasicNameValuePair(name, value);
	}

	private static byte[] NVP2Bytes(List<NameValuePair> content) {
		StringBuilder sb = new StringBuilder();
		for (NameValuePair nvp : content) {
			sb.append(nvp.getName()).append("=").append(nvp.getValue()).append("&");
		}
		return sb.substring(0, sb.length() - 1).getBytes(Charset.forName("utf-8"));
	}

	/**
	 * ��ȡ�ض�ҳ����
	 * 
	 * @param page
	 *            ҳ��
	 * @return �����б�
	 * @throws PKUHoleException
	 */
	public static Topic[] getTopics(int page) throws PKUHoleException {
		List<NameValuePair> nvp = Arrays.asList(pair("action", "getlist"), pair("p", page + ""));
		JsonObject json = api(PKU_HOLE_API_PATH, nvp).getAsJsonObject();
		if (json.get("code").getAsInt() != 0) {
			throw new PKUHoleException(json.get("msg").getAsString());
		}
		JsonElement data = json.get("data");
		if (data == null)
			return null;
		else
			return gson.fromJson(data, Topic[].class);
	}

	/**
	 * �õ���������
	 * 
	 * @param pid
	 *            ������
	 * @return ��������
	 * @throws PKUHoleException
	 */
	public static Topic getSingleTopic(int pid) throws PKUHoleException {
		List<NameValuePair> nvp = Arrays.asList(pair("action", "getone"), pair("pid", pid + ""));
		JsonObject json = api(PKU_HOLE_API_PATH, nvp).getAsJsonObject();
		if (json.get("code").getAsInt() != 0) {
			throw new PKUHoleException(json.get("msg").getAsString());
		}
		JsonElement data = json.get("data");
		if (data == null)
			return null;
		else
			return gson.fromJson(data, Topic.class);
	}

	/**
	 * ��ȡ��������
	 * 
	 * @param pid
	 *            ������
	 * @return �����б�
	 * @throws PKUHoleException
	 */
	public static Comment[] getComments(int pid) throws PKUHoleException {
		List<NameValuePair> nvp = Arrays.asList(pair("action", "getcomment"), pair("pid", pid + ""));
		JsonObject json = api(PKU_HOLE_API_PATH, nvp).getAsJsonObject();
		if (json.get("code").getAsInt() != 0) {
			throw new PKUHoleException(json.get("msg").getAsString());
		}
		JsonElement data = json.get("data");
		if (data == null)
			return null;
		else
			return gson.fromJson(data, Comment[].class);
	}

	/**
	 * �����ض��ؼ��ֵ�����
	 * 
	 * @param keywords
	 *            �ؼ���
	 * @param pageSize
	 *            �������
	 * @return �����б�
	 * @throws PKUHoleException
	 */
	public static Topic[] searchTopics(String keywords, int pageSize) throws PKUHoleException {
		List<NameValuePair> nvp = Arrays.asList(pair("action", "search"));
		List<NameValuePair> content = Arrays.asList(pair("keywords", keywords), pair("pagesize", pageSize + ""));
		JsonObject json = aqi(PKU_HOLE_API_PATH, nvp, content).getAsJsonObject();
		if (json.get("code").getAsInt() != 0) {
			throw new PKUHoleException(json.get("msg").getAsString());
		}
		JsonElement data = json.get("data");
		if (data == null)
			return null;
		else
			return gson.fromJson(data, Topic[].class);
	}

	// The following APIs are user-specified.

	/**
	 * ��¼
	 * 
	 * @param uid
	 *            ѧ��
	 * @param password
	 *            ����
	 * @return �û���Ϣ
	 * @throws PKUHoleException
	 */
	public static User login(String uid, String password) throws PKUHoleException {
		List<NameValuePair> nvp = Arrays.asList(pair("platform", "PC"));
		List<NameValuePair> content = Arrays.asList(pair("uid", uid), pair("password", password));
		JsonObject json = aqi(PKU_HOLE_LOGIN_PATH, nvp, content).getAsJsonObject();
		if (json.get("code").getAsInt() != 0)
			throw new PKUHoleException(json.get("msg").getAsString());
		else
			return gson.fromJson(json, User.class);
	}

	/**
	 * ��ȡ��ע����
	 * 
	 * @param token
	 *            token
	 * @return ��ע�����б�
	 * @throws PKUHoleException
	 */
	public static Topic[] getAttentionTopics(String token) throws PKUHoleException {
		List<NameValuePair> nvp = Arrays.asList(pair("action", "getattention"));
		List<NameValuePair> content = Arrays.asList(pair("token", token));
		JsonObject json = aqi(PKU_HOLE_API_PATH, nvp, content).getAsJsonObject();
		if (json.get("code").getAsInt() != 0) {
			throw new PKUHoleException(json.get("msg").getAsString());
		}
		JsonElement data = json.get("data");
		if (data == null)
			return null;
		else
			return gson.fromJson(data, Topic[].class);
	}

	/**
	 * ������������
	 * 
	 * @param token
	 *            token
	 * @param text
	 *            ��������
	 * @return �ɹ������������ţ�ʧ�ܣ��׳��쳣
	 * @throws PKUHoleException
	 */
	public static int sendTextPost(String token, String text) throws PKUHoleException {
		List<NameValuePair> nvp = Arrays.asList(pair("action", "dopost"));
		List<NameValuePair> content = Arrays.asList(pair("token", token), pair("type", "text"), pair("text", text));
		JsonObject json = aqi(PKU_HOLE_API_PATH, nvp, content).getAsJsonObject();
		if (json.get("code").getAsInt() != 0)
			throw new PKUHoleException(json.get("msg").getAsString());
		else
			return json.get("data").getAsInt();
	}

	/**
	 * ����ͼƬ����: ���ǵ����ݵ������ԣ�û����aqi�ӿڣ����粿�ִ�����д
	 * 
	 * @param token
	 *            token
	 * @param text
	 *            ��������
	 * @param image
	 * @throws PKUHoleException
	 */
	public static void sendImagePost(String token, String text, String image) throws PKUHoleException {
		URL url;
		try {
			url = new URL("http://" + PKU_HOLE_HOST + PKU_HOLE_API_PATH + "?action=dopost");
		} catch (MalformedURLException e) {
			throw new PKUHoleException("Bad URL: " + e.getMessage());
		}
		List<NameValuePair> content = Arrays.asList(pair("token", token), pair("type", "image"), pair("text", text));
		JsonObject json;
		try {
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Host", PKU_HOLE_HOST);
			conn.setRequestProperty("User-Agent", USER_AGENT);
			conn.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
			conn.setDoOutput(true);
			conn.setUseCaches(false);

			OutputStream out = conn.getOutputStream();
			out.write(NVP2Bytes(content));
			out.write("&data=".getBytes());
			out.write(image.getBytes());

			out.flush();
			out.close();

			int status = conn.getResponseCode();
			if (status == 200) {
				InputStreamReader response = new InputStreamReader(conn.getInputStream());
				BufferedReader reader = new BufferedReader(response);
				String line;
				StringBuilder sb = new StringBuilder();
				while ((line = reader.readLine()) != null) {
					sb.append(line);
				}
				json = (JsonObject) parser.parse(sb.toString());
			} else {
				throw new PKUHoleException(String.format("��������ʧ��(%d): %s", status, conn.getResponseMessage()));
			}
		} catch (IOException e) {
			throw new PKUHoleException("���粻����");
		}
		if (json.get("code").getAsInt() != 0)
			throw new PKUHoleException(json.get("msg").getAsString());
	}

	/**
	 * ��������
	 * 
	 * @param token
	 *            token
	 * @param pid
	 *            ������
	 * @param text
	 *            ��������
	 * @return �ɹ������������ţ�ʧ�ܣ��׳��쳣
	 * @throws PKUHoleException
	 */
	public static long sendComment(String token, long pid, String text) throws PKUHoleException {
		List<NameValuePair> nvp = Arrays.asList(pair("action", "docomment"));
		List<NameValuePair> content = Arrays.asList(pair("token", token), pair("pid", pid + ""), pair("text", text));
		JsonObject json = aqi(PKU_HOLE_API_PATH, nvp, content).getAsJsonObject();
		if (json.get("code").getAsInt() != 0)
			throw new PKUHoleException(json.get("msg").getAsString());
		else
			return json.get("data").getAsLong();
	}

	/**
	 * ����������ע
	 * 
	 * @param token
	 *            token
	 * @param pid
	 *            ������
	 * @param attention
	 *            �Ƿ��ע
	 * @throws PKUHoleException
	 */
	public static void setAttention(String token, long pid, boolean attention) throws PKUHoleException {
		List<NameValuePair> nvp = Arrays.asList(pair("action", "attention"));
		List<NameValuePair> content = Arrays.asList(pair("token", token), pair("pid", pid + ""),
				pair("switch", attention ? "1" : "0"));
		JsonObject json = aqi(PKU_HOLE_API_PATH, nvp, content).getAsJsonObject();
		if (json.get("code").getAsInt() != 0)
			throw new PKUHoleException(json.get("msg").getAsString());
	}

	/**
	 * �ٱ�����
	 * 
	 * @param token
	 *            token
	 * @param pid
	 *            ������
	 * @param reason
	 *            �ٱ�����
	 * @return �����Ƿ�ɹ�
	 * @throws PKUHoleException
	 */
	public static boolean report(String token, long pid, String reason) throws PKUHoleException {
		List<NameValuePair> nvp = Arrays.asList(pair("action", "report"));
		List<NameValuePair> content = Arrays.asList(pair("token", token), pair("pid", pid + ""),
				pair("reason", reason));
		JsonObject json = aqi(PKU_HOLE_API_PATH, nvp, content).getAsJsonObject();
		return json.get("code").getAsInt() == 0;
	}
}
