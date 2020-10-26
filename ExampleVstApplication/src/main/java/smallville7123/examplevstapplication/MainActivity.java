package smallville7123.examplevstapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import smallville7123.vstmanager.core.VstCallback;

public class MainActivity extends AppCompatActivity implements VstCallback {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}