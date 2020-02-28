package com.fqxd.gftools.vpn.service;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ProxyInfo;
import android.net.Uri;
import android.net.VpnService;
import android.os.Build;
import android.os.Handler;
import android.os.ParcelFileDescriptor;

import com.fqxd.gftools.vpn.Packet;
import com.fqxd.gftools.vpn.ProxyConfig;
import com.fqxd.gftools.R;
import com.fqxd.gftools.vpn.UDPServer;
import com.fqxd.gftools.vpn.VPNLog;
import com.fqxd.gftools.vpn.http.HttpRequestHeaderParser;
import com.fqxd.gftools.vpn.nat.NatSession;
import com.fqxd.gftools.vpn.nat.NatSessionManager;
import com.fqxd.gftools.vpn.processparse.PortHostService;
import com.fqxd.gftools.vpn.proxy.TcpProxyServer;
import com.fqxd.gftools.vpn.tcpip.IPHeader;
import com.fqxd.gftools.vpn.tcpip.TCPHeader;
import com.fqxd.gftools.vpn.tcpip.UDPHeader;
import com.fqxd.gftools.vpn.utils.AppDebug;
import com.fqxd.gftools.vpn.utils.CommonMethods;
import com.fqxd.gftools.vpn.utils.DebugLog;
import com.fqxd.gftools.vpn.utils.ThreadProxy;
import com.fqxd.gftools.vpn.utils.TimeFormatUtil;
import com.fqxd.gftools.vpn.utils.VpnServiceHelper;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.fqxd.gftools.vpn.VPNConstants.VPN_SP_NAME;

/**
 * Created by zengzheying on 15/12/28.
 */
public class FirewallVpnService extends VpnService implements Runnable {
    public static final String ACTION_START_VPN = "com.fqxd.gftools.START_VPN";
    public static final String ACTION_CLOSE_VPN = "com.fqxd.gftools.roav.CLOSE_VPN";
    private static final String FACEBOOK_APP = "com.facebook.katana";
    private static final String YOUTUBE_APP = "com.google.android.youtube";
    private static final String GOOGLE_MAP_APP = "com.google.android.apps.maps";

    private static final String VPN_ADDRESS = "10.0.0.2"; // Only IPv4 support for now
    private static final String VPN_ROUTE = "0.0.0.0"; // Intercept everything
    private static final String GOOGLE_DNS_FIRST = "8.8.8.8";
    private static final String GOOGLE_DNS_SECOND = "8.8.4.4";
    private static final String AMERICA = "208.67.222.222";
    private static final String HK_DNS_SECOND = "205.252.144.228";
    private static final String CHINA_DNS_FIRST = "114.114.114.114";
    public static final String BROADCAST_VPN_STATE = "com.fqxd.gftools.localvpn.VPN_STATE";
    public static final String SELECT_PACKAGE_ID = "select_protect_package_id";
    private static final String TAG = "FirewallVpnService";
    private static int ID;
    private static int LOCAL_IP;
    private final UDPHeader mUDPHeader;
    private final ByteBuffer mDNSBuffer;
    private boolean IsRunning = false;
    private Thread mVPNThread;
    private ParcelFileDescriptor mVPNInterface;
    private TcpProxyServer mTcpProxyServer;
    private FileOutputStream mVPNOutputStream;

    private byte[] mPacket;
    private IPHeader mIPHeader;
    private TCPHeader mTCPHeader;
    private Handler mHandler;
    private ConcurrentLinkedQueue<Packet> udpQueue;
    private FileInputStream in;
    private UDPServer udpServer;
    private String selectPackage;
    public static final int MUTE_SIZE = 2560;
    private int mReceivedBytes;
    private int mSentBytes;
    public static long vpnStartTime;
    public static String lastVpnStartTimeFormat = null;
    private SharedPreferences sp;
    Context context;

    public FirewallVpnService() {
        ID++;
        mHandler = new Handler();
        mPacket = new byte[MUTE_SIZE];
        mIPHeader = new IPHeader(mPacket, 0);
        mTCPHeader = new TCPHeader(mPacket, 20);
        mUDPHeader = new UDPHeader(mPacket, 20);
        mDNSBuffer = ((ByteBuffer) ByteBuffer.wrap(mPacket).position(28)).slice();
        this.context = context;


        DebugLog.i("New VPNService(%d)\n", ID);
    }

