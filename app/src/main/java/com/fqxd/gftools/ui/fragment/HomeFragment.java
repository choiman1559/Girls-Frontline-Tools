package com.fqxd.gftools.ui.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.fqxd.gftools.features.xapk.FilePathUtil;
import com.fqxd.gftools.global.AdHelper;
import com.fqxd.gftools.global.Global;
import com.fqxd.gftools.R;
import com.fqxd.gftools.features.calculator.CalculatorActivity;
import com.fqxd.gftools.features.gfd.GFDActivity;
import com.fqxd.gftools.features.gfneko.GFNekoActivity;
import com.fqxd.gftools.features.noti.NotiActivity;
import com.fqxd.gftools.features.xapk.XapkActivity;
import com.fqxd.gftools.features.xduc.XDUCActivity;
import com.google.android.material.snackbar.Snackbar;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;

public class HomeFragment extends Fragment {

    Activity context;
    ActivityResultLauncher<Intent> startFileSelectedAction;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if(context instanceof Activity) this.context = (Activity) context;
        else throw new RuntimeException("Can't instanceof context to activity!");

        startFileSelectedAction = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if(result.getResultCode() == RESULT_OK && result.getData().getData() != null && Objects.requireNonNull(result.getData().getData().getPath()).contains(".0")) {
                Uri data = result.getData().getData();
                File file = new File(Objects.requireNonNull(FilePathUtil.getPath(context, data)));
                AlertDialog.Builder b = new AlertDialog.Builder(getContext());
                b.setTitle("Notice");
                b.setMessage("Do you want to Install?");
                b.setPositiveButton("Yes", (dialogInterface, i) -> moveCA(file));
                b.setNegativeButton("No", (dialogInterface, i) -> { });
                b.create().show();
            } else {
                AlertDialog.Builder b = new AlertDialog.Builder(context);
                b.setTitle("Error!");
                b.setMessage("File not selected or file is not *.0 file!");
                b.setPositiveButton("close", (dialog, which) -> {});
                AlertDialog d = b.create();
                d.show();
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_md2, container, false);
        OnClickListener onClickListener = new onClickListener();

        view.findViewById(R.id.XDUC_Button).setOnClickListener(onClickListener);
        view.findViewById(R.id.XAPK_Button).setOnClickListener(onClickListener);
        view.findViewById(R.id.LSC_Button).setOnClickListener(onClickListener);
        view.findViewById(R.id.GFDIC_Button).setOnClickListener(onClickListener);
        view.findViewById(R.id.GFNEKO_Button).setOnClickListener(onClickListener);
        view.findViewById(R.id.GFNOTI_Button).setOnClickListener(onClickListener);
        view.findViewById(R.id.CA_Button).setOnClickListener(onClickListener);
        AdHelper.init(context,view.findViewById(R.id.AD_Banner), view.findViewById(R.id.AD_Banner_Layout));
        return view;
    }

    private class onClickListener implements View.OnClickListener {
        @SuppressLint("NonConstantResourceId")
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.XDUC_Button:
                    startActivity(new Intent(getContext(), XDUCActivity.class));
                    break;

                case R.id.XAPK_Button:
                    if(Global.isAmazonBuild) {
                        new AlertDialog.Builder(context)
                                .setTitle(R.string.Amazon_Store_Info_Title)
                                .setMessage(R.string.Amazon_Store_Info_Description)
                                .setNegativeButton("Cancel", (dialog, which) -> {})
                                .setPositiveButton("GO TO GITHUB", (dialog, which) -> {
                                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/choiman1559/Girls-Frontline-Tools/releases/latest"));
                                    startActivity(browserIntent);
                                }).show();
                    } else startActivity(new Intent(getContext(), XapkActivity.class));
                    break;

                case R.id.LSC_Button:
                    startActivity(new Intent(getContext(), CalculatorActivity.class));
                    break;

                case R.id.GFDIC_Button:
                    startActivity(new Intent(getContext(), GFDActivity.class));
                    break;

                case R.id.GFNEKO_Button:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (Settings.canDrawOverlays(getContext())) {
                            startActivity(new Intent(getContext(), GFNekoActivity.class));
                        } else {
                            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + context.getPackageName()));
                            Toast.makeText(getContext(), R.string.Overlay_Permission_Toast, Toast.LENGTH_SHORT).show();
                            context.startActivity(intent);
                        }
                    }
                    break;

                case R.id.GFNOTI_Button:
                    if (isOffline(context)) {
                        Snackbar.make(context.findViewById(android.R.id.content), "Check Internet and Try Again", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    } else {
                        Intent intent = new Intent(getContext(), NotiActivity.class);
                        startActivity(intent);
                    }
                    break;

                case R.id.CA_Button:
                    try {
                        if(!Global.checkRootPermission()) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                            builder.setTitle(R.string.Root_Permission_Title).setMessage(R.string.Root_Permission_Description);
                            builder.setPositiveButton(R.string.Root_Permission_OK, (dialog, id) -> {
                                try {
                                    Runtime.getRuntime().exec("su");
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }).setNegativeButton(R.string.Global_Cancel, (dialog, which) -> { }).show();
                        } else {
                            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                            intent.addCategory(Intent.CATEGORY_OPENABLE);
                            intent.setType("application/octet-stream");
                            startFileSelectedAction.launch(intent);
                        }
                    } catch (IOException | InterruptedException e) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setTitle("Error!").setMessage(R.string.Root_Permission_Error);
                        builder.setPositiveButton("OK", (dialog, id) -> { });
                        builder.create().show();
                        e.printStackTrace();
                    }
                    break;
            }
        }
    }

    private void moveCA(File file) {
        try {
            Process p = Runtime.getRuntime().exec("su");
            DataOutputStream dos = new DataOutputStream(p.getOutputStream());
            dos.writeBytes("mount -o remount,rw /\n");
            dos.writeBytes("cp " + file.getAbsolutePath() + " /system/etc/security/cacerts\n");
            dos.writeBytes("chmod 644 /system/etc/security/cacerts/" + file.getName() + "\n");
            dos.writeBytes("mount -o remount,ro /\n");
            dos.writeBytes("exit\n");
            dos.flush();
            dos.close();
            p.waitFor();

            AlertDialog.Builder b = new AlertDialog.Builder(getContext());
            b.setTitle("Waring");
            b.setMessage("You Need Reboot to apply the CA.\nAre you want to reboot?");
            b.setPositiveButton("Reboot", (dialogInterface, i) -> {
                try {
                    Runtime.getRuntime().exec("su -c reboot");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            b.setNegativeButton("Later", (dialogInterface, i) -> {});
            b.create().show();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public boolean isOffline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        assert cm != null;
        if (cm.getActiveNetworkInfo() != null) {
            NetworkInfo ni = cm.getActiveNetworkInfo();
            return ni == null || !ni.isConnected();
        }
        return true;
    }
}
