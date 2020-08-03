package com.fqxd.gftools.features.proxy;

import android.content.Intent;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fqxd.gftools.MainActivity;
import com.nononsenseapps.filepicker.AbstractFilePickerFragment;
import com.nononsenseapps.filepicker.FilePickerActivity;
import com.nononsenseapps.filepicker.FilePickerFragment;

import java.io.File;
import java.util.Arrays;

/**
 * Created by HtetzNaing on 11/25/2017.
 */

public class CAFilePicker extends FilePickerActivity {

    public CAFilePicker() {
        super();
    }

    @Override
    protected AbstractFilePickerFragment<File> getFragment(
            @Nullable final String startPath, final int mode, final boolean allowMultiple,
            final boolean allowCreateDir, final boolean allowExistingFile,
            final boolean singleClick) {
        AbstractFilePickerFragment<File> fragment = new CustomFilePickerFragment();
        fragment.setArgs(startPath != null ? startPath : Environment.getExternalStorageDirectory().getPath(),
                mode, allowMultiple, allowCreateDir, allowExistingFile, singleClick);
        return fragment;
    }

    public static class CustomFilePickerFragment extends FilePickerFragment {

        // File extension to filter on
        private static final String[] EXTENSIONS = new String[]{".0"};

        /**
         * @param file
         * @return The file extension. If file has no extension, it returns null.
         */

        private String getExtension(@NonNull File file) {
            String path = file.getPath();
            int i = path.lastIndexOf(".");
            if (i < 0) {
                return null;
            } else {
                return path.substring(i);
            }
        }

        @Override
        protected boolean isItemVisible(final File file) {
            boolean ret = super.isItemVisible(file);
            if (ret && !isDir(file) && (mode == MODE_FILE || mode == MODE_FILE_AND_DIR)) {
                String ext = getExtension(file);
                return ext != null && Arrays.asList(EXTENSIONS).contains(ext);
            }
            return ret;
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
        finish();
    }
}