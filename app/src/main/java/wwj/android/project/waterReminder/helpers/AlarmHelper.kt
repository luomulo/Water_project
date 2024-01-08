package wwj.android.project.waterReminder.helpers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import wwj.android.project.waterReminder.recievers.BootReceiver
import wwj.android.project.waterReminder.recievers.NotifierReceiver
import java.util.concurrent.TimeUnit

/**
 * AlarmHelper 类用于设置和取消定时提醒的闹钟，以及检查是否存在闹钟。
 */
class AlarmHelper {
    private var alarmManager: AlarmManager? = null

    // 定义用于触发通知的操作
    private val ACTION_BD_NOTIFICATION = "io.github.z3r0c00l_2k.aquadroid.NOTIFICATION"

    /**
     * 设置定时提醒的闹钟。
     *
     * @param context 应用程序上下文
     * @param notificationFrequency 通知频率（以分钟为单位）
     */
    fun setAlarm(context: Context, notificationFrequency: Long) {
        // 将通知频率转换为毫秒
        val notificationFrequencyMs = TimeUnit.MINUTES.toMillis(notificationFrequency)

        // 获取系统的 AlarmManager
        alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // 创建用于触发通知的 Intent
        val alarmIntent = Intent(context, NotifierReceiver::class.java)
        alarmIntent.action = ACTION_BD_NOTIFICATION

        // 创建用于 PendingIntent 的 Intent
        val pendingAlarmIntent = PendingIntent.getBroadcast(
            context,
            0,
            alarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        // 设置重复闹钟
        alarmManager!!.setRepeating(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis(),
            notificationFrequencyMs,
            pendingAlarmIntent
        )

        /* 在设备重启后重新启动定时提醒 */
        val receiver = ComponentName(context, BootReceiver::class.java)
        context.packageManager.setComponentEnabledSetting(
            receiver,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
    }

    /**
     * 取消定时提醒的闹钟。
     *
     * @param context 应用程序上下文
     */
    fun cancelAlarm(context: Context) {
        // 获取系统的 AlarmManager
        alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // 创建用于触发通知的 Intent
        val alarmIntent = Intent(context, NotifierReceiver::class.java)
        alarmIntent.action = ACTION_BD_NOTIFICATION

        // 创建用于 PendingIntent 的 Intent
        val pendingAlarmIntent = PendingIntent.getBroadcast(
            context,
            0,
            alarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        // 取消闹钟
        alarmManager!!.cancel(pendingAlarmIntent)

        /* 设备重启后不再启动闹钟 */
        val receiver = ComponentName(context, BootReceiver::class.java)
        val pm = context.packageManager
        pm.setComponentEnabledSetting(
            receiver,
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
        Log.i("AlarmHelper", "Cancelling alarms")
    }

    /**
     * 检查是否存在定时提醒的闹钟。
     *
     * @param context 应用程序上下文
     * @return 如果存在闹钟则返回 true，否则返回 false
     */
    fun checkAlarm(context: Context): Boolean {

        val alarmIntent = Intent(context, NotifierReceiver::class.java)
        alarmIntent.action = ACTION_BD_NOTIFICATION

        return PendingIntent.getBroadcast(
            context, 0,
            alarmIntent,
            PendingIntent.FLAG_NO_CREATE
        ) != null
    }
}
