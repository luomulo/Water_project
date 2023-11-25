package keyur.diwan.project.waterReminder

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import kotlinx.android.synthetic.main.activity_walk_through.*

class WalkThroughActivity : AppCompatActivity() {

    private var viewPagerAdapter: WalkThroughAdapter? = null

    /**
     * 在活动创建时调用，用于设置界面和初始化适配器。
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 设置状态栏为浅色模式（Light Status Bar）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

        // 设置布局文件
        setContentView(R.layout.activity_walk_through)

        // 初始化适配器并设置给 ViewPager
        viewPagerAdapter = WalkThroughAdapter(supportFragmentManager)
        walkThroughPager.adapter = viewPagerAdapter

        // 设置指示器与 ViewPager 关联
        indicator.setViewPager(walkThroughPager)
    }

    /**
     * 在活动开始时调用，处理点击 "Get Started" 按钮的事件。
     */
    override fun onStart() {
        super.onStart()

        getStarted.setOnClickListener {
            // 点击按钮后跳转到初始化用户信息的活动，并关闭当前活动
            startActivity(Intent(this, InitUserInfoActivity::class.java))
            finish()
        }
    }

    /**
     * 内部类，用于管理 WalkThroughActivity 的 ViewPager 适配器。
     */
    private inner class WalkThroughAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        /**
         * 获取页面数量，这里为 3。
         */
        override fun getCount(): Int {
            return 3
        }

        /**
         * 获取指定位置的 Fragment 对象。
         */
        override fun getItem(i: Int): Fragment {

            when (i) {
                0 -> {
                    return WalkThroughOne()
                }

                1 -> {
                    return WalkThroughTwo()
                }

                2 -> {
                    return WalkThroughThree()
                }
                else -> {
                    return WalkThroughOne()
                }
            }
        }
    }

    /**
     * 内部静态类，表示 WalkThroughActivity 中的第一个页面的 Fragment。
     */
    class WalkThroughOne : Fragment() {
        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            // 返回第一个页面的布局文件
            return inflater.inflate(R.layout.walk_through_one, container, false)
        }
    }

    /**
     * 内部静态类，表示 WalkThroughActivity 中的第二个页面的 Fragment。
     */
    class WalkThroughTwo : Fragment() {
        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            // 返回第二个页面的布局文件
            return inflater.inflate(R.layout.walk_through_two, container, false)
        }
    }

    /**
     * 内部静态类，表示 WalkThroughActivity 中的第三个页面的 Fragment。
     */
    class WalkThroughThree : Fragment() {
        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            // 返回第三个页面的布局文件
            return inflater.inflate(R.layout.walk_through_three, container, false)
        }
    }
}

