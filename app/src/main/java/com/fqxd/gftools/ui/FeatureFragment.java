package com.fqxd.gftools.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.fqxd.gftools.R;
import com.fqxd.gftools.features.cen.CenActivity;
import com.fqxd.gftools.features.decom.DecActivity;
import com.fqxd.gftools.features.icon.IconChangeActivity;
import com.fqxd.gftools.features.proxy.ProxyActivity;
import com.fqxd.gftools.features.rotation.RotationActivity;
import com.fqxd.gftools.features.txtkr.TxtKrPatchActivity;
import com.fqxd.gftools.features.txtkr.TxtKrPatchActivityLegecy;
import com.google.android.material.card.MaterialCardView;

public class FeatureFragment extends Fragment {
    String Package;
    FragmentActivity mContext;
    Integer LayoutMode = 0;

    FeatureFragment(String PackageName) {
        this.Package = PackageName;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof FragmentActivity) mContext = (FragmentActivity) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Intent intent = mContext.getPackageManager().getLaunchIntentForPackage(Package);
        LayoutMode = (intent == null ? 0 : 1);
        return inflater.inflate(LayoutMode == 1 ? R.layout.fragment_gf_features : R.layout.fragment_gfnone, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (LayoutMode == 0) {
            ((TextView) view.findViewById(R.id.NoneMessage)).setText(String.format("Can't find package \"%s\"", Package));
            view.findViewById(R.id.Button_Download).setOnClickListener(v -> startActivity(new Intent(mContext, GFDownloadActivity.class).putExtra("pkg", Package)));
        } else {
            MaterialCardView DEC = view.findViewById(R.id.Button_DEC);
            MaterialCardView ICO = view.findViewById(R.id.Button_ICO);
            MaterialCardView CEN = view.findViewById(R.id.Button_CEN);
            MaterialCardView PXY = view.findViewById(R.id.Button_PXY);
            MaterialCardView TXT = view.findViewById(R.id.Button_TXT);
            MaterialCardView ROT = view.findViewById(R.id.Button_ROT);

            if (Package.contains("txwy.and.snqx") || Package.contains("cn.uc")) {
                TXT.setVisibility(View.GONE);
            }

            OnClickListener onClickListener = new onClickListener();
            DEC.setOnClickListener(onClickListener);
            ICO.setOnClickListener(onClickListener);
            CEN.setOnClickListener(onClickListener);
            PXY.setOnClickListener(onClickListener);
            TXT.setOnClickListener(onClickListener);
            ROT.setOnClickListener(onClickListener);
        }
    }

    private class onClickListener implements View.OnClickListener {
        @SuppressLint("NonConstantResourceId")
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.Button_DEC:
                    startActivity(DecActivity.class);
                    break;

                case R.id.Button_ICO:
                    startActivity(IconChangeActivity.class);
                    break;

                case R.id.Button_ROT:
                    startActivity(RotationActivity.class);
                    break;

                case R.id.Button_CEN:
                    startActivity(CenActivity.class);
                    break;

                case R.id.Button_PXY:
                    startActivity(ProxyActivity.class);
                    break;

                case R.id.Button_TXT:
                    if(Build.VERSION.SDK_INT > 29) startActivity(TxtKrPatchActivity.class);
                    else startActivity(TxtKrPatchActivityLegecy.class);
            }
        }
    }

    void startActivity(Class<?> cls) {
        startActivity(new Intent(requireView().getContext(), cls).putExtra("pkg", Package));
    }
}
