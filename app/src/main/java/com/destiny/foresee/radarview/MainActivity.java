package com.destiny.foresee.radarview;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.View;
import android.view.animation.BounceInterpolator;

public class MainActivity extends AppCompatActivity {
    ScanPersonView spv_main;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        spv_main = findViewById(R.id.spv_main);
        spv_main.start();
        findViewById(R.id.profile_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                spv_main.startRipple();
                AnimatorSet set = new AnimatorSet();
                set.setInterpolator(new BounceInterpolator());
                set.playTogether(
                        ObjectAnimator.ofFloat(view,"scaleX",1.2f,0.8f,1f),
                        ObjectAnimator.ofFloat(view,"scaleY",1.2f,0.8f,1f));
                set.setDuration(600).start();
            }
        });
    }
}
