package com.fqxd.gftools.features.proxy;

import org.json.JSONException;
import org.json.JSONObject;

public class PacProxyConfig {
    private String Package;
    private String Address;
    private Boolean Enabled;

    public void setPackage(String r0) { this.Package = r0; }
    public void  setAddress(String r1) { this.Address = r1; }
    public void setEnabled(Boolean r3) { this.Enabled = r3; }

    public String getPackage() { return this.Package; }
    public String getAddress() { return this.Address; }
    public Boolean getEnabled() { return this.Enabled; }

    public static PacProxyConfig getProxyConfigFromJson(JSONObject r4) throws JSONException {
        PacProxyConfig r5 = new PacProxyConfig();
        r5.setPackage(r4.getString("package"));
        r5.setAddress(r4.getString("address"));
        r5.setEnabled(r4.getBoolean("enabled"));
        return r5;
    }

    public static JSONObject getJsonFromProxyConfig(PacProxyConfig r6) throws JSONException{
        JSONObject r7 = new JSONObject();
        r7.put("package",r6.getPackage());
        r7.put("address",r6.getAddress());
        r7.put("enabled",r6.getEnabled());
        return r7;
    }
}
