package com.fqxd.gftools.features.chip;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class ChipSetActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    static class ChipView extends View {
        ChipSetBlock[][] blocks;
        Paint paint;

        public ChipView(Context context) {
            super(context);
            paint = new Paint();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            setBackgroundColor(Color.BLACK);

            for(int i = 0;i < blocks.length;i++) {
                for(int j = 0;j < blocks[i].length;j++) {
                    paint.setColor(blocks[i][j].getColor());
                    if(!blocks[i+1][j].equals(blocks[i][j])) {
                        //doSomethings
                    }
                    //canvas.drawRect();
                }
            }
        }
    }
}
