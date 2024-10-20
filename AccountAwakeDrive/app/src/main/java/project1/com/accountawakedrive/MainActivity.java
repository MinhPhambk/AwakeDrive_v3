package project1.com.accountawakedrive;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Spinner spinner = findViewById(R.id.spinner);

        // Mảng chứa ID hình ảnh của các quốc gia
        int[] images = {
                R.drawable.vietnamflag,  // Hình ảnh cờ Việt Nam
                R.drawable.usaflag,      // Hình ảnh cờ Mỹ
                R.drawable.chinaflag      // Hình ảnh cờ Trung Quốc
        };

        // Mảng chứa tên ngôn ngữ
        String[] texts = {
                "Tiếng Việt",
                "English",
                "中文"
        };

        // Tạo adapter và gán cho spinner
        ImageSpinnerAdapter adapter = new ImageSpinnerAdapter(this, texts, images);
        spinner.setAdapter(adapter);

        // Tìm Button bằng id
        Button btnAccountInfo = findViewById(R.id.btn_back);

        // Chuyển sang AccountInfoActivity
        ((View) btnAccountInfo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Chuyển sang AccountInfoActivity
                Intent intent = new Intent(MainActivity.this, AccountInfoActivity.class);
                startActivity(intent);
            }
        });

        //Chuyển sang HistoryActicity
        Button btnHistory = findViewById(R.id.btn_history);
        btnHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Chuyển sang HistoryActivity
                Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
                startActivity(intent);
            }
        });

        //Chuyển sang Setting Activity
        Button btnSettings = findViewById(R.id.btn_settings);
        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Chuyển sang SettingActivity
                Intent intent = new Intent(MainActivity.this, SettingActivity.class);
                startActivity(intent);
            }
        });

        //Chuyển sang link Hỗ trợ và góp ý
        Button btnSupport = findViewById(R.id.btn_support);
        btnSupport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // URL để chuyển hướng
                String url = "https://www.example.com/support"; // Thay đổi đường link này thành đường link thật của bạn
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
            }
        });

        //Chuyển sang link Chính sách và Điều khoan
        Button btnPrivacyPolicy = findViewById(R.id.btn_privacy_policy);
        btnPrivacyPolicy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // URL để chuyển hướng
                String url = "https://www.example.com/privacy_policy"; // Thay đổi đường link này thành đường link thật của bạn
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
            }
        });

    }
}
