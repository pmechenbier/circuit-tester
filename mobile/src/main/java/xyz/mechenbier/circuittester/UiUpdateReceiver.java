package xyz.mechenbier.circuittester;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.widget.ImageView;

class UiUpdateReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        ImageView image = (ImageView)MainActivity.getInstance().findViewById(R.id.image_powerstate);

        if (intent.getExtras().getBoolean("isCharging")){
            image.setColorFilter(Color.parseColor("#dd514c"));
        } else {
            image.setColorFilter(Color.parseColor("#5eb95e"));
        }
    }
}