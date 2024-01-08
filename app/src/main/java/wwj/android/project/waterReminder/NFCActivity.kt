package wwj.android.project.waterReminder

import android.app.PendingIntent
import android.nfc.NfcAdapter
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import android.widget.TextView
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import com.google.android.material.snackbar.Snackbar
import wwj.android.project.waterReminder.helpers.SqliteHelper
import wwj.android.project.waterReminder.utils.AppUtils
import java.util.Arrays
import java.nio.charset.Charset
import kotlinx.android.synthetic.main.activity_main.*

class NFCActivity : AppCompatActivity(){
    private var nfcAdapter: NfcAdapter? = null
    private var pendingIntent: PendingIntent? = null
    private lateinit var titleTV: TextView
    private lateinit var payloadTV: TextView
    private var totalIntake: Int = 0
    private var inTook: Int = 0
    private lateinit var sharedPref: SharedPreferences
    private lateinit var sqliteHelper: SqliteHelper
    private lateinit var dateNow: String
    private var snackbar: Snackbar? = null
//    private lateinit var mContext: Content

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nfc)
        Log.d("NFC_DEBUG", "im coming1")

        // 初始化 NfcAdapter
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        // 初始化 PendingIntent
        val intent = Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)
    }
    fun updateValues() {
        totalIntake = sharedPref.getInt(AppUtils.TOTAL_INTAKE, 0)
        inTook = sqliteHelper.getIntook(dateNow)
        setWaterLevel(inTook, totalIntake)
    }
    private fun setWaterLevel(inTook: Int, totalIntake: Int) {
        tvIntook.text = "$inTook"
        tvTotalIntake.text = "/$totalIntake ml"
        val progress = ((inTook / totalIntake.toFloat()) * 100).toInt()
        intakeProgress.currentProgress = progress
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
                                    val add_intook = text.toInt()
                                    // 现在，"text" 中包含了从 Text Record 中提取的文本
                                    Log.d("NFC_DEBUG", "add_intook: $add_intook")
                                    if ((inTook * 100 / totalIntake) <= 140) {
                                        // 检查是否超过每日目标，如果未超过则更新数据
                                        if (sqliteHelper.addIntook(dateNow, add_intook!!) > 0) {
                                            inTook += add_intook!!
                                            setWaterLevel(inTook, totalIntake)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        updateValues()
    }


}