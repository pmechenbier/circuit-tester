package xyz.mechenbier.circuittester;

import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.ToggleButton;

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

        ToggleButton  muteToggleButton = (ToggleButton ) findViewById(R.id.toggle_mute);
        muteToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                pConRec.audio.SetMuted(isChecked);
            }
        });
    }

    @Override
    protected void onResume(){
        this.registerReceiver(pConRec, ifilter);
        pConRec.Resume();
        super.onResume();
    }

    @Override
    protected void onPause(){
        this.unregisterReceiver(pConRec);
        pConRec.Pause();
        super.onPause();
    }

    public void onRadioButtonClicked(View view) {
        pConRec.SoundButtonClicked(view);
    }
}