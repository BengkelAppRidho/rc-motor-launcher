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
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : Activity() {
    private val bg = Color.rgb(7, 12, 17)
    private val panelTop = Color.rgb(28, 38, 48)
    private val panelBottom = Color.rgb(12, 18, 24)
    private val orange = Color.rgb(255, 145, 0)
    private val orangeLight = Color.rgb(255, 184, 66)
    private val white = Color.rgb(244, 247, 250)
    private val muted = Color.rgb(153, 167, 180)
    private val green = Color.rgb(54, 220, 78)
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
        window.statusBarColor = Color.rgb(9, 14, 19)
        window.navigationBarColor = Color.BLACK
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = 0
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
        if (settingsMode) buildHome() else moveTaskToBack(false)
    }

    private fun buildHome() {
        settingsMode = false
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(bg, Color.rgb(10, 17, 23), Color.rgb(14, 21, 27)))
            setPadding(dp(15), dp(8), dp(15), dp(6))
        }

        // Premium brand header: logo is FIT_CENTER with breathing room, never cropped.
        val top = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        val logoHolder = LinearLayout(this).apply {
            gravity = Gravity.CENTER
            setPadding(dp(4), dp(3), dp(4), dp(3))
        }
        val logo = ImageView(this).apply { setImageResource(R.drawable.rc_logo); scaleType = ImageView.ScaleType.FIT_CENTER; adjustViewBounds = true }
        logoHolder.addView(logo, LinearLayout.LayoutParams(dp(78), dp(56)))
        val logoLp = LinearLayout.LayoutParams(dp(86), dp(64)); logoLp.rightMargin = dp(8)
        top.addView(logoHolder, logoLp)
        val separator = View(this).apply { setBackgroundColor(Color.rgb(95, 106, 116)) }
        val sepLp = LinearLayout.LayoutParams(dp(1), dp(42)); sepLp.rightMargin = dp(10)
        top.addView(separator, sepLp)
        val brandBlock = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val brand = TextView(this).apply {
            text = "RC MOTOR"; textSize = 23f; setTextColor(white)
            typeface = Typeface.create("sans-serif-condensed", Typeface.BOLD); letterSpacing = 0.09f
        }
        brandBlock.addView(brand)
        val subBrand = TextView(this).apply {
            text = "WORKSHOP TERMINAL"; textSize = 10f; setTextColor(orangeLight)
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL); letterSpacing = 0.12f
        }
        brandBlock.addView(subBrand)
        top.addView(brandBlock, LinearLayout.LayoutParams(0, -2, 1f))
        val settings = button("▦  APLIKASI", orange, Color.BLACK, 12f).apply { setPadding(dp(9), 0, dp(9), 0) }
        settings.setOnClickListener { buildSettings() }
        top.addView(settings, LinearLayout.LayoutParams(dp(112), dp(48)))
        root.addView(top)

        // Orange industrial accent divider.
        val accent = View(this).apply { background = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, intArrayOf(orange, Color.rgb(103, 61, 20), Color.TRANSPARENT)) }
        val accentLp = LinearLayout.LayoutParams(-1, dp(3)); accentLp.topMargin = dp(7); accentLp.bottomMargin = dp(10)
        root.addView(accent, accentLp)

        // Clock dashboard panel with subtle motorcycle image, clipped to rounded shape.
        val clockPanel = FrameLayout(this).apply {
            background = gradientCard(GradientDrawable.Orientation.TL_BR, panelTop, panelBottom, Color.rgb(63, 76, 88), 22)
            clipToOutline = true
            minimumHeight = dp(155)
        }
        val motorImage = ImageView(this).apply {
            setImageResource(R.drawable.motor_panel); scaleType = ImageView.ScaleType.CENTER_CROP; alpha = 0.48f
        }
        // Fixed image height prevents MATCH_PARENT from expanding the wrap-content clock panel.
        val imageLp = FrameLayout.LayoutParams(dp(205), dp(155), Gravity.RIGHT or Gravity.TOP)
        clockPanel.addView(motorImage, imageLp)
        val fade = View(this).apply {
            background = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, intArrayOf(panelTop, Color.argb(225, 28, 38, 48), Color.argb(45, 12, 18, 24)))
        }
        clockPanel.addView(fade, FrameLayout.LayoutParams(-1, -1))
        val clockContent = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; setPadding(dp(18), dp(15), dp(18), dp(13))
        }
        val dateRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        val calendar = TextView(this).apply { text = "▣"; textSize = 21f; setTextColor(orange); gravity = Gravity.CENTER }
        dateRow.addView(calendar, LinearLayout.LayoutParams(dp(34), dp(30)))
        val date = TextView(this).apply { id = 1002; text = dateText(); textSize = 14f; setTextColor(white); typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL) }
        dateRow.addView(date)
        clockContent.addView(dateRow)
        val clock = TextView(this).apply {
            id = 1001; text = timeText(); textSize = 55f; setTextColor(white)
            typeface = Typeface.create("sans-serif", Typeface.BOLD); letterSpacing = 0.02f
        }
        val clockLp = LinearLayout.LayoutParams(-2, -2); clockLp.topMargin = dp(1)
        clockContent.addView(clock, clockLp)
        val statusRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        val dot = TextView(this).apply { text = "●"; textSize = 12f; setTextColor(green) }
        statusRow.addView(dot)
        val status = TextView(this).apply { text = "  SISTEM AKTIF"; textSize = 12f; setTextColor(green); typeface = Typeface.DEFAULT_BOLD; letterSpacing = 0.06f }
        statusRow.addView(status)
        clockContent.addView(statusRow)
        clockPanel.addView(clockContent, FrameLayout.LayoutParams(-1, -2, Gravity.TOP or Gravity.LEFT))
        val clockLpPanel = LinearLayout.LayoutParams(-1, -2); clockLpPanel.bottomMargin = dp(12)
        root.addView(clockPanel, clockLpPanel)

        // Section heading with orange icon and rule.
        val section = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        val sectionIcon = TextView(this).apply { text = "▦"; textSize = 22f; setTextColor(orange); gravity = Gravity.CENTER }
        section.addView(sectionIcon, LinearLayout.LayoutParams(dp(30), dp(30)))
        val heading = TextView(this).apply {
            text = "APLIKASI TERPILIH"; textSize = 15f; setTextColor(white)
            typeface = Typeface.create("sans-serif-condensed", Typeface.BOLD); letterSpacing = 0.06f
        }
        val headingLp = LinearLayout.LayoutParams(-2, -2); headingLp.leftMargin = dp(7); headingLp.rightMargin = dp(9)
        section.addView(heading, headingLp)
        val rule = View(this).apply { background = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, intArrayOf(orange, Color.rgb(70, 48, 28), Color.TRANSPARENT)) }
        section.addView(rule, LinearLayout.LayoutParams(0, dp(2), 1f))
        val sectionLp = LinearLayout.LayoutParams(-1, dp(34)); sectionLp.bottomMargin = dp(6)
        root.addView(section, sectionLp)

        val scroll = ScrollView(this).apply { clipToPadding = false; setPadding(0, 0, 0, dp(2)) }
        val grid = GridLayout(this).apply { columnCount = 2; useDefaultMargins = false }
        val allApps = getLaunchableApps()
        val selected = getSelectedPackages()
        val visibleApps = if (selected == null) allApps else allApps.filter { selected.contains(it.packageName) }
        visibleApps.forEach { grid.addView(appTile(it)) }
        scroll.addView(grid)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))

        if (visibleApps.isEmpty()) {
            val empty = TextView(this).apply { text = "Belum ada aplikasi dipilih.\nTekan APLIKASI untuk mengatur."; setTextColor(muted); textSize = 13f; gravity = Gravity.CENTER; setPadding(dp(18), dp(20), dp(18), dp(20)) }
            root.addView(empty, LinearLayout.LayoutParams(-1, 0, 1f))
        }

        // Footer with small industrial stripes.
        val footer = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        val leftStripe = View(this).apply { background = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, intArrayOf(Color.TRANSPARENT, orange)) }
        footer.addView(leftStripe, LinearLayout.LayoutParams(0, dp(2), 1f))
        val footerText = TextView(this).apply { text = "⚙   TERMINAL   ·   RC MOTOR"; textSize = 10f; setTextColor(muted); gravity = Gravity.CENTER; letterSpacing = 0.06f }
        val footerTextLp = LinearLayout.LayoutParams(-2, dp(27)); footerTextLp.leftMargin = dp(12); footerTextLp.rightMargin = dp(12)
        footer.addView(footerText, footerTextLp)
        val rightStripe = View(this).apply { background = GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT, intArrayOf(Color.TRANSPARENT, orange)) }
        footer.addView(rightStripe, LinearLayout.LayoutParams(0, dp(2), 1f))
        val footerLp = LinearLayout.LayoutParams(-1, dp(28)); footerLp.topMargin = dp(5)
        root.addView(footer, footerLp)
        setContentView(root)
    }

    private fun appTile(app: LaunchApp): View {
        val item = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL
            background = gradientCard(GradientDrawable.Orientation.LEFT_RIGHT, Color.rgb(32, 43, 53), Color.rgb(11, 17, 23), Color.rgb(66, 78, 90), 18)
            setPadding(dp(8), dp(8), dp(8), dp(8)); isClickable = true; isFocusable = true
            setOnClickListener { launchApp(app.packageName) }
        }
        val accentBar = View(this).apply { background = rounded(orange, 4) }
        item.addView(accentBar, LinearLayout.LayoutParams(dp(4), dp(42)))
        val icon = ImageView(this).apply { setImageDrawable(app.icon); scaleType = ImageView.ScaleType.FIT_CENTER }
        val iconLp = LinearLayout.LayoutParams(dp(48), dp(48)); iconLp.leftMargin = dp(9); iconLp.rightMargin = dp(8)
        item.addView(icon, iconLp)
        val label = TextView(this).apply {
            text = app.label; textSize = 13f; setTextColor(white); gravity = Gravity.CENTER_VERTICAL
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL); maxLines = 2
        }
        item.addView(label, LinearLayout.LayoutParams(0, -2, 1f))
        val lp = GridLayout.LayoutParams().apply {
            width = 0; height = dp(84); columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            setMargins(dp(3), dp(4), dp(3), dp(4))
        }
        item.layoutParams = lp
        return item
    }

    private fun updateClock() {
        findViewById<TextView>(1001)?.text = timeText()
        findViewById<TextView>(1002)?.text = dateText()
    }

    private fun buildSettings() {
        settingsMode = true
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; background = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(bg, Color.rgb(13, 20, 26))); setPadding(dp(15), dp(12), dp(15), dp(8)) }
        val header = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        val title = TextView(this).apply { text = "PILIH APLIKASI"; textSize = 20f; setTextColor(white); typeface = Typeface.DEFAULT_BOLD; letterSpacing = 0.04f }
        header.addView(title, LinearLayout.LayoutParams(0, -2, 1f))
        val back = button("KEMBALI", orange, Color.BLACK, 12f); back.setOnClickListener { buildHome() }; header.addView(back)
        root.addView(header)
        val hint = TextView(this).apply { text = "Pilih aplikasi yang ditampilkan di terminal RC Motor."; textSize = 12f; setTextColor(muted); setPadding(0, dp(9), 0, dp(10)) }
        root.addView(hint)
        val scroll = ScrollView(this)
        val list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val apps = getLaunchableApps(); val saved = getSelectedPackages()
        apps.forEach { app ->
            val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL; background = gradientCard(GradientDrawable.Orientation.LEFT_RIGHT, panelTop, panelBottom, Color.rgb(54, 66, 78), 13); setPadding(dp(10), dp(7), dp(9), dp(7)) }
            val icon = ImageView(this).apply { setImageDrawable(app.icon); scaleType = ImageView.ScaleType.FIT_CENTER }
            row.addView(icon, LinearLayout.LayoutParams(dp(38), dp(38)))
            val check = CheckBox(this).apply { text = app.label; textSize = 13f; setTextColor(white); isChecked = saved == null || saved.contains(app.packageName); buttonTintList = android.content.res.ColorStateList.valueOf(orange); setPadding(dp(7), 0, 0, 0) }
            check.setOnCheckedChangeListener { _, checked ->
                val current = getSelectedPackages() ?: apps.map { it.packageName }.toMutableSet()
                if (checked) current.add(app.packageName) else current.remove(app.packageName)
                prefs.edit().putStringSet("selected_apps", current.toSet()).apply()
            }
            row.addView(check, LinearLayout.LayoutParams(0, -2, 1f))
            val lp = LinearLayout.LayoutParams(-1, -2); lp.bottomMargin = dp(5); list.addView(row, lp)
        }
        scroll.addView(list); root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        val all = button("TAMPILKAN SEMUA APLIKASI", orange, Color.BLACK, 13f); all.setOnClickListener { prefs.edit().remove("selected_apps").apply(); buildSettings() }
        val allLp = LinearLayout.LayoutParams(-1, -2); allLp.topMargin = dp(8); root.addView(all, allLp)
        setContentView(root)
    }

    private data class LaunchApp(val label: String, val packageName: String, val icon: android.graphics.drawable.Drawable)

    @Suppress("DEPRECATION")
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
        if (launchIntent != null) try { startActivity(launchIntent) } catch (_: Exception) { Toast.makeText(this, "Tidak bisa membuka aplikasi", Toast.LENGTH_SHORT).show() }
    }

    private fun button(text: String, color: Int, textColor: Int, size: Float): Button = Button(this).apply {
        this.text = text; setTextColor(textColor); textSize = size; background = rounded(color, 12); isAllCaps = false
        minHeight = dp(42); minimumHeight = dp(42); setPadding(dp(8), 0, dp(8), 0)
        typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
    }

    private fun rounded(color: Int, radius: Int) = GradientDrawable().apply { setColor(color); cornerRadius = dp(radius).toFloat() }

    private fun gradientCard(orientation: GradientDrawable.Orientation, start: Int, end: Int, stroke: Int, radius: Int) = GradientDrawable(orientation, intArrayOf(start, end)).apply {
        cornerRadius = dp(radius).toFloat(); setStroke(dp(1), stroke)
    }

    private fun timeText() = SimpleDateFormat("HH:mm", Locale("id", "ID")).format(Date())
    private fun dateText() = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID")).format(Date())
    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
}
