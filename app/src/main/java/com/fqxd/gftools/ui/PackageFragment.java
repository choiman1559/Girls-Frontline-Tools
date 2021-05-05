package com.fqxd.gftools.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fqxd.gftools.Global;
import com.fqxd.gftools.R;
import com.fqxd.gftools.implement.AsyncTask;
import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.util.ArrayList;

public class PackageFragment extends Fragment {
    FragmentActivity mContext;
    PackageManager pm;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof FragmentActivity) mContext = (FragmentActivity) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_gfpackage, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        pm = mContext.getPackageManager();
        ArrayList<String> array = new ArrayList<>();
        array.add("com.digitalsky.girlsfrontline.cn.uc");
        array.add("com.digitalsky.girlsfrontline.cn.bili");
        array.add("com.sunborn.girlsfrontline.en");
        array.add("com.sunborn.girlsfrontline.jp");
        array.add("com.sunborn.girlsfrontline.cn");
        array.add("tw.txwy.and.snqx");
        array.add("kr.txwy.and.snqx");

        ArrayList<String> list = new ArrayList<>();
        for(String pkg : array) {
            if(isPackageInstalled(pkg, pm)) {
                list.add(pkg);
            }
        }
        if(list.size() > 0) view.findViewById(R.id.noShowLayer).setVisibility(View.GONE);

        RecyclerView recyclerView = view.findViewById(R.id.RecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        PackageAdapter adapter = new PackageAdapter(list);
        recyclerView.setAdapter(adapter);
    }

    public static boolean isPackageInstalled(String packageName, PackageManager packageManager) {
        try {
            return packageManager.getApplicationInfo(packageName, 0).enabled;
        }
        catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 5) {
            mContext.getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment, new PackageFragment())
                    .commitNowAllowingStateLoss();
        }
    }

    public class PackageAdapter extends RecyclerView.Adapter<PackageAdapter.ViewHolder> {
        private final ArrayList<String> mData;

        public class ViewHolder extends RecyclerView.ViewHolder {
            ConstraintLayout baseLayout;
            ImageView appIcon;
            TextView appName;
            TextView appDescription;
            ImageButton startGame;

            LinearLayout moreLayout;
            MaterialButton DeleteData;
            MaterialButton DeleteApp;
            MaterialButton AppInfo;

            ViewHolder(View itemView) {
                super(itemView);
                baseLayout = itemView.findViewById(R.id.baseLayout);
                appIcon = itemView.findViewById(R.id.app_icon);
                appName = itemView.findViewById(R.id.app_name);
                appDescription = itemView.findViewById(R.id.app_des);
                startGame = itemView.findViewById(R.id.PlayGame);
                moreLayout = itemView.findViewById(R.id.extraMenu);
                DeleteData = itemView.findViewById(R.id.DeleteData);
                DeleteApp = itemView.findViewById(R.id.DeleteApp);
                AppInfo = itemView.findViewById(R.id.AppInfo);
            }
        }

        PackageAdapter(ArrayList<String> list) {
            mData = list;
        }

        @NonNull
        @Override
        public PackageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.item_package_apps, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull PackageAdapter.ViewHolder holder, int position) {
            String Package = mData.get(position);
            View.OnClickListener onClickListener = new onClickListener(holder, Package);

            try {
                holder.appName.setText(pm.getApplicationLabel(pm.getApplicationInfo(Package, PackageManager.GET_META_DATA)));
                holder.appIcon.setImageDrawable(pm.getApplicationIcon(Package));
                holder.appDescription.setText(String.format("%s (v.%s)",Package ,pm.getPackageInfo(Package, 0).versionName));
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            holder.moreLayout.setVisibility(View.GONE);

            holder.baseLayout.setOnClickListener(onClickListener);
            holder.appIcon.setOnClickListener(onClickListener);
            holder.appName.setOnClickListener(onClickListener);
            holder.appDescription.setOnClickListener(onClickListener);
            holder.startGame.setOnClickListener(onClickListener);

            holder.moreLayout.setOnClickListener(onClickListener);
            holder.DeleteData.setOnClickListener(onClickListener);
            holder.DeleteApp.setOnClickListener(onClickListener);
            holder.AppInfo.setOnClickListener(onClickListener);
        }

        private class onClickListener implements View.OnClickListener {
            PackageAdapter.ViewHolder holder;
            String Package;

            onClickListener(PackageAdapter.ViewHolder holder, String Package) {
                this.holder = holder;
                this.Package = Package;
            }

            @SuppressLint("NonConstantResourceId")
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.PlayGame:
                        startActivity(pm.getLaunchIntentForPackage(Package));
                        break;

                    case R.id.DeleteData:
                        AlertDialog.Builder b = new AlertDialog.Builder(mContext);
                        String path = Global.Storage + "/Android/data/" + Package;
                        b.setTitle("삭제 확인");
                        b.setMessage("정말로 데이터를 전부 삭제하시겠습니까?");
                        b.setPositiveButton("삭제", (dialogInterface, i) -> {
                            if (!new File(path).exists())
                                Toast.makeText(mContext, "데이터를 찾을수 없습니다!", Toast.LENGTH_SHORT).show();
                            else {
                                deleteDir dd = new deleteDir(mContext, path, Package);
                                dd.execute();
                            }
                        });
                        b.setNegativeButton("취소", (dialogInterface, i) -> { });
                        b.create().show();
                        break;

                    case R.id.DeleteApp:
                        startActivityForResult(new Intent(Intent.ACTION_DELETE).setData(Uri.parse("package:" + Package)), 5);
                        break;

                    case R.id.AppInfo:
                        try {
                            Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            intent.setData(Uri.parse("package:" + Package));
                            startActivity(intent);

                        } catch (ActivityNotFoundException e) {
                            Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
                            startActivity(intent);
                        }
                        break;

                    default:
                        holder.moreLayout.setVisibility(holder.moreLayout.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
                        break;
                }
            }
        }

        public class deleteDir extends AsyncTask<Void,Void,String> {
            Activity main;
            String dirname;
            String Package;
            ProgressDialog progressDialog;

            deleteDir(Activity main, String dirname, String Package) {
                this.main = main;
                this.dirname = dirname;
                this.Package = Package;
                progressDialog = new ProgressDialog(this.main);
            }

            @Override
            protected String doInBackground(Void... voids) {
                setDirEmpty(dirname);
                return null;
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                progressDialog.setProgressStyle(R.style.Widget_AppCompat_ProgressBar_Horizontal);
                progressDialog.setCancelable(false);
                progressDialog.setMessage("Working...");
                progressDialog.show();
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                new File(dirname).delete();
                progressDialog.dismiss();
                Toast.makeText(main, "done", Toast.LENGTH_SHORT).show();
            }

            void setDirEmpty(String dirname) {
                String path = dirname;

                File dir = new File(path);
                File[] child = dir.listFiles();

                if (dir.exists()) {
                    for (File childfile : child) {
                        if (childfile.isDirectory()) {
                            setDirEmpty(childfile.getAbsolutePath());
                        } else childfile.delete();
                    }
                }
                dir.delete();
            }
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }
    }
}
