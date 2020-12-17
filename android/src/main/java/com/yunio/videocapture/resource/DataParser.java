package com.yunio.videocapture.resource;

import com.google.gson.Gson;

import org.json.JSONObject;

import java.lang.reflect.Type;

public class DataParser {

	public final static String TAG = "DataParser";
	private static Gson mGson = new Gson();

	public final static <T> T parser(String jsonStr, Class<T> classOfT) {
		try {
			T t = mGson.fromJson(jsonStr, classOfT);
			handleParseComplete(t);
			return t;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public final static <T> T parser(String jsonStr, Type typeOfT) {
		try {
			T t = mGson.fromJson(jsonStr, typeOfT);
			handleParseComplete(t);
			return t;
		} catch (Exception e) {
			e.printStackTrace();
		}
//		catch (java.lang.StackOverflowError e) {
//			System.out.println(e);
//			//LogUtils.d(TAG, "parser error:%s", e);
//		}
		return null;
	}

	private static <T> void handleParseComplete(T t) {
		if (t instanceof IDataFromServer) {
			((IDataFromServer) t).onParseComplete();
		}
	}

	public final static String toJsonString(Object object) {
		return mGson.toJson(object);
	}

	public final static int toIntByKey(String jsonStr, String key) {
		return toIntByKey(jsonStr, key, 0);
	}

	public final static int toIntByKey(String jsonStr, String key, int defaultValue) {
		try {
			JSONObject json = new JSONObject(jsonStr);
			return json.optInt(key);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return defaultValue;
	}

	public final static String toStringByKey(String jsonStr, String key) {
		try {
			JSONObject json = new JSONObject(jsonStr);
			return json.optString(key);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	public final static long toLongByKey(String jsonStr, String key, long defaultValue) {
		try {
			JSONObject json = new JSONObject(jsonStr);
			return json.optLong(key);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return defaultValue;
	}

	public final static boolean toBooleanByKey(String jsonStr, String key, boolean defaultValue) {
		try {
			JSONObject json = new JSONObject(jsonStr);
			return json.optBoolean(key);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return defaultValue;
	}

	public static Gson getGson() {
		return mGson;
	}
}
