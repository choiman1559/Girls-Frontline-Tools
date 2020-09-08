package com.fqxd.gftools.fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.fqxd.gftools.R;

import com.google.android.material.snackbar.Snackbar;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItem;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class GFFragment extends Fragment {
    int LayoutMode;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Intent intent = getContext().getPackageManager().getLaunchIntentForPackage(indexToPackage(FragmentPagerItem.getPosition(getArguments())));
        LayoutMode = intent == null ? 0 : 1;
        return inflater.inflate(LayoutMode == 1 ? R.layout.fragment_gf : R.layout.fragment_gfnone, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        if (LayoutMode == 1) {
            try {
                super.onViewCreated(view, savedInstanceState);
                String pkg = indexToPackage(FragmentPagerItem.getPosition(getArguments()));
                PackageManager pm = view.getContext().getApplicationContext().getPackageManager();
                TextView name = view.findViewById(R.id.appname);
                TextView ver = view.findViewById(R.id.version);
                TextView pk = view.findViewById(R.id.packagename);
                ImageView icon = view.findViewById(R.id.IconView);

                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.prefs, newInstance(pkg))
                        .commit();

                name.setText(pm.getApplicationLabel(pm.getApplicationInfo(pkg, PackageManager.GET_META_DATA)));
                ver.setText("version : " + pm.getPackageInfo(pkg, 0).versionName);
                pk.setText("package : " + pkg);

                icon.setImageDrawable(pm.getApplicationIcon(pkg));
                new GetVersionCode().execute();
            } catch (Exception e) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Error!").setMessage("Unexpected Exception catched!\nCause : " + e.toString());
                builder.setPositiveButton("CLOSE APP", (dialog, id) -> { getActivity().finish(); });
                builder.setNegativeButton("RESTART APP",((dialog, which) -> getActivity().recreate()));
                builder.create().show();
            }
        } else {
            String pkg = indexToPackage(FragmentPagerItem.getPosition(getArguments()));
            ((TextView) view.findViewById(R.id.NoneMessage)).setText("Can't find package \"" + pkg + "\"");
            view.findViewById(R.id.Button_Download).setOnClickListener(v -> {
                startActivity(new Intent(getContext(), GFDownloadActivity.class).putExtra("pkg", pkg));
            });
        }
    }

    class GetVersionCode extends AsyncTask<Void, String, String> {
        @Override
        protected String doInBackground(Void... voids) {

            String pkg = indexToPackage(FragmentPagerItem.getPosition(getArguments()));
            String newVersion = null;
            try {
                Document document = Jsoup.connect("https://play.google.com/store/apps/details?id=" + pkg + "&hl=en")
                        .timeout(30000)
                        .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                        .referrer("http://www.google.com")
                        .get();
                if (document != null) {
                    Elements element = document.getElementsContainingOwnText("Current Version");
                    for (Element ele : element) {
                        if (ele.siblingElements() != null) {
                            Elements sibElemets = ele.siblingElements();
                            for (Element sibElemet : sibElemets) {
                                newVersion = sibElemet.text();
                            }
                        }
                    }
                }
                return newVersion;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "";
        }

        @Override
        protected void onPostExecute(String newV) {
            super.onPostExecute(newV);
            String pkg = indexToPackage(FragmentPagerItem.getPosition(getArguments()));
            try {
                if (newV.equals("")) return;
                PackageManager pm = getContext().getPackageManager();
                String nowV = pm.getPackageInfo(pkg, 0).versionName;

                String[] i = nowV.split("_");
                int now = Integer.parseInt(i[1]);
                String[] j = newV.split("_");
                int newi = Integer.parseInt(j[1]);

                if (!newV.isEmpty() && now < newi) {
                    Snackbar.make(getView(), pm.getApplicationLabel(pm.getApplicationInfo(pkg, PackageManager.GET_META_DATA)) + "의 새 업데이트가 있습니다!", Snackbar.LENGTH_LONG)
                            .setAction("업데이트", v -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + pkg)))).show();
                }
            } catch (PackageManager.NameNotFoundException ignore) {
            }
        }
    }

    public static GFPrefsFragment newInstance(String pkg) {
        GFPrefsFragment fragment = new GFPrefsFragment(pkg);
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    String indexToPackage(int index) {
        String i;
        switch (index) {
            case 1:
                i = "kr.txwy.and.snqx";
                break;

            case 2:
                i = "tw.txwy.and.snqx";
                break;

            case 3:
                i = "com.digitalsky.girlsfrontline.cn.uc";
                break;

            case 4:
                i = "com.sunborn.girlsfrontline.jp";
                break;

            case 5:
                i = "com.sunborn.girlsfrontline.en";
                break;

            case 6:
                i = "com.digitalsky.girlsfrontline.cn.bili";
                break;

            default:
                return "";
        }
        return i;
    }
}