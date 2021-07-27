package com.fqxd.gftools.global;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.fqxd.gftools.BuildConfig;
import com.fqxd.gftools.R;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;

import java.util.Collections;
import java.util.List;

public class AdHelper {
    public static void init(Activity mContext, View parentView, View innerView) {
        AdView adview = new AdView(mContext);
        adview.setAdSize(AdSize.BANNER);
        adview.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                Log.d("AdService","ad Loaded successfully!");
                parentView.setVisibility(View.VISIBLE);
                adview.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAdFailedToLoad(@androidx.annotation.NonNull LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                Log.d("AdService","Failed while loading ad!");
                parentView.setVisibility(View.GONE);
                adview.setVisibility(View.GONE);
            }
        });

        switch(Global.getSHA1Hash(mContext)) {
            case "cf:61:36:5e:71:42:fa:21:7c:b5:5f:52:6d:e3:d9:06:57:f5:5e:01":
            case "d5:5c:2e:6a:58:4c:3d:4f:4a:3a:08:cd:1c:7e:6a:eb:ee:ea:46:10":
                if(!BuildConfig.DEBUG){
                    MobileAds.initialize(mContext, i -> { });
                    adview.setAdUnitId(mContext.getString(R.string.ad_id_pub));
                    adview.loadAd(new AdRequest.Builder().build());
                }
                break;

            //Ad Config for Test : you can delete this case
            case "36:47:5f:49:ce:2d:bc:cb:b8:59:30:e3:86:17:85:6c:78:cf:86:53":
                if(BuildConfig.DEBUG) {
                    MobileAds.initialize(mContext, i -> { });
                    adview.setAdUnitId(mContext.getString(R.string.ad_id_test));
                    List<String> testDeviceIds = Collections.singletonList("8F3F0D8FE070C4498B1C6B5B3C754B4E");
                    RequestConfiguration configuration = new RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build();
                    MobileAds.setRequestConfiguration(configuration);
                    adview.loadAd(new AdRequest.Builder().build());
                }
                break;

            default:
                parentView.setVisibility(View.GONE);
                adview.setVisibility(View.GONE);
                break;
        }

        ((RelativeLayout)innerView).addView(adview,0);
    }

    public static void init(Activity mContext, View parentView) {
        AdView adview = new AdView(mContext);
        adview.setAdSize(AdSize.BANNER);
        adview.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                Log.d("AdService","ad Loaded successfully!");
                adview.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAdFailedToLoad(@androidx.annotation.NonNull LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                Log.d("AdService","Failed while loading ad!");
                adview.setVisibility(View.GONE);
            }
        });

        switch(Global.getSHA1Hash(mContext)) {
            case "cf:61:36:5e:71:42:fa:21:7c:b5:5f:52:6d:e3:d9:06:57:f5:5e:01":
            case "d5:5c:2e:6a:58:4c:3d:4f:4a:3a:08:cd:1c:7e:6a:eb:ee:ea:46:10":
                if(!BuildConfig.DEBUG){
                    MobileAds.initialize(mContext, i -> { });
                    adview.setAdUnitId(mContext.getString(R.string.ad_id_pub));
                    adview.loadAd(new AdRequest.Builder().build());
                }
                break;

            //Ad Config for Test : you can delete this case
            case "36:47:5f:49:ce:2d:bc:cb:b8:59:30:e3:86:17:85:6c:78:cf:86:53":
                if(BuildConfig.DEBUG) {
                    MobileAds.initialize(mContext, i -> { });
                    adview.setAdUnitId(mContext.getString(R.string.ad_id_test));
                    List<String> testDeviceIds = Collections.singletonList("8F3F0D8FE070C4498B1C6B5B3C754B4E");
                    RequestConfiguration configuration = new RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build();
                    MobileAds.setRequestConfiguration(configuration);
                    adview.loadAd(new AdRequest.Builder().build());
                }
                break;

            default:
                adview.setVisibility(View.GONE);
                break;
        }

        if(parentView instanceof LinearLayout) {
            ((LinearLayout) parentView).addView(adview, 0);
        }

        if(parentView instanceof ConstraintLayout) {
            ((ConstraintLayout) parentView).addView(adview, 0);
        }

        if(parentView instanceof RelativeLayout) {
            ((RelativeLayout) parentView).addView(adview, 0);
        }
    }
}
