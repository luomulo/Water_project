package wwj.android.project.waterReminder.utils

import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.ValueFormatter

/**
 * 自定义的 MPAndroidChart X 轴数值格式化器，用于在图表上显示日期。
 *
 * @property dateArray 包含日期字符串的 ArrayList。
 */
class ChartXValueFormatter(val dateArray: ArrayList<String>) : ValueFormatter() {

    /**
     * 获取 X 轴标签的格式化字符串。
     *
     * @param value 要格式化的数值。
     * @param axis 对应的轴。
     * @return 格式化后的字符串。
     */
    override fun getAxisLabel(value: Float, axis: AxisBase?): String {
        // 返回索引对应的日期字符串，如果索引越界则返回空字符串
        return dateArray.getOrNull(value.toInt()) ?: ""
    }
}