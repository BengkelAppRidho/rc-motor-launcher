package id.rcmotor.launcher

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : Activity() {
    private val dark = Color.rgb(24, 32, 40)
    private val tile = Color.rgb(43, 56, 70)
    private val accent = Color.rgb(255, 170, 40)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildHome()
    }

    override fun onResume() {
        super.onResume()
        buildHome()
    }

    private fun buildHome() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(dark)
            setPadding(dp(20), dp(24), dp(20), dp(18))
        }

        val header = TextView(this).apply {
            text = "RC MOTOR"
            textSize = 22f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
        }
        root.addView(header)

        val clock = TextView(this).apply {
            text = SimpleDateFormat("HH:mm", Locale("id", "ID")).format(Date())
            textSize = 48f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, dp(14), 0, 0)
        }
        root.addView(clock)

        val date = TextView(this).apply {
            text = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID")).format(Date())
            textSize = 15f
            setTextColor(Color.LTGRAY)
            setPadding(0, 0, 0, dp(22))
        }
        root.addView(date)

        val grid = GridLayout(this).apply {
            columnCount = 2
            useDefaultMargins = false
        }
        val apps = getLaunchableApps()
        apps.forEach { app ->
            val item = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setBackgroundColor(tile)
                setPadding(dp(10), dp(14), dp(10), dp(14))
                val params = GridLayout.LayoutParams().apply {
                    width = 0
                    height = dp(112)
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    setMargins(dp(5), dp(5), dp(5), dp(5))
                }
                layoutParams = params
                isClickable = true
                isFocusable = true
                setOnClickListener { launchApp(app.packageName) }
            }

            val icon = TextView(this).apply {
                text = "●"
                textSize = 24f
                setTextColor(accent)
                gravity = Gravity.CENTER
            }
            val label = TextView(this).apply {
                text = app.label
                textSize = 14f
                setTextColor(Color.WHITE)
                gravity = Gravity.CENTER
                maxLines = 2
                typeface = Typeface.DEFAULT_BOLD
                setPadding(0, dp(7), 0, 0)
            }
            item.addView(icon)
            item.addView(label)
            grid.addView(item)
        }
        root.addView(grid)

        val footer = TextView(this).apply {
            text = "TERMINAL RC MOTOR"
            textSize = 12f
            setTextColor(Color.GRAY)
            gravity = Gravity.CENTER
            setPadding(0, dp(18), 0, 0)
        }
        root.addView(footer)
        setContentView(root)
    }

    private data class LaunchApp(val label: String, val packageName: String)

    private fun getLaunchableApps(): List<LaunchApp> {
        val pm = packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        return pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)
            .map {
                LaunchApp(
                    it.loadLabel(pm).toString(),
                    it.activityInfo.packageName
                )
            }
            .filter { it.packageName != packageName }
            .distinctBy { it.packageName }
            .sortedBy { it.label.lowercase(Locale.getDefault()) }
    }

    private fun launchApp(packageName: String) {
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        if (launchIntent != null) {
            try {
                startActivity(launchIntent)
            } catch (e: Exception) {
                Toast.makeText(this, "Tidak bisa membuka aplikasi", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()
}
