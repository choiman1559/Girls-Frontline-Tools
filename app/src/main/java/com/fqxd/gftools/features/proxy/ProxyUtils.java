package com.fqxd.gftools.features.proxy;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class ProxyUtils {
    public static void setProxy(ProxyConfig r0) throws IOException, InterruptedException {
        String cmd = "su -c settings put global http_proxy " + r0.getAddress() + ":" + r0.getPort();
        Runtime.getRuntime().exec(cmd).waitFor();
    }

    public static void undoProxy() throws IOException, InterruptedException {
        String cmd = "su -c settings put global http_proxy :0";
        Runtime.getRuntime().exec(cmd).waitFor();
    }

    public static JSONObject getJsonFromPrefs(String Package,Context context) throws JSONException {
        SharedPreferences prefs = context.getSharedPreferences("MainActivity", Context.MODE_PRIVATE);
        JSONArray arr = new JSONArray(prefs.getString("proxy_data","[]"));
        for(int i = 0;i < arr.length();i++) {
            JSONObject obj = arr.getJSONObject(i);
            if(obj.getString("package").equals(Package)) return obj;
        }
        return null;
    }

    public static void saveJsonInPrefs(JSONObject obj,Context context) throws JSONException {
        SharedPreferences prefs = context.getSharedPreferences("MainActivity", Context.MODE_PRIVATE);
        JSONArray arr = new JSONArray(prefs.getString("proxy_data","[]"));
        for(int i = 0;i < arr.length();i++) {
            JSONObject o = arr.getJSONObject(i);
            if(o.getString("package").equals(obj.getString("package"))) {
                arr.remove(i);
                break;
            }
        }
        arr.put(obj);
        prefs.edit().putString("proxy_data",arr.toString()).apply();
    }
}