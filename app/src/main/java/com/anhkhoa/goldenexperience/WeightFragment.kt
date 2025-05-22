package com.anhkhoa.goldenexperience

import android.app.AlertDialog
import android.graphics.Color
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.anhkhoa.goldenexperience.models.User
import com.anhkhoa.goldenexperience.repository.WeightRepository
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date
import java.util.Locale
import kotlin.math.sqrt

/**
 * A simple [Fragment] subclass.
 * Use the [WeightFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class WeightFragment : Fragment() {
    private val auth = FirebaseAuth.getInstance()
    private lateinit var txtCurrentWeightValue: TextView
    private lateinit var txtBMI: TextView
    private lateinit var txtBMIStatus: TextView
    private lateinit var edtTarget: TextView
    private lateinit var setTarget: Button
    private lateinit var updateWeight: Button
    private val weightRepository = WeightRepository()
    private lateinit var txtCurrentHeightValue: TextView
    private lateinit var updateHeight: Button
    private val db = FirebaseFirestore.getInstance()
    private lateinit var lineChart: LineChart
    private var currentUser: User? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_weight, container, false)
        lineChart = view.findViewById(R.id.LineChart)

        // Lấy dữ liệu cân nặng từ Firestore

        txtCurrentWeightValue = view.findViewById(R.id.txtCurrentWeightValue)
        txtBMI = view.findViewById(R.id.BMI)
        edtTarget = view.findViewById(R.id.edtTarget)
        txtBMIStatus = view.findViewById(R.id.BMIStatus)
        updateWeight = view.findViewById(R.id.btnUpdateWeight) // Thêm EditText vào XML nếu chưa có
        setTarget = view.findViewById(R.id.btnSetTarget) // Thêm Button vào XML nếu chưa có
        updateHeight = view.findViewById(R.id.btnUpdateHeight)
        txtCurrentHeightValue = view.findViewById(R.id.txtCurrentHeightValue)
        // Lấy dữ liệu cân nặng mới nhất từ Firestore
        loadUserFromFirestore()

        loadWeightData()
        setTarget.setOnClickListener {
            val editText = EditText(requireContext())
            editText.hint = "Enter new target (kg)"
            editText.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL

            AlertDialog.Builder(requireContext())
                .setTitle("Update target")
                .setView(editText)
                .setPositiveButton("Lưu") { _, _ ->
                    val newTarget = editText.text.toString().toDoubleOrNull()
                    if (newTarget != null) {
                        saveTarget(newTarget)
                    } else {
                        Toast.makeText(context, "Chiều cao không hợp lệ", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Hủy", null)
                .show()
        }
        updateHeight.setOnClickListener {
            val editText = EditText(requireContext())
            editText.hint = "Nhập chiều cao (cm)"
            editText.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL

            AlertDialog.Builder(requireContext())
                .setTitle("Cập nhật chiều cao")
                .setView(editText)
                .setPositiveButton("Lưu") { _, _ ->
                    val newHeight = editText.text.toString().toDoubleOrNull()
                    if (newHeight != null) {
                        saveHeight(newHeight)
                    } else {
                        Toast.makeText(context, "Chiều cao không hợp lệ", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Hủy", null)
                .show()
        }

        updateWeight.setOnClickListener {
            val editText = EditText(requireContext())
            editText.hint = "Nhập cân nặng mới (kg)"
            editText.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL

            AlertDialog.Builder(requireContext())
                .setTitle("Cập nhật cân nặng")
                .setView(editText)
                .setPositiveButton("Lưu") { _, _ ->
                    val newWeight = editText.text.toString().toDoubleOrNull()
                    if (newWeight != null) {
                        saveWeight(newWeight)
                    } else {
                        Toast.makeText(context, "Cân nặng không hợp lệ", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Hủy", null)
                .show()
        }

        // Lưu cân nặng khi nhấn nút

        return view
    }
    private fun saveHeight(height: Double) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            Toast.makeText(requireContext(), "Người dùng chưa đăng nhập!", Toast.LENGTH_SHORT).show()
            return
        }
        val userDocRef = FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
        userDocRef.update("height", height)
            .addOnSuccessListener {
                loadUserFromFirestore()
                Toast.makeText(
                    requireContext(),
                    "Cập nhật chiều cao thành công!",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    requireContext(),
                    "Lỗi khi cập nhật: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
    private fun saveTarget(target: Double) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            Toast.makeText(requireContext(), "Người dùng chưa đăng nhập!", Toast.LENGTH_SHORT).show()
            return
        }
        val userDocRef = FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
        userDocRef.update("target", target)
            .addOnSuccessListener {
                loadUserFromFirestore()
                Toast.makeText(
                    requireContext(),
                    "Success!",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    requireContext(),
                    "Lỗi khi cập nhật: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
    private fun saveWeight(weight: Double) {
        weightRepository.saveWeight(
            weight = weight,
            onSuccess = {
                loadUserFromFirestore()
                loadWeightData()
                Toast.makeText(
                    requireContext(),
                    "Cập nhật cân nặng thành công!",
                    Toast.LENGTH_SHORT
                ).show()
            },
            onFailure = { e ->
                Toast.makeText(
                    requireContext(),
                    "Lỗi lưu dữ liệu: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
    }

    private fun loadWeightData() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            Toast.makeText(requireContext(), "Người dùng chưa đăng nhập!", Toast.LENGTH_SHORT).show()
            Log.d("WeightFragment", "User is not logged in.")
            return
        }
        var targetWeight = 0f
        currentUser?.let { user ->
            targetWeight = user.target.toFloat()
        }
        weightRepository.getAllWeights(
            onResult = { weights ->
                if (weights.isEmpty()) {
                    Log.d("WeightFragment", "No weight data found for the user.")
                    return@getAllWeights
                }

                val entries = weights.map {
                    Entry(it.timestamp.toDate().time.toFloat(), it.weight.toFloat())
                }

                val dataSet = LineDataSet(entries, "Cân nặng theo thời gian").apply {
                    color = ContextCompat.getColor(requireContext(), R.color.purple_500)
                    setCircleColor(Color.RED)
                    circleRadius = 5f
                    setDrawCircleHole(false)
                    setDrawValues(false)
                    setDrawFilled(false)
                    fillColor = ContextCompat.getColor(requireContext(), R.color.teal_200)
                    lineWidth = 2f
                    mode = LineDataSet.Mode.CUBIC_BEZIER
                }
                val range = 5f // khoảng lệch trên/dưới, tuỳ chỉnh
                val maxWeight = weights.maxOf { it.weight + range}.toFloat()
                val minWeight = weights.minOf { it.weight - range}.toFloat()

                val yAxis = lineChart.axisLeft
                val minY = minOf(minWeight , targetWeight - 1f)
                val maxY = maxOf(maxWeight , targetWeight + 1f)

                yAxis.axisMinimum = minY
                yAxis.axisMaximum = maxY
                yAxis.granularity = 1f
                yAxis.isGranularityEnabled = true
                lineChart.axisRight.isEnabled = false

                // ➕ Thêm target line
                val targetEntries = listOf(
                    Entry(entries.first().x, targetWeight),
                    Entry(entries.last().x, targetWeight)
                )
                val targetDataSet = LineDataSet(targetEntries, "Mục tiêu").apply {
                    color = Color.BLUE
                    lineWidth = 2.5f
                    setDrawCircles(false)
                    enableDashedLine(10f, 10f, 0f)
                    setDrawValues(false)
                }

                val lineData = LineData(dataSet, targetDataSet)
                lineChart.data = lineData

                val xAxis = lineChart.xAxis
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.setDrawGridLines(false)
                xAxis.granularity = 24 * 60 * 60 * 1000f
                xAxis.valueFormatter = object : ValueFormatter() {
                    private val sdf = SimpleDateFormat("dd/MM", Locale.getDefault())
                    override fun getFormattedValue(value: Float): String {
                        return sdf.format(Date(value.toLong()))
                    }
                }

                lineChart.description.isEnabled = false
                lineChart.setTouchEnabled(true)
                lineChart.setPinchZoom(true)
                lineChart.invalidate()

                Log.d("WeightFragment", "LineChart updated with weight data.")
            },
            onFailure = { e ->
                Log.e("WeightFragment", "Error loading data: ${e.message}")
                Toast.makeText(
                    requireContext(),
                    "Lỗi tải dữ liệu: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
    }

    private fun calculateBMI(weight: Double, height: Double): Double{
        return weight/ Math.pow((height/100),2.0)
    }
    private fun loadUserFromFirestore() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                currentUser = document.toObject(User::class.java)!!
                updateUserUI()
                loadWeightData()

            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Lỗi tải thông tin người dùng", Toast.LENGTH_SHORT).show()
            }

    }
    private fun updateUserUI() {
        currentUser?.let { user ->
            txtCurrentHeightValue.text = "${user.height} cm"
            txtCurrentWeightValue.text = "${user.latestWeight} kg"
            edtTarget.text = "${user.target} kg"
            val bmi = calculateBMI(user.latestWeight, user.height)
            txtBMI.text = "%.2f ".format(bmi)

            val status = when {
                bmi < 18.5 -> "Under Weight"
                bmi < 25 -> "Normal"
                bmi < 30 -> "Over Weight"
                bmi < 35 -> "Obese"
                else -> "Severely Obese"
            }
            txtBMIStatus.text = status
        }
    }



}