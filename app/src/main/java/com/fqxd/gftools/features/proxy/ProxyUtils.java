package com.fqxd.gftools.features.proxy;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.fqxd.gftools.Global;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;

public class ProxyUtils {
    public static void setProxy(ProxyConfig r0,Context ctx) {
        Settings.Global.putString(ctx.getContentResolver(),"http_proxy",r0.getAddress() + ":" + r0.getPort());
        Toast.makeText(ctx, "Http Proxy set to " + r0.getAddress() + ":" + r0.getPort(), Toast.LENGTH_SHORT).show();
    }

    public static void setPacProxy(PacProxyConfig r0, Context ctx) {
        Settings.Global.putString(ctx.getContentResolver(),"global_proxy_pac_url",r0.getAddress());
        Toast.makeText(ctx, "Http PAC Proxy set to " + r0.getAddress(), Toast.LENGTH_SHORT).show();
    }

    public static void undoProxy(Context ctx) {
        Settings.Global.putString(ctx.getContentResolver(),"http_proxy",":0");
    }

    public static void undoPacProxy(Context ctx) {
        Settings.Global.putString(ctx.getContentResolver(),"global_proxy_pac_url","");
    }

    @Nullable
    public static JSONObject getProxyJsonFromPrefs(String Package,Context context) throws JSONException {
        SharedPreferences prefs = context.getSharedPreferences(Global.Prefs, Context.MODE_PRIVATE);
        JSONArray arr = new JSONArray(prefs.getString("proxy_data","[]"));
        for(int i = 0;i < arr.length();i++) {
            JSONObject obj = arr.getJSONObject(i);
            if(obj.getString("package").equals(Package)) return obj;
        }
        return null;
    }

    public static void saveProxyJsonInPrefs(JSONObject obj,Context context) throws JSONException {
        SharedPreferences prefs = context.getSharedPreferences(Global.Prefs, Context.MODE_PRIVATE);
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

    @Nullable
    public static JSONObject getPacProxyJsonFromPrefs(String Package,Context context) throws JSONException {
        SharedPreferences prefs = context.getSharedPreferences(Global.Prefs, Context.MODE_PRIVATE);
        JSONArray arr = new JSONArray(prefs.getString("pac_proxy_data","[]"));
        for(int i = 0;i < arr.length();i++) {
            JSONObject obj = arr.getJSONObject(i);
            if(obj.getString("package").equals(Package)) return obj;
        }
        return null;
    }

    public static void savePacProxyJsonInPrefs(JSONObject obj,Context context) throws JSONException {
        SharedPreferences prefs = context.getSharedPreferences(Global.Prefs, Context.MODE_PRIVATE);
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