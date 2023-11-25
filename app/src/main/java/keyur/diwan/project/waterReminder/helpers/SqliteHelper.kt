package keyur.diwan.project.waterReminder.helpers

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * SQLite 数据库辅助类，用于管理和操作水份提醒应用的统计数据。
 *
 * @param context 应用程序上下文
 */
class SqliteHelper(val context: Context) : SQLiteOpenHelper(
    context,
    DATABASE_NAME, null,
    DATABASE_VERSION
) {

    companion object {
        private val DATABASE_VERSION = 1
        private val DATABASE_NAME = "Aqua"
        private val TABLE_STATS = "stats"
        private val KEY_ID = "id"
        private val KEY_DATE = "date"
        private val KEY_INTOOK = "intook"
        private val KEY_TOTAL_INTAKE = "totalintake"
    }

    /**
     * 在数据库创建时调用，用于创建统计表。
     *
     * @param db SQLiteDatabase 实例
     */
    override fun onCreate(db: SQLiteDatabase?) {

        val CREATE_STATS_TABLE = ("CREATE TABLE " + TABLE_STATS + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + KEY_DATE + " TEXT UNIQUE,"
                + KEY_INTOOK + " INT," + KEY_TOTAL_INTAKE + " INT" + ")")
        db?.execSQL(CREATE_STATS_TABLE)

    }

    /**
     * 在数据库升级时调用，用于删除旧表并重新创建统计表。
     *
     * @param db SQLiteDatabase 实例
     * @param oldVersion 旧版本号
     * @param newVersion 新版本号
     */
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS " + TABLE_STATS)
        onCreate(db)
    }

    /**
     * 向统计表中添加新的统计数据。
     *
     * @param date 日期
     * @param intook 当日摄入量
     * @param totalintake 总摄入量
     * @return 插入数据的行号，如果数据已存在则返回 -1
     */
    fun addAll(date: String, intook: Int, totalintake: Int): Long {
        if (checkExistance(date) == 0) {
            val values = ContentValues()
            values.put(KEY_DATE, date)
            values.put(KEY_INTOOK, intook)
            values.put(KEY_TOTAL_INTAKE, totalintake)
            val db = this.writableDatabase
            val response = db.insert(TABLE_STATS, null, values)
            db.close()
            return response
        }
        return -1
    }

    /**
     * 获取指定日期的当日摄入量。
     *
     * @param date 日期
     * @return 当日摄入量
     */
    fun getIntook(date: String): Int {
        val selectQuery = "SELECT $KEY_INTOOK FROM $TABLE_STATS WHERE $KEY_DATE = ?"
        val db = this.readableDatabase
        db.rawQuery(selectQuery, arrayOf(date)).use {
            if (it.moveToFirst()) {
                return it.getInt(it.getColumnIndex(KEY_INTOOK))
            }
        }
        return 0
    }

    /**
     * 更新指定日期的当日摄入量。
     *
     * @param date 日期
     * @param selectedOption 增加的摄入量
     * @return 更新的行数
     */
    fun addIntook(date: String, selectedOption: Int): Int {
        val intook = getIntook(date)
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(KEY_INTOOK, intook + selectedOption)

        val response = db.update(TABLE_STATS, contentValues, "$KEY_DATE = ?", arrayOf(date))
        db.close()
        return response
    }

    /**
     * 检查指定日期的数据是否存在。
     *
     * @param date 日期
     * @return 存在的行数
     */
    fun checkExistance(date: String): Int {
        val selectQuery = "SELECT $KEY_INTOOK FROM $TABLE_STATS WHERE $KEY_DATE = ?"
        val db = this.readableDatabase
        db.rawQuery(selectQuery, arrayOf(date)).use {
            if (it.moveToFirst()) {
                return it.count
            }
        }
        return 0
    }

    /**
     * 获取所有统计数据的游标。
     *
     * @return 所有统计数据的游标
     */
    fun getAllStats(): Cursor {
        val selectQuery = "SELECT * FROM $TABLE_STATS"
        val db = this.readableDatabase
        return db.rawQuery(selectQuery, null)
    }

    /**
     * 更新指定日期的总摄入量。
     *
     * @param date 日期
     * @param totalintake 总摄入量
     * @return 更新的行数
     */
    fun updateTotalIntake(date: String, totalintake: Int): Int {
        val intook = getIntook(date)
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(KEY_TOTAL_INTAKE, totalintake)

        val response = db.update(TABLE_STATS, contentValues, "$KEY_DATE = ?", arrayOf(date))
        db.close()
        return response
    }

}
