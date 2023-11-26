package keyur.diwan.project.waterReminder.utils

import java.text.SimpleDateFormat
import java.util.*


class AppUtils {
    companion object {

        /**
         * 计算基于体重和锻炼时间的推荐每日水分摄入量。
         *
         * @param weight 用户的体重（千克）。
         * @param workTime 用户每日的锻炼时间（分钟）。
         * @return 推荐的每日水分摄入量（毫升）。
         */
        fun calculateIntake(weight: Int, workTime: Int): Double {
            return ((weight * 100 / 3.0) + (workTime / 6 * 7))
        }

        /**
         * 获取当前日期，格式为 "dd-MM-yyyy"。
         *
         * @return 当前日期的格式化字符串。
         */
        fun getCurrentDate(): String? {
            val c = Calendar.getInstance().time
            val df = SimpleDateFormat("dd-MM-yyyy")
            return df.format(c)
        }

        // SharedPreferences 键名
        const val USERS_SHARED_PREF = "user_pref"
        const val PRIVATE_MODE = 0
        const val WEIGHT_KEY = "weight"
        const val WORK_TIME_KEY = "worktime"
        const val TOTAL_INTAKE = "totalintake"
        const val NOTIFICATION_STATUS_KEY = "notificationstatus"
        const val NOTIFICATION_FREQUENCY_KEY = "notificationfrequency"
        const val NOTIFICATION_MSG_KEY = "notificationmsg"
        const val SLEEPING_TIME_KEY = "sleepingtime"
        const val WAKEUP_TIME = "wakeuptime"
        const val NOTIFICATION_TONE_URI_KEY = "notificationtone"
        const val FIRST_RUN_KEY = "firstrun"
    }
}
