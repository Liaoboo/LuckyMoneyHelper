package com.luckymoneyhelper.services;

import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.PowerManager;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import com.luckymoneyhelper.R;
import com.luckymoneyhelper.constants.Const;

/**
 * 红包监听
 * Created by LiaoBo on 2016/5/24.
 */
public class LuckyMoneyMonitorService extends NotificationListenerService {
    private KeyguardManager.KeyguardLock kl;

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        // 主界面设置的信息保存在SharedPreferences中，在这里进行获取
        SharedPreferences sharedPreferences = getSharedPreferences(Const.CONFIG, MODE_PRIVATE);
        // 判断消息是否为微信红包
        if (sbn.getNotification().tickerText.toString().contains(Const.TYPE_NOTIFICATION_STATE_TIP)
                && sbn.getPackageName ().equals(Const.TYPE_PACKAGE_NAME)) {

            // 读取设置信息，判断是否该点亮屏幕并解开锁屏，解锁的原理是把锁屏关闭掉
            if (sharedPreferences.getBoolean(Const.IS_UNLOCK, true)) {
                KeyguardManager km = (KeyguardManager) getSystemService(getApplicationContext()
                        .KEYGUARD_SERVICE);
                kl = km.newKeyguardLock("unlock");

                // 把系统锁屏暂时关闭
                kl.disableKeyguard();
                PowerManager pm = (PowerManager) getSystemService(getApplicationContext()
                        .POWER_SERVICE);
                PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP |
                        PowerManager.SCREEN_DIM_WAKE_LOCK, "bright");
                wl.acquire();
                wl.release();
            }

            try {
                // 打开notification所对应的pendingintent
                sbn.getNotification().contentIntent.send();

            } catch (PendingIntent.CanceledException e) {
                e.printStackTrace();
            }

            // 判断是否该播放提示音
            if (sharedPreferences.getBoolean(Const.IS_PROMPT_TONE, true)) {
                MediaPlayer mediaPlayer = new MediaPlayer().create(this, R.raw.hongbao_arrived);
                mediaPlayer.start();
            }

            // 监听系统广播，如果屏幕熄灭就把系统锁屏还原
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.intent.action.SCREEN_OFF");
            ScreenOffReceiver screenOffReceiver = new ScreenOffReceiver();
            registerReceiver(screenOffReceiver, intentFilter);

        }

    }

    class ScreenOffReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (kl != null) {
                // 还原锁屏
                kl.reenableKeyguard();
            }
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        super.onNotificationRemoved(sbn);
    }
}
