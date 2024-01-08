package wwj.android.project.waterReminder

import android.app.PendingIntent
import android.nfc.NfcAdapter
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.content.SharedPreferences

import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.google.android.material.snackbar.Snackbar
import wwj.android.project.waterReminder.helpers.SqliteHelper
import wwj.android.project.waterReminder.utils.AppUtils

import kotlinx.android.synthetic.main.activity_main.*
import android.app.NotificationManager
import android.nfc.NdefMessage
import android.nfc.NdefRecord

import android.text.TextUtils
import android.util.TypedValue
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog

import com.google.android.material.textfield.TextInputLayout
import wwj.android.project.waterReminder.fragments.BottomSheetFragment
import wwj.android.project.waterReminder.helpers.AlarmHelper
import java.nio.charset.Charset
import java.util.Arrays


/**
 * 主活动，用于展示用户的水摄入情况和提供相关操作。
 */
class MainActivity : AppCompatActivity() {

    private var totalIntake: Int = 0
    private var inTook: Int = 0
    private lateinit var sharedPref: SharedPreferences
    private lateinit var sqliteHelper: SqliteHelper
    private lateinit var dateNow: String
    private var notificStatus: Boolean = false
    private var selectedOption: Int? = null
    private var snackbar: Snackbar? = null
    private var doubleBackToExitPressedOnce = false
    private var nfcAdapter: NfcAdapter? = null
    private var pendingIntent: PendingIntent? = null

    /**
     * 在创建活动时调用，用于设置界面和初始化数据。
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 获取共享偏好和SQLite帮助类的实例
        sharedPref = getSharedPreferences(AppUtils.USERS_SHARED_PREF, AppUtils.PRIVATE_MODE)
        sqliteHelper = SqliteHelper(this)

        // 获取用户设定的每日总水摄入量
        totalIntake = sharedPref.getInt(AppUtils.TOTAL_INTAKE, 0)

        // 检查是否首次运行，如果是则跳转到引导页
        if (sharedPref.getBoolean(AppUtils.FIRST_RUN_KEY, true)) {
            startActivity(Intent(this, WalkThroughActivity::class.java))
            finish()
        } else if (totalIntake <= 0) {
            // 如果总摄入量未设置，则跳转到用户信息初始化页
            startActivity(Intent(this, InitUserInfoActivity::class.java))
            finish()
        }

        // 获取当前日期
        dateNow = AppUtils.getCurrentDate()!!



        // 初始化 NfcAdapter
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        // 初始化 PendingIntent
        val intent = Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)

        if (NfcAdapter.ACTION_NDEF_DISCOVERED == getIntent().action) {
            handleNFCIntent(getIntent())
        }

    }
    override fun onResume() {
        super.onResume()

        // 在 onResume 中注册 PendingIntent
        nfcAdapter?.enableForegroundDispatch(this, pendingIntent, null, null)

    }
    override fun onPause() {
        super.onPause()

        // 在 onPause 中取消注册 PendingIntent
        nfcAdapter?.disableForegroundDispatch(this)
    }
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleNFCIntent(intent)
    }


private fun handleNFCIntent(intent: Intent) {
    val action = intent.action
    if (NfcAdapter.ACTION_NDEF_DISCOVERED == action) {
        val rawMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
        if (rawMessages != null) {
            for (rawMessage in rawMessages) {
                if (rawMessage is NdefMessage) {
                    val records = rawMessage.records
                    for (record in records) {
                        // 判断记录是否为 Text Record
                        if (NdefRecord.TNF_WELL_KNOWN == record.tnf) {
                            if (Arrays.equals(record.type, NdefRecord.RTD_TEXT)) {
                                // 解析 Text Record 的数据
                                val payload = record.payload
                                val textEncoding = if ((payload[0].toInt() and 128) == 0) "UTF-8" else "UTF-16"
                                val languageCodeLength = payload[0].toInt() and 0x63
                                val text = String(payload, languageCodeLength + 1, payload.size - languageCodeLength - 1, Charset.forName(textEncoding))
                                // 假设文本是整数值表示水量
                                val addIntake = text.toIntOrNull()
                                addIntake?.let {
                                    // 更新水量状态和UI
                                    updateIntake(addIntake)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

    private fun updateIntake(addIntake: Int) {
        // 增加水量的逻辑
        if (sqliteHelper.addIntook(dateNow, addIntake) > 0) {
            inTook += addIntake
            setWaterLevel(inTook, totalIntake)
            // 显示更新后的水量
            // 更新UI等
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 释放资源等操作
    }
    /**
     * 更新摄入值和界面元素。
     */
    fun updateValues() {
        totalIntake = sharedPref.getInt(AppUtils.TOTAL_INTAKE, 0)
        inTook = sqliteHelper.getIntook(dateNow)
        setWaterLevel(inTook, totalIntake)
    }

