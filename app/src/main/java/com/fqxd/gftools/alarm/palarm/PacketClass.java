package com.fqxd.gftools.alarm.palarm;

import android.content.Context;

import com.fqxd.gftools.vpn.utils.VpnServiceHelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class PacketClass {

    public void setVpn(Context context) { VpnServiceHelper.changeVpnRunningStatus(context, true); }

    public void endVpn(Context context) {
        VpnServiceHelper.changeVpnRunningStatus(context, false);
    }

    public void runVpn(Context context) {
        VpnServiceHelper.startVpnService(context);
    }

    public Boolean isInclude(File file,String string) {
        String line = null;
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));

            while ((line = bufferedReader.readLine()) != null) {
                if (line.contains(string)) return true;
            }
            bufferedReader.close();
        } catch (Exception e) { }
        return false;
    }
}
