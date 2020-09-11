package com.fqxd.gftools.features.proxy;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;

public class ProxyUtils {
    public static void setProxy(ProxyConfig r0) throws IOException, InterruptedException {
        String cmd = "settings put global http_proxy " + r0.getAddress() + ":" + r0.getPort();
        Process process = Runtime.getRuntime().exec("su");
        DataOutputStream dos = new DataOutputStream(process.getOutputStream());
        dos.writeBytes(cmd + "\n");
        dos.writeBytes("exit\n");
        dos.flush();
        dos.close();
        process.waitFor();
    }

    public static void setPacProxy(PacProxyConfig r0) throws IOException, InterruptedException {
        String cmd = "settings put global global_proxy_pac_url " + r0.getAddress();
        Process process = Runtime.getRuntime().exec("su");
        DataOutputStream dos = new DataOutputStream(process.getOutputStream());
        dos.writeBytes(cmd + "\n");
        dos.writeBytes("exit\n");
        dos.flush();
        dos.close();
        process.waitFor();
    }

    public static void undoProxy() throws IOException, InterruptedException {
        Process process = Runtime.getRuntime().exec("su");
        DataOutputStream dos = new DataOutputStream(process.getOutputStream());
        dos.writeBytes("settings put global http_proxy :0\n");
        dos.writeBytes("exit\n");
        dos.flush();
        dos.close();
        process.waitFor();
    }

    public static void undoPacProxy() throws IOException, InterruptedException {
        String cmd = "settings put global global_proxy_pac_url :0";
        Process process = Runtime.getRuntime().exec("su");
        DataOutputStream dos = new DataOutputStream(process.getOutputStream());
        dos.writeBytes(cmd + "\n");
        dos.writeBytes("exit\n");
        dos.flush();
        dos.close();
        process.waitFor();
    }

    public static JSONObject getProxyJsonFromPrefs(String Package,Context context) throws JSONException {
        SharedPreferences prefs = context.getSharedPreferences("com.fqxd.gftools_preferences", Context.MODE_PRIVATE);
        JSONArray arr = new JSONArray(prefs.getString("proxy_data","[]"));
        for(int i = 0;i < arr.length();i++) {
            JSONObject obj = arr.getJSONObject(i);
            if(obj.getString("package").equals(Package)) return obj;
        }
        return null;
    }

    public static void saveProxyJsonInPrefs(JSONObject obj,Context context) throws JSONException {
        SharedPreferences prefs = context.getSharedPreferences("com.fqxd.gftools_preferences", Context.MODE_PRIVATE);
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

    public static JSONObject getPacProxyJsonFromPrefs(String Package,Context context) throws JSONException {
        SharedPreferences prefs = context.getSharedPreferences("com.fqxd.gftools_preferences", Context.MODE_PRIVATE);
        JSONArray arr = new JSONArray(prefs.getString("pac_proxy_data","[]"));
        for(int i = 0;i < arr.length();i++) {
            JSONObject obj = arr.getJSONObject(i);
            if(obj.getString("package").equals(Package)) return obj;
        }
        return null;
    }

    public static void savePacProxyJsonInPrefs(JSONObject obj,Context context) throws JSONException {
        SharedPreferences prefs = context.getSharedPreferences("com.fqxd.gftools_preferences", Context.MODE_PRIVATE);
        JSONArray arr = new JSONArray(prefs.getString("pac_proxy_data","[]"));
        for(int i = 0;i < arr.length();i++) {
            JSONObject o = arr.getJSONObject(i);
            if(o.getString("package").equals(obj.getString("package"))) {
                arr.remove(i);
                break;
            }
        }
        arr.put(obj);
        prefs.edit().putString("pac_proxy_data",arr.toString()).apply();
    }
}