    /**
     * 在活动开始时调用，用于设置通知状态、按钮点击事件等。
     */
    override fun onStart() {
        super.onStart()

        // 获取系统默认可选项背景
        val outValue = TypedValue()
        applicationContext.theme.resolveAttribute(
            android.R.attr.selectableItemBackground,
            outValue,
            true
        )

        // 获取通知状态和通知频率，并设置相应的按钮图标
        notificStatus = sharedPref.getBoolean(AppUtils.NOTIFICATION_STATUS_KEY, true)
        val alarm = AlarmHelper()
        if (!alarm.checkAlarm(this) && notificStatus) {
            btnNotific.setImageDrawable(getDrawable(R.drawable.ic_bell))
            alarm.setAlarm(
                this,
                sharedPref.getInt(AppUtils.NOTIFICATION_FREQUENCY_KEY, 30).toLong()
            )
        }

        // 根据通知状态设置按钮图标
        if (notificStatus) {
            btnNotific.setImageDrawable(getDrawable(R.drawable.ic_bell))
        } else {
            btnNotific.setImageDrawable(getDrawable(R.drawable.ic_bell_disabled))
        }

        // 将当天摄入记录添加到SQLite数据库
        sqliteHelper.addAll(dateNow, 0, totalIntake)

        // 更新界面元素
        updateValues()

        // 设置按钮点击事件
        btnMenu.setOnClickListener {
            val bottomSheetFragment = BottomSheetFragment(this)
            bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
        }

        fabAdd.setOnClickListener {
            // 处理用户选择的摄入选项
            if (selectedOption != null) {
                if ((inTook * 100 / totalIntake) <= 140) {
                    // 检查是否超过每日目标，如果未超过则更新数据
                    if (sqliteHelper.addIntook(dateNow, selectedOption!!) > 0) {
                        inTook += selectedOption!!
                        setWaterLevel(inTook, totalIntake)

                        Snackbar.make(it, "你的饮水量已保存！！", Snackbar.LENGTH_SHORT)
                            .show()

                    }
                } else {
                    Snackbar.make(it, "你已经达成饮水目标了！！！", Snackbar.LENGTH_SHORT).show()
                }
                selectedOption = null
                tvCustom.text = "自定义"
                op50ml.background = getDrawable(outValue.resourceId)
                op100ml.background = getDrawable(outValue.resourceId)
                op150ml.background = getDrawable(outValue.resourceId)
                op200ml.background = getDrawable(outValue.resourceId)
                op250ml.background = getDrawable(outValue.resourceId)
                opCustom.background = getDrawable(outValue.resourceId)

                // 移除挂起的通知
                val mNotificationManager: NotificationManager =
                    getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                mNotificationManager.cancelAll()
            } else {
                YoYo.with(Techniques.Shake)
                    .duration(700)
                    .playOn(cardView)
                Snackbar.make(it, "请选择一个按钮", Snackbar.LENGTH_SHORT).show()
            }
        }

        // 处理通知按钮点击事件
        btnNotific.setOnClickListener {
            // 切换通知状态，并更新按钮图标
            notificStatus = !notificStatus
            sharedPref.edit().putBoolean(AppUtils.NOTIFICATION_STATUS_KEY, notificStatus).apply()
            if (notificStatus) {
                btnNotific.setImageDrawable(getDrawable(R.drawable.ic_bell))
                Snackbar.make(it, "通知已开启", Snackbar.LENGTH_SHORT).show()
                alarm.setAlarm(
                    this,
                    sharedPref.getInt(AppUtils.NOTIFICATION_FREQUENCY_KEY, 30).toLong()
                )
            } else {
                btnNotific.setImageDrawable(getDrawable(R.drawable.ic_bell_disabled))
                Snackbar.make(it, "通知已关闭", Snackbar.LENGTH_SHORT).show()
                alarm.cancelAlarm(this)
            }
        }

        // 跳转到统计页面
        btnStats.setOnClickListener {
            startActivity(Intent(this, StatsActivity::class.java))
        }
//        share.setOnClickListener {
//            startActivity(Intent(this, NFCActivity::class.java))
//        }

        // 处理不同容量选项的点击事件
        op50ml.setOnClickListener {
            if (snackbar != null) {
                snackbar?.dismiss()
            }
            selectedOption = 50
            op50ml.background = getDrawable(R.drawable.option_select_bg)
            op100ml.background = getDrawable(outValue.resourceId)
            op150ml.background = getDrawable(outValue.resourceId)
            op200ml.background = getDrawable(outValue.resourceId)
            op250ml.background = getDrawable(outValue.resourceId)
            opCustom.background = getDrawable(outValue.resourceId)
        }

        op100ml.setOnClickListener {
            if (snackbar != null) {
                snackbar?.dismiss()
            }
            selectedOption = 100
            op50ml.background = getDrawable(outValue.resourceId)
            op100ml.background = getDrawable(R.drawable.option_select_bg)
            op150ml.background = getDrawable(outValue.resourceId)
            op200ml.background = getDrawable(outValue.resourceId)
            op250ml.background = getDrawable(outValue.resourceId)
            opCustom.background = getDrawable(outValue.resourceId)
        }

        op150ml.setOnClickListener {
            if (snackbar != null) {
                snackbar?.dismiss()
            }
            selectedOption = 150
            op50ml.background = getDrawable(outValue.resourceId)
            op100ml.background = getDrawable(outValue.resourceId)
            op150ml.background = getDrawable(R.drawable.option_select_bg)
            op200ml.background = getDrawable(outValue.resourceId)
            op250ml.background = getDrawable(outValue.resourceId)
            opCustom.background = getDrawable(outValue.resourceId)
        }

        op200ml.setOnClickListener {
            if (snackbar != null) {
                snackbar?.dismiss()
            }
            selectedOption = 200
            op50ml.background = getDrawable(outValue.resourceId)
            op100ml.background = getDrawable(outValue.resourceId)
            op150ml.background = getDrawable(outValue.resourceId)
            op200ml.background = getDrawable(R.drawable.option_select_bg)
            op250ml.background = getDrawable(outValue.resourceId)
            opCustom.background = getDrawable(outValue.resourceId)
        }

        op250ml.setOnClickListener {
            if (snackbar != null) {
                snackbar?.dismiss()
            }
            selectedOption = 250
            op50ml.background = getDrawable(outValue.resourceId)
            op100ml.background = getDrawable(outValue.resourceId)
            op150ml.background = getDrawable(outValue.resourceId)
            op200ml.background = getDrawable(outValue.resourceId)
            op250ml.background = getDrawable(R.drawable.option_select_bg)
            opCustom.background = getDrawable(outValue.resourceId)
        }

        opCustom.setOnClickListener {
            if (snackbar != null) {
                snackbar?.dismiss()
            }

            // 自定义输入对话框
            val li = LayoutInflater.from(this)
            val promptsView = li.inflate(R.layout.custom_input_dialog, null)

            val alertDialogBuilder = AlertDialog.Builder(this)
            alertDialogBuilder.setView(promptsView)

            val userInput = promptsView
                .findViewById(R.id.etCustomInput) as TextInputLayout

            alertDialogBuilder.setPositiveButton("确定") { dialog, id ->
                val inputText = userInput.editText!!.text.toString()
                if (!TextUtils.isEmpty(inputText)) {
                    tvCustom.text = "${inputText} ml"
                    selectedOption = inputText.toInt()
                }
            }.setNegativeButton("取消") { dialog, id ->
                dialog.cancel()
            }

            val alertDialog = alertDialogBuilder.create()
            alertDialog.show()

            // 更新选项背景
            op50ml.background = getDrawable(outValue.resourceId)
            op100ml.background = getDrawable(outValue.resourceId)
            op150ml.background = getDrawable(outValue.resourceId)
            op200ml.background = getDrawable(outValue.resourceId)
            op250ml.background = getDrawable(outValue.resourceId)
            opCustom.background = getDrawable(R.drawable.option_select_bg)
        }
    }

    /**
     * 设置水位图的高度和进度。
     */
    private fun setWaterLevel(inTook: Int, totalIntake: Int) {
        YoYo.with(Techniques.SlideInDown)
            .duration(500)
            .playOn(tvIntook)
        tvIntook.text = "$inTook"
        tvTotalIntake.text = "/$totalIntake ml"
        val progress = ((inTook / totalIntake.toFloat()) * 100).toInt()
        YoYo.with(Techniques.Pulse)
            .duration(500)
            .playOn(intakeProgress)
        intakeProgress.currentProgress = progress
        if ((inTook * 100 / totalIntake) > 140) {
            Snackbar.make(main_activity_parent, "你已经达成饮水目标了！！！", Snackbar.LENGTH_SHORT)
                .show()
        }
    }

    /**
     * 处理返回按钮点击事件，双击退出应用。
     */
    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }
    }
}

