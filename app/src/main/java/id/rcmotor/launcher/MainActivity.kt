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
    private val bg = Color.rgb(24, 33, 43)
    private val panel = Color.rgb(38, 52, 66)
    private val orange = Color.rgb(245, 158, 11)
    private val white = Color.rgb(244, 246, 248)
    private val muted = Color.rgb(154, 168, 181)
    private val prefs by lazy { getSharedPreferences("launcher_settings", MODE_PRIVATE) }
    private val handler = Handler(Looper.getMainLooper())
    private var settingsMode = false
    private val clockUpdater = object : Runnable {
        override fun run() {
            if (!settingsMode) updateClock()
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

    private fun buildHome() {
        settingsMode = false
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bg)
            setPadding(dp(18), dp(20), dp(18), dp(12))
        }

        val top = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val brand = TextView(this).apply {
            text = "RC MOTOR"
            textSize = 21f
            setTextColor(white)
            typeface = Typeface.DEFAULT_BOLD
            letterSpacing = 0.08f
        }
        top.addView(brand, LinearLayout.LayoutParams(0, -2, 1f))
        val settings = button("⚙  APLIKASI", orange, bg)
        settings.textSize = 12f
        settings.setOnClickListener { buildSettings() }
        top.addView(settings)
        root.addView(top)

        val clock = TextView(this).apply {
            id = 1001
            text = timeText()
            textSize = 48f
            setTextColor(white)
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, dp(14), 0, 0)
        }
        root.addView(clock)
        val date = TextView(this).apply {
            text = dateText()
            textSize = 14f
            setTextColor(muted)
            setPadding(0, 0, 0, dp(16))
        }
        root.addView(date)

        val heading = TextView(this).apply {
            text = "APLIKASI TERPILIH"
            textSize = 12f
            setTextColor(orange)
            typeface = Typeface.DEFAULT_BOLD
            letterSpacing = 0.08f
            setPadding(0, 0, 0, dp(8))
        }
        root.addView(heading)

        val scroll = ScrollView(this).apply { isFillViewport = false }
        val grid = GridLayout(this).apply { columnCount = 2; useDefaultMargins = false }
        val allApps = getLaunchableApps()
        val selected = getSelectedPackages()
        val visibleApps = if (selected == null) allApps else allApps.filter { selected.contains(it.packageName) }
        visibleApps.forEach { app -> grid.addView(appTile(app)) }
        scroll.addView(grid)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))

        if (visibleApps.isEmpty()) {
            val empty = TextView(this).apply {
                text = "Belum ada aplikasi dipilih.\nTekan ⚙ APLIKASI untuk mengatur."
                setTextColor(muted); textSize = 15f; gravity = Gravity.CENTER
                setPadding(dp(20), dp(30), dp(20), dp(30))
            }
            root.addView(empty)
        }

        val footer = TextView(this).apply {
            text = "TERMINAL RC MOTOR"
            textSize = 11f
            setTextColor(muted)
            gravity = Gravity.CENTER
            setPadding(0, dp(12), 0, 0)
        }
        root.addView(footer)
        setContentView(root)
    }

    private fun updateClock() {
        findViewById<TextView>(1001)?.text = timeText()
    }

    private fun buildSettings() {
        settingsMode = true
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bg)
            setPadding(dp(18), dp(18), dp(18), dp(12))
        }
        val header = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        val title = TextView(this).apply {
            text = "PENGATURAN APLIKASI"
            textSize = 18f; setTextColor(white); typeface = Typeface.DEFAULT_BOLD
        }
        header.addView(title, LinearLayout.LayoutParams(0, -2, 1f))
        val back = button("KEMBALI", orange, bg)
        back.setOnClickListener { buildHome() }
        header.addView(back)
        root.addView(header)
        val hint = TextView(this).apply {
            text = "Centang aplikasi yang ingin ditampilkan di layar utama."
            textSize = 13f; setTextColor(muted); setPadding(0, dp(10), 0, dp(12))
        }
        root.addView(hint)
        val scroll = ScrollView(this)
        val list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val apps = getLaunchableApps()
        val saved = getSelectedPackages()
        apps.forEach { app ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL
                background = rounded(panel, 12)
                setPadding(dp(10), dp(7), dp(10), dp(7))
            }
            val icon = ImageView(this).apply { setImageDrawable(app.icon); scaleType = ImageView.ScaleType.FIT_CENTER }
            row.addView(icon, LinearLayout.LayoutParams(dp(38), dp(38)))
            val check = CheckBox(this).apply {
                text = app.label; textSize = 14f; setTextColor(white)
                isChecked = saved == null || saved.contains(app.packageName)
                buttonTintList = android.content.res.ColorStateList.valueOf(orange)
                setPadding(dp(8), 0, 0, 0)
            }
            check.setOnCheckedChangeListener { _, checked ->
                val current = getSelectedPackages() ?: apps.map { it.packageName }.toMutableSet()
                if (checked) current.add(app.packageName) else current.remove(app.packageName)
                prefs.edit().putStringSet("selected_apps", current.toSet()).apply()
            }
            row.addView(check, LinearLayout.LayoutParams(0, -2, 1f))
            val lp = LinearLayout.LayoutParams(-1, -2); lp.bottomMargin = dp(6)
            list.addView(row, lp)
        }
        scroll.addView(list)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        val all = button("TAMPILKAN SEMUA APLIKASI", orange, bg)
        all.setOnClickListener { prefs.edit().remove("selected_apps").apply(); buildSettings() }
        root.addView(all)
        setContentView(root)
    }

    private fun appTile(app: LaunchApp): View {
        val item = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER
            background = rounded(panel, 16)
            setPadding(dp(10), dp(12), dp(10), dp(10))
            isClickable = true; isFocusable = true
            setOnClickListener { launchApp(app.packageName) }
        }
        val icon = ImageView(this).apply { setImageDrawable(app.icon); scaleType = ImageView.ScaleType.FIT_CENTER }
        item.addView(icon, LinearLayout.LayoutParams(dp(48), dp(48)))
        val label = TextView(this).apply {
            text = app.label; textSize = 13f; setTextColor(white); gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD; maxLines = 2
            setPadding(0, dp(7), 0, 0)
        }
        item.addView(label)
        val lp = GridLayout.LayoutParams().apply {
            width = 0; height = dp(105); columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            setMargins(dp(4), dp(4), dp(4), dp(4))
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
        if (launchIntent != null) try { startActivity(launchIntent) } catch (_: Exception) {
            Toast.makeText(this, "Tidak bisa membuka aplikasi", Toast.LENGTH_SHORT).show()
        }
    }

    private fun button(text: String, color: Int, textColor: Int): Button = Button(this).apply {
        this.text = text; setTextColor(textColor); textSize = 13f
        background = rounded(color, 10); isAllCaps = false; minHeight = dp(42); minimumHeight = dp(42)
        setPadding(dp(10), 0, dp(10), 0)
    }

    private fun rounded(color: Int, radius: Int) = GradientDrawable().apply {
        setColor(color); cornerRadius = dp(radius).toFloat()
    }
    private fun timeText() = SimpleDateFormat("HH:mm", Locale("id", "ID")).format(Date())
    private fun dateText() = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID")).format(Date())
    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
}
