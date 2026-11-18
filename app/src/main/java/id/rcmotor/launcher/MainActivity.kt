package id.rcmotor.launcher

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : Activity() {
    private val bg = Color.rgb(17, 25, 32)
    private val panel = Color.rgb(29, 42, 53)
    private val tile = Color.rgb(34, 49, 62)
    private val orange = Color.rgb(245, 158, 11)
    private val white = Color.rgb(242, 245, 248)
    private val muted = Color.rgb(151, 166, 179)
    private val prefs by lazy { getSharedPreferences("launcher_settings", MODE_PRIVATE) }
    private val handler = Handler(Looper.getMainLooper())
    private var settingsMode = false

    private val clockUpdater = object : Runnable {
        override fun run() {
            updateClock()
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildHome()
        handler.post(clockUpdater)
    }

    override fun onResume() {
        super.onResume()
        if (!settingsMode) buildHome()
    }

    override fun onDestroy() {
        handler.removeCallbacks(clockUpdater)
        super.onDestroy()
    }

    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        if (settingsMode) {
            buildHome()
        } else {
            // Back from the home screen should not close the launcher accidentally.
            moveTaskToBack(false)
        }
    }

    private fun buildHome() {
        settingsMode = false
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(bg, Color.rgb(20, 30, 38)))
            setPadding(dp(16), dp(14), dp(16), dp(10))
        }

        // Compact brand header
        val top = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val logo = ImageView(this).apply {
            setImageResource(R.drawable.rc_logo)
            scaleType = ImageView.ScaleType.FIT_CENTER
        }
        val logoLp = LinearLayout.LayoutParams(dp(42), dp(42))
        logoLp.rightMargin = dp(10)
        top.addView(logo, logoLp)

        val brandBlock = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val brand = TextView(this).apply {
            text = "RC MOTOR"
            textSize = 20f
            setTextColor(white)
            typeface = Typeface.create("sans-serif-condensed", Typeface.BOLD)
            letterSpacing = 0.11f
        }
        brandBlock.addView(brand)
        val subBrand = TextView(this).apply {
            text = "WORKSHOP TERMINAL"
            textSize = 9f
            setTextColor(muted)
            letterSpacing = 0.16f
        }
        brandBlock.addView(subBrand)
        top.addView(brandBlock, LinearLayout.LayoutParams(0, -2, 1f))

        val settings = button("⚙  APLIKASI", orange, Color.rgb(24, 32, 39)).apply {
            textSize = 11f
            setPadding(dp(8), 0, dp(8), 0)
        }
        settings.setOnClickListener { buildSettings() }
        top.addView(settings)
        root.addView(top)

        // Dashboard clock panel
        val clockPanel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = rounded(panel, 18)
            setPadding(dp(15), dp(10), dp(15), dp(11))
        }
        val clock = TextView(this).apply {
            id = 1001
            text = timeText()
            textSize = 42f
            setTextColor(white)
            typeface = Typeface.create("sans-serif", Typeface.BOLD)
            letterSpacing = 0.02f
        }
        clockPanel.addView(clock)
        val date = TextView(this).apply {
            id = 1002
            text = dateText()
            textSize = 12f
            setTextColor(muted)
            setPadding(0, dp(1), 0, 0)
        }
        clockPanel.addView(date)
        val status = TextView(this).apply {
            text = "●  SISTEM AKTIF"
            textSize = 9f
            setTextColor(orange)
            typeface = Typeface.DEFAULT_BOLD
            letterSpacing = 0.08f
            setPadding(0, dp(6), 0, 0)
        }
        clockPanel.addView(status)
        val clockLp = LinearLayout.LayoutParams(-1, -2)
        clockLp.topMargin = dp(13)
        root.addView(clockPanel, clockLp)

        val heading = TextView(this).apply {
            text = "APLIKASI TERPILIH"
            textSize = 11f
            setTextColor(orange)
            typeface = Typeface.DEFAULT_BOLD
            letterSpacing = 0.12f
            setPadding(dp(2), dp(13), 0, dp(6))
        }
        root.addView(heading)

        val scroll = ScrollView(this).apply {
            isFillViewport = false
            clipToPadding = false
            setPadding(0, 0, 0, dp(2))
        }
        val grid = GridLayout(this).apply { columnCount = 2; useDefaultMargins = false }
        val allApps = getLaunchableApps()
        val selected = getSelectedPackages()
        val visibleApps = if (selected == null) allApps else allApps.filter { selected.contains(it.packageName) }
        visibleApps.forEach { grid.addView(appTile(it)) }
        scroll.addView(grid)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))

        if (visibleApps.isEmpty()) {
            val empty = TextView(this).apply {
                text = "Belum ada aplikasi dipilih.\nTekan APLIKASI untuk mengatur."
                setTextColor(muted); textSize = 13f; gravity = Gravity.CENTER
                setPadding(dp(18), dp(20), dp(18), dp(20))
            }
            root.addView(empty, LinearLayout.LayoutParams(-1, 0, 1f))
        }

        val footerLine = View(this).apply { setBackgroundColor(Color.rgb(48, 64, 76)) }
        val lineLp = LinearLayout.LayoutParams(-1, dp(1)); lineLp.topMargin = dp(6)
        root.addView(footerLine, lineLp)
        val footer = TextView(this).apply {
            text = "TERMINAL  •  RC MOTOR"
            textSize = 9f
            setTextColor(muted)
            gravity = Gravity.CENTER
            letterSpacing = 0.10f
            setPadding(0, dp(7), 0, 0)
        }
        root.addView(footer)
        setContentView(root)
    }

    private fun updateClock() {
        findViewById<TextView>(1001)?.text = timeText()
        findViewById<TextView>(1002)?.text = dateText()
    }

    private fun buildSettings() {
        settingsMode = true
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(bg, Color.rgb(20, 30, 38)))
            setPadding(dp(16), dp(14), dp(16), dp(10))
        }
        val header = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        val title = TextView(this).apply {
            text = "PILIH APLIKASI"
            textSize = 18f; setTextColor(white); typeface = Typeface.DEFAULT_BOLD; letterSpacing = 0.04f
        }
        header.addView(title, LinearLayout.LayoutParams(0, -2, 1f))
        val back = button("KEMBALI", orange, Color.rgb(24, 32, 39))
        back.setOnClickListener { buildHome() }
        header.addView(back)
        root.addView(header)

        val hint = TextView(this).apply {
            text = "Pilih aplikasi yang akan muncul di dashboard RC Motor."
            textSize = 12f; setTextColor(muted); setPadding(0, dp(9), 0, dp(10))
        }
        root.addView(hint)
        val scroll = ScrollView(this)
        val list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val apps = getLaunchableApps()
        val saved = getSelectedPackages()
        apps.forEach { app ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL
                background = rounded(tile, 12)
                setPadding(dp(9), dp(6), dp(9), dp(6))
            }
            val icon = ImageView(this).apply { setImageDrawable(app.icon); scaleType = ImageView.ScaleType.FIT_CENTER }
            row.addView(icon, LinearLayout.LayoutParams(dp(38), dp(38)))
            val check = CheckBox(this).apply {
                text = app.label; textSize = 13f; setTextColor(white)
                isChecked = saved == null || saved.contains(app.packageName)
                buttonTintList = android.content.res.ColorStateList.valueOf(orange)
                setPadding(dp(7), 0, 0, 0)
            }
            check.setOnCheckedChangeListener { _, checked ->
                val current = getSelectedPackages() ?: apps.map { it.packageName }.toMutableSet()
                if (checked) current.add(app.packageName) else current.remove(app.packageName)
                prefs.edit().putStringSet("selected_apps", current.toSet()).apply()
            }
            row.addView(check, LinearLayout.LayoutParams(0, -2, 1f))
            val lp = LinearLayout.LayoutParams(-1, -2); lp.bottomMargin = dp(5)
            list.addView(row, lp)
        }
        scroll.addView(list)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        val all = button("TAMPILKAN SEMUA APLIKASI", orange, Color.rgb(24, 32, 39))
        all.setOnClickListener { prefs.edit().remove("selected_apps").apply(); buildSettings() }
        val allLp = LinearLayout.LayoutParams(-1, -2); allLp.topMargin = dp(8)
        root.addView(all, allLp)
        setContentView(root)
    }

    private fun appTile(app: LaunchApp): View {
        val item = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER
            background = rounded(tile, 14)
            setPadding(dp(6), dp(7), dp(6), dp(6))
            isClickable = true; isFocusable = true
            setOnClickListener { launchApp(app.packageName) }
        }
        val icon = ImageView(this).apply { setImageDrawable(app.icon); scaleType = ImageView.ScaleType.FIT_CENTER }
        item.addView(icon, LinearLayout.LayoutParams(dp(40), dp(40)))
        val label = TextView(this).apply {
            text = app.label; textSize = 11.5f; setTextColor(white); gravity = Gravity.CENTER
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL); maxLines = 2
            setPadding(0, dp(4), 0, 0)
        }
        item.addView(label)
        val lp = GridLayout.LayoutParams().apply {
            width = 0; height = dp(82); columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            setMargins(dp(3), dp(3), dp(3), dp(3))
        }
        item.layoutParams = lp
        return item
    }

    private data class LaunchApp(val label: String, val packageName: String, val icon: android.graphics.drawable.Drawable)

    private fun getLaunchableApps(): List<LaunchApp> {
        val pm = packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        return pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)
            .map { LaunchApp(it.loadLabel(pm).toString(), it.activityInfo.packageName, it.loadIcon(pm)) }
            .filter { it.packageName != packageName }
            .distinctBy { it.packageName }
            .sortedBy { it.label.lowercase(Locale.getDefault()) }
    }

    private fun getSelectedPackages(): MutableSet<String>? = prefs.getStringSet("selected_apps", null)?.toMutableSet()

    private fun launchApp(packageName: String) {
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        if (launchIntent != null) try {
            startActivity(launchIntent)
        } catch (_: Exception) {
            Toast.makeText(this, "Tidak bisa membuka aplikasi", Toast.LENGTH_SHORT).show()
        }
    }

    private fun button(text: String, color: Int, textColor: Int): Button = Button(this).apply {
        this.text = text; setTextColor(textColor); textSize = 12f
        background = rounded(color, 10); isAllCaps = false
        minHeight = dp(40); minimumHeight = dp(40)
        setPadding(dp(8), 0, dp(8), 0)
    }

    private fun rounded(color: Int, radius: Int) = GradientDrawable().apply {
        setColor(color); cornerRadius = dp(radius).toFloat()
    }
    private fun timeText() = SimpleDateFormat("HH:mm", Locale("id", "ID")).format(Date())
    private fun dateText() = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID")).format(Date())
    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
}
