package com.anhkhoa.goldenexperience

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.anhkhoa.goldenexperience.databinding.FragmentMealPrepBinding
import com.anhkhoa.goldenexperience.models.Meal
import com.anhkhoa.goldenexperience.models.MealPlan
import com.anhkhoa.goldenexperience.models.calculateNutrition
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class MealPrepFragment : Fragment() {

    private var _binding: FragmentMealPrepBinding? = null
    private val binding get() = _binding!!
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private var allMeals = emptyList<Meal>()
    private val filteredMeals = mutableListOf<Meal>()
    private lateinit var adapter: MealAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMealPrepBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadWeeklyCaloriesChart()
        setupMealPlanIcons()
        setupRecyclerView()
        setupSearchBar()
        loadMealsFromFirestore()
        fetchAndUpdateNutrition()
    }

    override fun onStart() {
        super.onStart()
        setupMealPlanIcons()
        fetchAndUpdateNutrition()
        loadWeeklyCaloriesChart()

    }

    private fun setupRecyclerView() {
        adapter = MealAdapter(
            filteredMeals,
            onAddClick = { meal -> Toast.makeText(context, "Add món: ${meal.name}", Toast.LENGTH_SHORT).show() },
            onMealClick = { meal ->
                val intent = Intent(context, MealDetail::class.java)
                intent.putExtra("meal_data", meal)
                startActivity(intent)
            }
        )
        binding.rvMeals.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMeals.adapter = adapter
    }

    private fun setupSearchBar() {
        val searchEditText = binding.foodSearchBar.searchEditText
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim().lowercase()
                filteredMeals.clear()
                if (query.isEmpty()) {
                    binding.rvMeals.visibility = View.GONE
                } else {
                    filteredMeals.addAll(allMeals.filter { it.name.lowercase().startsWith(query) })
                    binding.rvMeals.visibility = View.VISIBLE
                }
                adapter.notifyDataSetChanged()
            }
        })
    }

    private fun loadMealsFromFirestore() {
        db.collection("meals")
            .get()
            .addOnSuccessListener { result ->
                allMeals = result.mapNotNull { it.toObject(Meal::class.java) }
                filteredMeals.clear()
                filteredMeals.addAll(allMeals)
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseLoad", "Error getting meals", e)
            }
    }

    private fun setupMealPlanIcons() {
        loadTodayMealPlanIds(
            onResult = { planMap ->
                val startActivityForType = { type: String ->
                    val intent = Intent(requireContext(), MealPlanActivity::class.java)
                    intent.putExtra("meal_type", type)
                    intent.putExtra("plan_id", planMap[type])
                    startActivity(intent)
                }
                binding.breakfastIcon.setOnClickListener { startActivityForType("Breakfast") }
                binding.lunchActivity.setOnClickListener { startActivityForType("Lunch") }
                binding.DinnerActivity.setOnClickListener { startActivityForType("Dinner") }
                binding.snackActivity.setOnClickListener { startActivityForType("Snack") }
            },
            onError = {
                Toast.makeText(requireContext(), "Lỗi lấy meal plan: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        )
    }
    fun calculateTDEE(weightKg: Float, heightCm: Float, age: Int, gender: String): Int {
        val bmr = if (gender.lowercase() == "male") {
            66.47f + 13.75f * weightKg + 5.003f * heightCm - 6.755f * age
        } else {
            655.1f + 9.563f * weightKg + 1.850f * heightCm - 4.676f * age
        }
        val activityFactor = 1.55f // trung bình
        return (bmr * activityFactor).toInt()
    }

    private fun fetchAndUpdateNutrition() {
        val userId = auth.currentUser?.uid ?: return
        val todayStart = getStartOfTodayMillis()
        val todayEnd = getEndOfTodayMillis()

        db.collection("users").document(userId).get()
            .addOnSuccessListener { userDoc ->
                val gender = userDoc.getString("gender") ?: "male"
                val height = userDoc.getDouble("height")?.toFloat() ?: 170f
                val weight = userDoc.getDouble("latestWeight")?.toFloat() ?: 65f
                val age = userDoc.getLong("age")?.toInt() ?: 22

                val tdee = calculateTDEE(weight, height, age, gender)
                // Tính lượng dinh dưỡng cần thiết từ TDEE
                val neededProtein = (tdee * 0.20 / 4).toInt()  // 20% calo cho protein
                val neededCarbs = (tdee * 0.50 / 4).toInt()    // 50% calo cho carb
                val neededFat = (tdee * 0.30 / 9).toInt()      // 30% calo cho fat

                db.collection("mealPlans")
                    .whereEqualTo("userId", userId)
                    .whereGreaterThanOrEqualTo("createdAt", todayStart)
                    .whereLessThanOrEqualTo("createdAt", todayEnd)
                    .get()
                    .addOnSuccessListener { result ->
                        val mealPlans = result.mapNotNull { it.toObject(MealPlan::class.java) }

                        var totalCalories = 0
                        var totalProtein = 0f
                        var totalCarbs = 0f
                        var totalFat = 0f

                        for (plan in mealPlans) {
                            for (mealInPlan in plan.meals) {
                                val factor = if (mealInPlan.unit == "Serving") {
                                    mealInPlan.quantity / 1f
                                } else {
                                    mealInPlan.quantity / 100f
                                }
                                val meal = mealInPlan.meal ?: continue
                                totalCalories += (meal.calories * factor).toInt()
                                totalProtein += meal.protein * factor
                                totalCarbs += meal.carbs * factor
                                totalFat += meal.fat * factor
                            }
                        }

                        // Cập nhật text
                        binding.txtTotalCalories.text = "$totalCalories/$tdee Kcal"
                        binding.txtProteinValue.text = "${totalProtein.toInt()}/$neededProtein g"
                        binding.txtCarbsValue.text = "${totalCarbs.toInt()}/$neededCarbs g"
                        binding.txtFatsValue.text = "${totalFat.toInt()}/$neededFat g"

                        // Set progress
                        val progress = ((totalCalories.toFloat() / tdee) * 100).toInt().coerceIn(0, 100)
                        binding.circleProgress.setProgressCompat(progress, true)
                    }
                    .addOnFailureListener { e ->
                        Log.e("MealPlan", "Lỗi khi tải kế hoạch hôm nay", e)
                    }
            }
            .addOnFailureListener { e ->
                Log.e("UserData", "Lỗi khi lấy dữ liệu user", e)
            }
    }

    private fun loadWeeklyCaloriesChart() {
        val userId = auth.currentUser?.uid ?: return
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val barEntries = ArrayList<BarEntry>()
        val dayLabels = mutableListOf<String>()
        val dateList = mutableListOf<Long>()

        // Lấy 7 ngày gần nhất (tính từ hôm nay về trước)
        for (i in 6 downTo 0) {
            calendar.timeInMillis = System.currentTimeMillis()
            calendar.add(Calendar.DAY_OF_YEAR, -i)
            val start = calendar.timeInMillis

            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            val end = calendar.timeInMillis

            dateList.add(start)
            dayLabels.add(SimpleDateFormat("EEE", Locale.getDefault()).format(Date(start)))
        }

        val caloriesPerDay = MutableList(7) { 0 }

        db.collection("mealPlans")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { result ->
                val allPlans = result.mapNotNull { it.toObject(MealPlan::class.java) }

                for ((index, dayStart) in dateList.withIndex()) {
                    val dayEnd = dayStart + 24 * 60 * 60 * 1000
                    val dayPlans = allPlans.filter {
                        it.createdAt in dayStart..dayEnd
                    }

                    for (plan in dayPlans) {
                        for (mealInPlan in plan.meals) {
                            val factor = if (mealInPlan.unit == "Serving") mealInPlan.quantity else mealInPlan.quantity / 100
                            val meal = mealInPlan.meal ?: continue
                            caloriesPerDay[index] += (meal.calories * factor).toInt()
                        }
                    }
                    barEntries.add(BarEntry(index.toFloat(), caloriesPerDay[index].toFloat()))
                }

                // Setup biểu đồ
                val barDataSet = BarDataSet(barEntries, "Calories")
                barDataSet.color = resources.getColor(R.color.my_soft_pink, null)
                val barData = BarData(barDataSet)

                binding.barChart.apply {
                    data = barData
                    description.isEnabled = false
                    xAxis.valueFormatter = IndexAxisValueFormatter(dayLabels)
                    xAxis.granularity = 1f
                    xAxis.setDrawGridLines(false)
                    xAxis.labelRotationAngle = -45f
                    xAxis.position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
                    axisLeft.axisMinimum = 0f
                    axisRight.isEnabled = false
                    animateY(1000)
                    invalidate()
                }
            }
            .addOnFailureListener { e ->
                Log.e("Chart", "Lỗi khi tải dữ liệu biểu đồ", e)
            }
    }

    private fun loadTodayMealPlanIds(
        onResult: (Map<String, String>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val userId = auth.currentUser?.uid ?: return
        val todayStart = getStartOfTodayMillis()
        val todayEnd = getEndOfTodayMillis()
        val mealTypes = listOf("Breakfast", "Lunch", "Dinner", "Snack")
        val resultMap = mutableMapOf<String, String>()

        var doneCount = 0
        for (type in mealTypes) {
            db.collection("mealPlans")
                .whereEqualTo("userId", userId)
                .whereEqualTo("mealType", type)
                .whereGreaterThanOrEqualTo("createdAt", todayStart)
                .whereLessThanOrEqualTo("createdAt", todayEnd)
                .get()
                .addOnSuccessListener { documents ->
                    documents.firstOrNull()?.getString("planId")?.let {
                        resultMap[type] = it
                    }
                    doneCount++
                    if (doneCount == mealTypes.size) {
                        onResult(resultMap)
                    }
                }
                .addOnFailureListener { e -> onError(e) }
        }
    }

    private fun getStartOfTodayMillis(): Long = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    private fun getEndOfTodayMillis(): Long = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
    }.timeInMillis

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
