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
    private var focusTime = 10 * 60 // mặc định 25 phút, có thể tăng giảm
    private val increment = 15 * 60
    private val restTime = 5 * 60
    private var isPaused = false
    private var isFocusPhase = false
    private var isTimerRunning = false
    private var cycleCount = 0
    private val maxCycles = 4
    var totalFocusTimeLeft = 0
    private lateinit var btnBack: ImageButton
    private val focusColor = Color.parseColor("#5E7753")  // xanh lá đậm
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
        btnBack  = findViewById(R.id.btnBack)
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
        btnBack.setOnClickListener{
            if(isFocusPhase){
                val dialogView = layoutInflater.inflate(R.layout.dialog_confirm_stop, null)
                val dialog = android.app.AlertDialog.Builder(this)
                    .setView(dialogView)
                    .setCancelable(true)
                    .create()

                val btnCancel = dialogView.findViewById<Button>(R.id.btn_cancel)
                val btnConfirm = dialogView.findViewById<Button>(R.id.btn_confirm)

                btnCancel.setOnClickListener {
                    dialog.dismiss()
                }

                btnConfirm.setOnClickListener {
                    timer.reset()
                    cycleCount = 0
                    isTimerRunning = false
                    isPaused = false
                    super.onBackPressed()
                }

                dialog.window?.setBackgroundDrawableResource(android.R.color.transparent) // xoá viền mặc định
                dialog.window?.attributes?.windowAnimations = R.style.DialogFadeAnimation
                dialog.show()
            }
            else
                super.onBackPressed()
        }
        btnStop.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.dialog_confirm_stop, null)
            val dialog = android.app.AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create()

            val btnCancel = dialogView.findViewById<Button>(R.id.btn_cancel)
            val btnConfirm = dialogView.findViewById<Button>(R.id.btn_confirm)

            btnCancel.setOnClickListener {
                dialog.dismiss()
            }

            btnConfirm.setOnClickListener {
                timer.reset()
                resetUI()
                cycleCount = 0
                isTimerRunning = false
                isPaused = false
                dialog.dismiss()
            }

            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent) // xoá viền mặc định
            dialog.window?.attributes?.windowAnimations = R.style.DialogFadeAnimation
            dialog.show()
        }

        btnStart.setOnClickListener {
            groupPreStart.visibility = View.GONE
            groupTimer.visibility = View.VISIBLE
            cycleCount = 0
            isFocusPhase = true
            totalFocusTimeLeft = focusTime
            startPhase()
        }

    }

    private fun updateTimeDisplay(seconds: Int) {
        val minutes = seconds / 60
        tvTime.text = "$minutes : 00"
    }

    private fun startPhase() {
        if (cycleCount >= maxCycles) {
            Toast.makeText(this, "Hoàn thành $maxCycles chu kỳ Pomodoro, nghỉ dài thôi bro!", Toast.LENGTH_SHORT).show()
            resetUI()
            isTimerRunning = false
            return
        }

        isTimerRunning = true
        timer.reset()

        if (isFocusPhase) {
            setPhaseBackground(true)
            if(totalFocusTimeLeft > 1500){
                timer.setMaximumTime(1500)
                timer.setInitPosition(1500)
                totalFocusTimeLeft -= 1500
            }
            else
            {
                timer.setMaximumTime(totalFocusTimeLeft)
                timer.setInitPosition(totalFocusTimeLeft)
            }
            updateTimeDisplay(totalFocusTimeLeft)
        } else {
            setPhaseBackground(false)
            timer.setMaximumTime(300)
            timer.setInitPosition(300)
            updateTimeDisplay(restTime)
        }

        timer.setBaseTimerEndedListener(object : CircleTimer.baseTimerEndedListener {
            override fun OnEnded() {
                if (isFocusPhase) {
                    isFocusPhase = false
                } else {
                    isFocusPhase = true
                    cycleCount++
                }
                startPhase()
            }
        })

        timer.start()
    }

    private fun resetUI() {
        layoutTimePicker.visibility = View.VISIBLE
        btnStart.visibility = View.VISIBLE
        groupPreStart.visibility = View.VISIBLE
        groupTimer.visibility = View.GONE
        isPaused = false
        btnAction.setImageResource(R.drawable.ic_play)
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
            iconStatus.setImageResource(R.drawable.ic_rest) // icon nghỉ
        }
    }

    override fun onPause() {
        super.onPause()
        if (isTimerRunning && !isPaused) {
            val alertDialog = AlertDialog.Builder(this)
                .setTitle("Session Interrupted")
                .setMessage("You exited during a focus session. This session will be cancelled and not counted in your stats.")
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                    timer.stop()
                    isPaused = false
                    isTimerRunning = false
                    resetUI()
                    // Optionally reset UI, like resetting the timer
                }
                .setCancelable(false)
                .create()
            alertDialog.show()
        }

    }
}
