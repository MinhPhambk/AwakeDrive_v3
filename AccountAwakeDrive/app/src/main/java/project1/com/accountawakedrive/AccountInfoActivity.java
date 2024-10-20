package project1.com.accountawakedrive;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class AccountInfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_info);

        Button btnAccount = findViewById(R.id.btn_back);
        ((View) btnAccount).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Chuyển về AccountActivity
                Intent intent = new Intent(AccountInfoActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        // Thiết lập giao diện ở đây
    }
}

