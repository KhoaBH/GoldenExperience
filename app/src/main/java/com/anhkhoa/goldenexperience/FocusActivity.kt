package com.anhkhoa.goldenexperience

import android.animation.ObjectAnimator
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.*
import android.widget.ImageButton
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.Group
import com.mut_jaeryo.circletimer.CircleTimer

class FocusActivity : AppCompatActivity() {

    private lateinit var timer: CircleTimer
    private lateinit var btnStart: Button
    private lateinit var btnMinus: ImageButton
    private lateinit var btnPlus: ImageButton
    private lateinit var tvTime: TextView
    private lateinit var tvStatus: TextView
    private lateinit var iconStatus: ImageView
    private lateinit var layoutTimePicker: LinearLayout
    private lateinit var layoutRoot: ConstraintLayout
    private lateinit var groupPreStart: Group
    private lateinit var groupTimer: Group
    private lateinit var btnAction: ImageView
    private lateinit var btnStop: ImageView
    private var focusTime = 15 * 60
    private val increment = 15 * 60
    private val restTime = 5 * 60
    private var isPaused = false
    private var isFocusPhase = true
    private var isTimerRunning = false

    private val focusColor = Color.parseColor("#5E7753")  // xanh lá đậm
    private val restColor = Color.parseColor("#FFFFFF")    // trắng
    private val restAltColor = Color.WHITE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_focus)

        timer = findViewById(R.id.main_timer)
        btnStart = findViewById(R.id.btn_start)
        btnPlus = findViewById(R.id.btn_plus)
        btnMinus = findViewById(R.id.btn_minus)
        tvTime = findViewById(R.id.tv_time)
        tvStatus = findViewById(R.id.tv_status)
        iconStatus = findViewById(R.id.icon_status)
        layoutTimePicker = findViewById(R.id.layout_time_picker)
        layoutRoot = findViewById(R.id.layout_root)
        groupPreStart = findViewById(R.id.group_pre_start)
        groupTimer = findViewById(R.id.group_timer)
        btnAction  = findViewById(R.id.btn_action)
        btnStop  = findViewById(R.id.btn_stop)
        updateTimeDisplay(focusTime)

        btnPlus.setOnClickListener {
            focusTime += increment
            updateTimeDisplay(focusTime)
        }

        btnMinus.setOnClickListener {
            if (focusTime > increment) {
                focusTime -= increment
                updateTimeDisplay(focusTime)
            }
        }

        groupPreStart.visibility = View.VISIBLE
        groupTimer.visibility = View.GONE
        btnAction.setOnClickListener {
            if (isTimerRunning) {
                if (isPaused) {
                    timer.start()
                    btnAction.setImageResource(R.drawable.ic_pause_green)
                    isPaused = false
                } else {
                    timer.stop()
                    btnAction.setImageResource(R.drawable.ic_play)
                    isPaused = true
                }
            }
        }
        btnStop.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Dừng phiên?")
                .setMessage("Mày có chắc muốn dừng không? Phiên này sẽ reset về đầu á.")
                .setPositiveButton("Dừng luôn") { dialog, _ ->
                    timer.reset()
                    resetUI()
                }
                .setNegativeButton("Hủy", null)
                .show()
        }
        btnStart.setOnClickListener {
            groupPreStart.visibility = View.GONE
            groupTimer.visibility = View.VISIBLE
            startFocusSession()
        }
    }

    private fun updateTimeDisplay(seconds: Int) {
        val minutes = seconds / 60
        tvTime.text = "$minutes : 00"
    }

    private fun startFocusSession() {
        isTimerRunning = true
        isFocusPhase = true

        layoutTimePicker.visibility = View.GONE
        btnStart.visibility = View.GONE
        timer.visibility = View.VISIBLE

        setPhaseBackground(isFocus = true)

        timer.setMaximumTime(10)
        timer.setInitPosition(5)

        timer.setBaseTimerEndedListener(object : CircleTimer.baseTimerEndedListener {
            override fun OnEnded() {
                if (isFocusPhase) {
                    isFocusPhase = false
                    startRestPhase()
                } else {
                    isTimerRunning = false
                    Toast.makeText(this@FocusActivity, "Hoàn thành chu kỳ bro!", Toast.LENGTH_SHORT).show()
                    resetUI()
                }
            }
        })

        timer.start()
    }

    private fun startRestPhase() {
        setPhaseBackground(isFocus = false)
        timer.setMaximumTime(restTime)
        timer.setInitPosition(restTime)
        timer.start()
    }

    private fun resetUI() {
        layoutTimePicker.visibility = View.VISIBLE
        btnStart.visibility = View.VISIBLE
        groupPreStart.visibility = View.VISIBLE
        groupTimer.visibility = View.GONE
    }

    private fun setPhaseBackground(isFocus: Boolean) {
        val fromColor = (layoutRoot.background as? ColorDrawable)?.color ?: Color.WHITE
        val toColor = if (isFocus) {
            focusColor // xanh lá đậm cho tập trung
        } else {
            Color.parseColor("#37474F") // màu xanh nhạt, dịu mắt cho nghỉ
        }

        val colorAnim = ObjectAnimator.ofArgb(layoutRoot, "backgroundColor", fromColor, toColor)
        colorAnim.duration = 800
        colorAnim.start()

        tvStatus.text = if (isFocus) "Focus Time" else "Short Break"

        if (isFocus) {
            iconStatus.setImageResource(R.drawable.ic_work)
            iconStatus.setColorFilter(Color.WHITE)
        } else {
            iconStatus.setImageResource(R.drawable.ic_rest) // icon nghỉ (đổi cho hợp)
        }
    }



    override fun onPause() {
        super.onPause()
        if (isTimerRunning) {
            Toast.makeText(this, "Thoát giữa chừng trừ điểm bro!", Toast.LENGTH_SHORT).show()
        }
    }
}
