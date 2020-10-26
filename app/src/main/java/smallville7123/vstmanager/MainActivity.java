package smallville7123.vstmanager;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    VstManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        manager = new VstManager(this);
        findViewById(R.id.button).setOnClickListener(v -> manager.showList());
    }

    @Override
    public void onBackPressed() {
        manager.onBackPressed();
    }
}