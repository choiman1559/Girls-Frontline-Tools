package com.fqxd.gftools;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Objects;

public class DeviceBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent)
    {
        if(Objects.equals(intent.getAction(),"android.intent.action.BOOT_COMPLETED")) {

        }
    }
}
