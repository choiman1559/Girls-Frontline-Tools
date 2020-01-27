package com.fqxd.gftools.alarm.palarm;

import android.annotation.TargetApi;
import android.service.quicksettings.TileService;

import com.fqxd.gftools.ExceptionCatchClass;
import com.fqxd.gftools.vpn.utils.VpnServiceHelper;

@TargetApi(24)
public class TileServiceClass extends TileService{
    @Override
    public void onTileAdded() {
        super.onTileAdded();
        if(getSharedPreferences("ListAlarm",MODE_PRIVATE).getBoolean("isChecked",false) && !VpnServiceHelper.vpnRunningStatus()) getQsTile().setState(1);
        else getQsTile().setState(2);
        getQsTile().updateTile();
    }

    @Override
    public void onTileRemoved() {
        super.onTileRemoved();
    }

    @Override
    public void onClick() {
        if(getQsTile().getState() == 1) {
            PAlarmAddClass.context = TileServiceClass.this;
            getSharedPreferences("ListAlarm",MODE_PRIVATE).edit().putBoolean("isChecked", true).apply();
            getQsTile().setState(2);
            getQsTile().updateTile();
            new PacketClass().setVpn(TileServiceClass.this);
            new PacketClass().runVpn(TileServiceClass.this);
        } else if(getQsTile().getState() == 2){
            getSharedPreferences("ListAlarm",MODE_PRIVATE).edit().putBoolean("isChecked", false).apply();
            getQsTile().setState(1);
            getQsTile().updateTile();
            new PacketClass().endVpn(TileServiceClass.this);
        } else new ExceptionCatchClass().CatchException(TileServiceClass.this,new Exception("Tile State is not 1 or 2!"));
    }
}
