package com.fqxd.gftools;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import com.theartofdev.edmodo.cropper.*;

import androidx.fragment.app.FragmentActivity;
import gun0912.tedbottompicker.*;

public class ImgSelActivity extends FragmentActivity {

    @Override
    public void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        setContentView(R.layout.activity_imgsel);

        TedBottomPicker.with(ImgSelActivity.this)
                .show(new TedBottomSheetDialogFragment.OnImageSelectedListener() {
                    public void onImageSelected(Uri uri) {
                        CropImage.activity(uri)
                                .setCropShape(CropImageView.CropShape.RECTANGLE)
                                .setFixAspectRatio(true)
                                .setAspectRatio(1,1)
                                .start(ImgSelActivity.this);
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        setContentView(R.layout.activity_imgsel);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                SendIntent(resultUri);
            }

            else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    public void SendIntent(Uri uri) {
        Intent intent = new Intent(this,ICCActivity.class);
        intent.putExtra("imageUri",uri);
        startActivityForResult(intent,0x01);
        finish();
    }
}