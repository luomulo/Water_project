package wwj.android.project.waterReminder.recievers

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import wwj.android.project.waterReminder.R
import wwj.android.project.waterReminder.helpers.NotificationHelper
import wwj.android.project.waterReminder.utils.AppUtils

/**
 * 接收水份提醒广播的广播接收器，负责生成和显示水份提醒通知。
 */
class NotifierReceiver : BroadcastReceiver() {

    /**
     * 在接收到水份提醒广播时调用，生成并显示水份提醒通知。
     *
     * @param context 上下文
     * @param intent 收到的广播意图
     */
    @SuppressLint("ResourceType")
    override fun onReceive(context: Context, intent: Intent) {

        // 获取用户偏好设置
        val prefs = context.getSharedPreferences(
            AppUtils.USERS_SHARED_PREF,
            AppUtils.PRIVATE_MODE
        )
        // 获取通知铃声设置
        val notificationsTone = prefs.getString(
            AppUtils.NOTIFICATION_TONE_URI_KEY, RingtoneManager.getDefaultUri(
                RingtoneManager.TYPE_NOTIFICATION
            ).toString()
        )

        // 获取应用名称作为通知标题
        val title = context.resources.getString(R.string.app_name)
        // 获取用户设置的通知消息或使用默认消息
        val messageToShow = prefs.getString(
            AppUtils.NOTIFICATION_MSG_KEY,
            context.resources.getString(R.string.pref_notification_message_value)
        )

        // 创建 NotificationHelper 实例用于生成和显示通知
        val nHelper = NotificationHelper(context)
        // 获取通知构建器
        val nBuilder = messageToShow?.let {
            nHelper.getNotification(title, it, notificationsTone)
        }
        // 显示通知
        nHelper.notify(1, nBuilder)
    }
}
