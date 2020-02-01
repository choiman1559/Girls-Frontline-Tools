package com.fqxd.gftools.features.decom;

import android.app.AlertDialog;
import android.content.res.Resources;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.fqxd.gftools.R;

import java.io.File;
import java.io.FilenameFilter;

final class OnTargetSelectedListener implements AdapterView.OnItemSelectedListener {
    private DecActivity main;
    OnTargetSelectedListener(DecActivity main) {
        this.main = main;
    }

    @Override
    public void onItemSelected(final AdapterView<?> parent, final View view, int position, long id) {
        if (view == null) return;
        final Button runPatch = this.main.findViewById(R.id.centrue);
        final TextView status = this.main.findViewById(R.id.status);
        final TextView log = this.main.findViewById(R.id.log);
        final ProgressBar progress = this.main.findViewById(R.id.progress);
        if (((TextView)view).getText().equals("...")) {
            runPatch.setVisibility(View.GONE);
            status.setVisibility(View.GONE);
            progress.setVisibility(View.GONE);
            return;
        } else {
            runPatch.setVisibility(View.VISIBLE);
            status.setVisibility(View.VISIBLE);
            progress.setVisibility(View.VISIBLE);
        }
        File obbDir = new File(Environment.getExternalStorageDirectory().getPath() + "/Android/obb/" + ((TextView) view).getText());
        File[] files = obbDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.substring(name.length() - 4).equals(".obb");
            }
        });
        boolean patch_obb = true;
        if (files == null || files.length == 0) {
            patch_obb = false;
            AlertDialog.Builder alert = new AlertDialog.Builder(this.main);
            alert.setMessage(R.string.info_no_obb);
            alert.setPositiveButton(Resources.getSystem().getText(android.R.string.ok), null);
            alert.setCancelable(false);
            alert.show();
        }
        runPatch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                runPatch.setEnabled(false);
                parent.setEnabled(false);
                PatchTask patchTask = new PatchTask(main, status, log, progress, ((TextView)view).getText().toString());
                patchTask.execute(new Object[]{ new Runnable() {
                    @Override
                    public void run() {
                        runPatch.post(new Runnable() {
                            @Override
                            public void run() {
                                runPatch.setEnabled(true);
                                parent.setSelection(0);
                                parent.setEnabled(true);
                            }
                        });
                    }
                }});
            }
        });
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}