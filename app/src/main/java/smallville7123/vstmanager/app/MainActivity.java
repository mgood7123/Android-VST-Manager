package smallville7123.vstmanager.app;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import smallville7123.vstmanager.VstManager;

public class MainActivity extends AppCompatActivity {

    VstManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        manager = new VstManager(this, this, findViewById(R.id.vstView));
        findViewById(R.id.button).setOnClickListener(v -> manager.showList());
    }

    @Override
    public void onBackPressed() {
        manager.onBackPressed();
    }
}