    @Override
    public void onCreate() {
        DebugLog.i("VPNService(%s) created.\n", ID);
        sp = getSharedPreferences(VPN_SP_NAME, Context.MODE_PRIVATE);
        VpnServiceHelper.onVpnServiceCreated(this);
        mVPNThread = new Thread(this, "VPNServiceThread");
        mVPNThread.start();
        setVpnRunningStatus(true);
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        DebugLog.i("VPNService(%s) destroyed.\n", ID);
        if (mVPNThread != null) {
            mVPNThread.interrupt();
        }
        VpnServiceHelper.onVpnServiceDestroy();
        super.onDestroy();
    }


    private void runVPN() throws Exception {
        this.mVPNInterface = establishVPN();
        startStream();
    }

    private void startStream() throws Exception {
        int size = 0;
        mVPNOutputStream = new FileOutputStream(mVPNInterface.getFileDescriptor());
        in = new FileInputStream(mVPNInterface.getFileDescriptor());
        while (size != -1 && IsRunning) {
            boolean hasWrite = false;
            size = in.read(mPacket);
            if (size > 0) {
                if (mTcpProxyServer.Stopped) {
                    in.close();
                    throw new Exception("LocalServer stopped.");
                }
                hasWrite = onIPPacketReceived(mIPHeader, size, context);

            }
            if (!hasWrite) {
                Packet packet = udpQueue.poll();
                if (packet != null) {
                    ByteBuffer bufferFromNetwork = packet.backingBuffer;
                    bufferFromNetwork.flip();
                    mVPNOutputStream.write(bufferFromNetwork.array());

                }
            }
            Thread.sleep(10);
        }
        in.close();
        disconnectVPN();
    }

    boolean onIPPacketReceived(IPHeader ipHeader, int size, Context context) throws IOException {
        boolean hasWrite = false;

        switch (ipHeader.getProtocol()) {
            case IPHeader.TCP:
                hasWrite = onTcpPacketReceived(ipHeader, size);

                break;
            case IPHeader.UDP:
                onUdpPacketReceived(ipHeader, size, context);


                break;
            default:
                break;
        }
        return hasWrite;

    }

