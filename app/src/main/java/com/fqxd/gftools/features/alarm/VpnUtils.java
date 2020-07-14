package com.fqxd.gftools.features.alarm;

import android.content.Context;

import com.github.megatronking.netbare.NetBare;
import com.github.megatronking.netbare.NetBareConfig;

public class VpnUtils {
    NetBare netBare;
    Context context;

    VpnUtils(Context context) {
        this.context = context;
        netBare = NetBare.get();
    }

    public void PrepareAndRunVpn() {
        netBare.prepare();
        String pkg = context.getPackageName();
        //netBare.start(NetBareConfig.defaultConfig());
        //NetBareConfig.defaultHttpConfig(new JKS(context,pkg,pkg.toCharArray(),pkg,pkg,pkg,pkg,pkg),));
    }

    public void StopVpn() {
        netBare.stop();
    }

    /*private List<HttpInterceptorFactory> iff() {
        List<HttpInjectInterceptor> iff = new ArrayList<>();
        iff().add(HttpInjectInterceptor.createFactory());
        return iff;
    }*/
}
