package com.fqxd.gftools.features.proxy;

import org.json.JSONException;
import org.json.JSONObject;

public class ProxyConfig {
    private String Package;
    private String Address;
    private String Port;
    private Boolean Enabled;

    public void setPackage(String r0) { this.Package = r0; }
    public void  setAddress(String r1) { this.Address = r1; }
    public void setPort(String r2) { this.Port = r2; }
    public void setEnabled(Boolean r3) { this.Enabled = r3; }

    public String getPackage() { return this.Package; }
    public String getAddress() { return this.Address; }
    public String getPort() { return this.Port; }
    public Boolean getEnabled() { return this.Enabled; }

    public static ProxyConfig getProxyConfigFromJson(JSONObject r4) throws JSONException {
        ProxyConfig r5 = new ProxyConfig();
        r5.setPackage(r4.getString("package"));
        r5.setAddress(r4.getString("address"));
        r5.setPort(r4.getString("port"));
        r5.setEnabled(r4.getBoolean("enabled"));
        return r5;
    }

    public static JSONObject getJsonFromProxyConfig(ProxyConfig r6) throws JSONException{
        JSONObject r7 = new JSONObject();
        r7.put("package",r6.getPackage());
        r7.put("address",r6.getAddress());
        r7.put("port",r6.getPort());
        r7.put("enabled",r6.getEnabled());
        return r7;
    }
}