    private void onUdpPacketReceived(IPHeader ipHeader, int size, Context context) throws UnknownHostException {
        TCPHeader tcpHeader = mTCPHeader;
        short portKey = tcpHeader.getSourcePort();


        NatSession session = NatSessionManager.getSession(portKey);
        if (session == null || session.remoteIP != ipHeader.getDestinationIP() || session.remotePort
                != tcpHeader.getDestinationPort()) {
            session = NatSessionManager.createSession(portKey, ipHeader.getDestinationIP(), tcpHeader
                    .getDestinationPort(), NatSession.UDP);
            session.vpnStartTime = vpnStartTime;
            ThreadProxy.getInstance().execute(new Runnable() {
                @Override
                public void run() {
                    if (PortHostService.getInstance() != null) {
                        PortHostService.getInstance().refreshSessionInfo();
                    }

                }
            });
        }

        session.lastRefreshTime = System.currentTimeMillis();
        session.packetSent++; //注意顺序

        byte[] bytes = Arrays.copyOf(mPacket, mPacket.length);
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes, 0, size);
        byteBuffer.limit(size);
        Packet packet = new Packet(byteBuffer);
        udpServer.processUDPPacket(packet, portKey, context);
    }

    private boolean onTcpPacketReceived(IPHeader ipHeader, int size) throws IOException {
        boolean hasWrite = false;
        TCPHeader tcpHeader = mTCPHeader;
        //矫正TCPHeader里的偏移量，使它指向真正的TCP数据地址
        tcpHeader.mOffset = ipHeader.getHeaderLength();
        if (tcpHeader.getSourcePort() == mTcpProxyServer.port) {
            VPNLog.d(TAG, "process  tcp packet from net ");
            NatSession session = NatSessionManager.getSession(tcpHeader.getDestinationPort());
            if (session != null) {
                ipHeader.setSourceIP(ipHeader.getDestinationIP());
                tcpHeader.setSourcePort(session.remotePort);
                ipHeader.setDestinationIP(LOCAL_IP);

                CommonMethods.ComputeTCPChecksum(ipHeader, tcpHeader);
                mVPNOutputStream.write(ipHeader.mData, ipHeader.mOffset, size);
                mReceivedBytes += size;
            } else {
                DebugLog.i("NoSession: %s %s\n", ipHeader.toString(), tcpHeader.toString());
            }

        } else {
            VPNLog.d(TAG, "process  tcp packet to net ");
            short portKey = tcpHeader.getSourcePort();
            NatSession session = NatSessionManager.getSession(portKey);
            if (session == null || session.remoteIP != ipHeader.getDestinationIP() || session.remotePort
                    != tcpHeader.getDestinationPort()) {
                session = NatSessionManager.createSession(portKey, ipHeader.getDestinationIP(), tcpHeader
                        .getDestinationPort(), NatSession.TCP);
                session.vpnStartTime = vpnStartTime;
                ThreadProxy.getInstance().execute(new Runnable() {
                    @Override
                    public void run() {
                        PortHostService instance = PortHostService.getInstance();
                        if (instance != null) {
                            instance.refreshSessionInfo();
                        }

                    }
                });
            }

            session.lastRefreshTime = System.currentTimeMillis();
            session.packetSent++; //注意顺序
            int tcpDataSize = ipHeader.getDataLength() - tcpHeader.getHeaderLength();
            //丢弃tcp握手的第二个ACK报文。因为客户端发数据的时候也会带上ACK，这样可以在服务器Accept之前分析出HOST信息。
            if (session.packetSent == 2 && tcpDataSize == 0) {
                return false;
            }

            //分析数据，找到host
            if (session.bytesSent == 0 && tcpDataSize > 10) {
                int dataOffset = tcpHeader.mOffset + tcpHeader.getHeaderLength();
                HttpRequestHeaderParser.parseHttpRequestHeader(session, tcpHeader.mData, dataOffset,
                        tcpDataSize);
                DebugLog.i("Host: %s\n", session.remoteHost);
                DebugLog.i("Request: %s %s\n", session.method, session.requestUrl);
            } else if (session.bytesSent > 0
                    && !session.isHttpsSession
                    && session.isHttp
                    && session.remoteHost == null
                    && session.requestUrl == null) {
                int dataOffset = tcpHeader.mOffset + tcpHeader.getHeaderLength();
                session.remoteHost = HttpRequestHeaderParser.getRemoteHost(tcpHeader.mData, dataOffset,
                        tcpDataSize);
                session.requestUrl = "http://" + session.remoteHost + "/" + session.pathUrl;
            }

            //转发给本地TCP服务器
            ipHeader.setSourceIP(ipHeader.getDestinationIP());
            ipHeader.setDestinationIP(LOCAL_IP);
            tcpHeader.setDestinationPort(mTcpProxyServer.port);

            CommonMethods.ComputeTCPChecksum(ipHeader, tcpHeader);
            mVPNOutputStream.write(ipHeader.mData, ipHeader.mOffset, size);
            //注意顺序
            session.bytesSent += tcpDataSize;
            mSentBytes += size;
        }
        hasWrite = true;
        return hasWrite;
    }

    private void waitUntilPrepared() {
        while (prepare(this) != null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                if (AppDebug.IS_DEBUG) {
                    e.printStackTrace();
                }
                DebugLog.e("waitUntilPrepared catch an exception %s\n", e);
            }
        }
    }

    private ParcelFileDescriptor establishVPN() throws Exception {
        Builder builder = new Builder();
        builder.setMtu(MUTE_SIZE);
        selectPackage = "kr.txwy.and.snqx";
        DebugLog.i("setMtu: %d\n", ProxyConfig.Instance.getMTU());

        ProxyConfig.IPAddress ipAddress = ProxyConfig.Instance.getDefaultLocalIP();
        LOCAL_IP = CommonMethods.ipStringToInt(ipAddress.Address);
        builder.addAddress(ipAddress.Address, ipAddress.PrefixLength);
        DebugLog.i("addAddress: %s/%d\n", ipAddress.Address, ipAddress.PrefixLength);

        builder.addRoute(VPN_ROUTE, 0);

        builder.addDnsServer(GOOGLE_DNS_FIRST);
        builder.addDnsServer(CHINA_DNS_FIRST);
        builder.addDnsServer(GOOGLE_DNS_SECOND);
        builder.addDnsServer(AMERICA);

        //TODO : add setHttpProxy
        if (Build.VERSION.SDK_INT >= 29 && getSharedPreferences("TxtKRPrefs", MODE_PRIVATE).getBoolean("GFPACEnabled", false))
            builder.setHttpProxy(ProxyInfo.buildPacProxy(Uri.parse("http://speedtest63120.synology.me:404/GFPAC.js")));
        vpnStartTime = System.currentTimeMillis();
        lastVpnStartTimeFormat = TimeFormatUtil.formatYYMMDDHHMMSS(vpnStartTime);
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder.addAllowedApplication(getString(R.string.target_cn_bili));
                builder.addAllowedApplication(getString(R.string.target_cn_uc));
                builder.addAllowedApplication(getString(R.string.target_tw));
                builder.addAllowedApplication(getString(R.string.target_jp));
                builder.addAllowedApplication(getString(R.string.target_kr));
                builder.addAllowedApplication(getString(R.string.target_en));
                builder.addAllowedApplication(getPackageName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        builder.setSession(getString(R.string.app_name));
        ParcelFileDescriptor pfdDescriptor = builder.establish();
        return pfdDescriptor;
    }

    @Override
    public void run() {
        try {
            DebugLog.i("VPNService(%s) work thread is Running...\n", ID);

            waitUntilPrepared();
            udpQueue = new ConcurrentLinkedQueue<>();

            //启动TCP代理服务
            mTcpProxyServer = new TcpProxyServer(0, context);
            mTcpProxyServer.start();
            udpServer = new UDPServer(this, udpQueue);
            udpServer.start();
            NatSessionManager.clearAllSession();
            if (PortHostService.getInstance() != null) {
                PortHostService.startParse(getApplicationContext());
            }
            DebugLog.i("DnsProxy started.\n");

            ProxyConfig.Instance.onVpnStart(this);
            while (IsRunning) {
                runVPN();
            }


        } catch (InterruptedException e) {
            if (AppDebug.IS_DEBUG) {
                e.printStackTrace();
            }
            DebugLog.e("VpnService run catch an exception %s.\n", e);
        } catch (Exception e) {
            if (AppDebug.IS_DEBUG) {
                e.printStackTrace();
            }
            DebugLog.e("VpnService run catch an exception %s.\n", e);
        } finally {
            DebugLog.i("VpnService terminated");
            ProxyConfig.Instance.onVpnEnd(this);
            dispose();
        }
    }

    public void disconnectVPN() {
        try {
            if (mVPNInterface != null) {
                mVPNInterface.close();
                mVPNInterface = null;
            }
        } catch (Exception e) {
            //ignore
        }
        // notifyStatus(new VPNEvent(VPNEvent.Status.UNESTABLISHED));
        this.mVPNOutputStream = null;
    }

    private synchronized void dispose() {
        try {
            //断开VPN
            disconnectVPN();

            //停止TCP代理服务
            if (mTcpProxyServer != null) {
                mTcpProxyServer.stop();
                mTcpProxyServer = null;
                DebugLog.i("TcpProxyServer stopped.\n");
            }
            if (udpServer != null) {
                udpServer.closeAllUDPConn();
            }
            ThreadProxy.getInstance().execute(new Runnable() {
                @Override
                public void run() {
                    if (PortHostService.getInstance() != null) {
                        PortHostService.getInstance().refreshSessionInfo();
                    }
                    PortHostService.stopParse(getApplicationContext());
                }
            });


            stopSelf();
            setVpnRunningStatus(false);
        } catch (Exception e) {

        }

    }


    public boolean vpnRunningStatus() {
        return IsRunning;
    }

    public void setVpnRunningStatus(boolean isRunning) {
        IsRunning = isRunning;
    }
}
