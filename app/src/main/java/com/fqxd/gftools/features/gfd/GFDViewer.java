package com.fqxd.gftools.features.gfd;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import com.fqxd.gftools.R;

public class GFDViewer extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gfdviewer);
        ImageView imageView = findViewById(R.id.imageView);

        Intent getnum = getIntent();
        int btnum = getnum.getIntExtra("buttonnum",0x00);

        switch(btnum){
            case 1:
                imageView.setImageResource(R.drawable.ko_producttable_doll);
                break;

            case 2:
                imageView.setImageResource(R.drawable.ko_producttable_equipment);
                break;

            case 3:
                imageView.setImageResource(R.drawable.ko_producttable_fairy);
                break;

            case 4:
                imageView.setImageResource(R.drawable.ko_md_table);
                break;

            case 5:
                imageView.setImageResource(R.drawable.ko_fairyattribute);
                break;

            case 6:
                imageView.setImageResource(R.drawable.ko_recommenddollrecipe);
                break;

            case 7:
                imageView.setImageResource(R.drawable.ko_recommendequipmentrecipe);
                break;

            case 8:
                imageView.setImageResource(R.drawable.ko_recommendmd);
                break;

            case 9:
                imageView.setImageResource(R.drawable.ko_recommendleveling);
                break;

            case 10:
                imageView.setImageResource(R.drawable.ko_recommendbreeding);
                break;

                default:
                    imageView.setImageResource(R.drawable.splashimage);
                    Toast.makeText(getApplicationContext(), "Error!", Toast.LENGTH_SHORT).show();
                    break;
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(getApplicationContext(),GFDActivity.class));
        finish();
    }
}
