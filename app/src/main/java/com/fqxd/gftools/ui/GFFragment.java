package com.fqxd.gftools.ui;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;

import com.fqxd.gftools.implement.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.fqxd.gftools.R;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import org.jsoup.Jsoup;

import java.util.ArrayList;

public class GFFragment extends Fragment {

    FragmentActivity mContext;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if(context instanceof FragmentActivity) mContext = (FragmentActivity)context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_gf_md2, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        MaterialAutoCompleteTextView spinner = view.findViewById(R.id.packageSpinner);

        mContext.getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.FeatureFragment, new StubFragment())
                .commit();

        ArrayList<String> array = new ArrayList<>();
        array.add("com.digitalsky.girlsfrontline.cn.uc (중섭)");
        array.add("com.digitalsky.girlsfrontline.cn.bili (비리섭)");
        array.add("com.sunborn.girlsfrontline.en (글섭)");
        array.add("com.sunborn.girlsfrontline.jp (일섭)");
        array.add("tw.txwy.and.snqx (대만섭)");
        array.add("kr.txwy.and.snqx (한섭)");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_dropdown_item, array);
        spinner.setAdapter(adapter);
        spinner.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String Package = s.toString().split(" ")[0];
                FeatureFragment fragment =  new FeatureFragment(Package);
                fragment.setRetainInstance(true);

                mContext.getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.FeatureFragment, fragment)
                        .commitNowAllowingStateLoss();
                new GetVersionCode(Package).execute();
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
    }

    public static class StubFragment extends Fragment {
        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_gf_md2_none, container, false);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            Drawable alpha = ((ImageView)view.findViewById(R.id.InfoImage)).getDrawable();
            alpha.setAlpha(50);
        }
    }

    class GetVersionCode extends AsyncTask<Void, String, String> {
        String pkg;

        GetVersionCode(String pkg) {
            this.pkg = pkg;
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                return Jsoup.connect("https://play.google.com/store/apps/details?id=" + pkg + "&hl=en")
                        .timeout(30000)
                        .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                        .referrer("http://www.google.com")
                        .get()
                        .select("div.hAyfc:nth-child(4) > span:nth-child(2) > div:nth-child(1) > span:nth-child(1)")
                        .first()
                        .ownText();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "";
        }

        @Override
        protected void onPostExecute(String newV) {
            super.onPostExecute(newV);
            try {
                if (newV.equals("")) return;
                PackageManager pm = mContext.getPackageManager();
                String nowV = pm.getPackageInfo(pkg, 0).versionName;

                String[] i = nowV.split("_");
                int now = Integer.parseInt(i[1]);
                String[] j = newV.split("_");
                int newi = Integer.parseInt(j[1]);

                if (!newV.isEmpty() && now < newi) {
                    Snackbar.make(mContext.findViewById(android.R.id.content), pm.getApplicationLabel(pm.getApplicationInfo(pkg, PackageManager.GET_META_DATA)) + "의 새 업데이트가 있습니다!", Snackbar.LENGTH_LONG)
                            .setAction("업데이트", v -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + pkg)))).show();
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}