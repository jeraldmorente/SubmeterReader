package cict.thesis.submeterreader;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class SplashScreen extends AppCompatActivity {
    private static int SPLASH_SCREEN = 3500;
    Animation topAnim, bottomAnim;
    ImageView logoImage;
    TextView logoText, logoTag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.splash_screen);
        logoImage = findViewById(R.id.imageView);
        logoText = findViewById(R.id.textlogo);
        logoTag= findViewById(R.id.texttag);
        topAnim = AnimationUtils.loadAnimation(this, R.anim.top_animation);
        bottomAnim = AnimationUtils.loadAnimation(this, R.anim.bottom_animation);
        logoImage.setAnimation(topAnim);
        logoText.setAnimation(bottomAnim);
        logoTag.setAnimation(bottomAnim);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashScreen.this, MainMenu.class);
                startActivity(intent);
                finish();
            }
        },SPLASH_SCREEN);

    }
}
