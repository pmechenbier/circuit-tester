package xyz.mechenbier.circuittester;

import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    private PowerConnectionReceiver pConRec = new PowerConnectionReceiver();
    private IntentFilter ifilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pConRec.init(this);

        ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        pConRec.init(this);
    }

    @Override
    protected void onResume(){
        this.registerReceiver(pConRec, ifilter);
        super.onResume();
    }

    @Override
    protected void onPause(){
        this.unregisterReceiver(pConRec);
        super.onPause();
    }
}