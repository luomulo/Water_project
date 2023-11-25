package keyur.diwan.project.waterReminder.recievers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import keyur.diwan.project.waterReminder.helpers.AlarmHelper
import keyur.diwan.project.waterReminder.utils.AppUtils

/**
 * 接收系统启动广播的广播接收器，用于重新设置水份提醒的闹钟定时器。
 */
class BootReceiver : BroadcastReceiver() {

    // 创建 AlarmHelper 实例用于管理水份提醒的闹钟
    private val alarm = AlarmHelper()

    /**
     * 在接收到广播时调用，用于处理系统启动广播，重新设置水份提醒的闹钟定时器。
     *
     * @param context 上下文
     * @param intent 收到的广播意图
     */
    override fun onReceive(context: Context?, intent: Intent?) {
        // 检查意图和动作是否非空
        if (intent != null && intent.action != null) {
            // 判断是否为系统启动广播
            if (intent.action == "android.intent.action.BOOT_COMPLETED") {
                // 获取用户偏好设置
                val prefs = context!!.getSharedPreferences(
                    AppUtils.USERS_SHARED_PREF,
                    AppUtils.PRIVATE_MODE
                )
                // 获取通知频率和新消息通知设置
                val notificationFrequency = prefs.getInt(AppUtils.NOTIFICATION_FREQUENCY_KEY, 60)
                val notificationsNewMessage =
                    prefs.getBoolean("notifications_new_message", true)
                // 取消之前的水份提醒闹钟
                alarm.cancelAlarm(context)
                // 如果允许新消息通知，则重新设置水份提醒闹钟
                if (notificationsNewMessage) {
                    alarm.setAlarm(context, notificationFrequency.toLong())
                }
            }
        }
    }
}
