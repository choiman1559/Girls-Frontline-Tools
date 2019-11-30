package com.fqxd.gftools;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import com.theartofdev.edmodo.cropper.*;

import androidx.fragment.app.FragmentActivity;
import gun0912.tedbottompicker.*;

public class ImgSelActivity extends FragmentActivity {

    @Override
    public void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        setContentView(R.layout.activity_imgsel);

        Button IOK = findViewById(R.id.ImgOk);
        ImageView IPV = findViewById(R.id.ImgPreview);
        Button ICB = findViewById(R.id.ImgSelButton);

        IOK.setEnabled(false);
        ICB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TedBottomPicker.with(ImgSelActivity.this)
                        .show(new TedBottomSheetDialogFragment.OnImageSelectedListener() {
                            public void onImageSelected(Uri uri) {
                                CropImage.activity(uri).start(ImgSelActivity.this);
                            }
                        });
            }
        });

        IOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), ICCActivity.class));
                finish();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        setContentView(R.layout.activity_imgsel);

        Button IOK = findViewById(R.id.ImgOk);
        ImageView IPV = findViewById(R.id.ImgPreview);
        Button ICB = findViewById(R.id.ImgSelButton);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                IPV.setImageURI(resultUri);
                IOK.setEnabled(true);
            }

            else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}