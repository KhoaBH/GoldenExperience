package com.anhkhoa.goldenexperience

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.Button
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import com.anhkhoa.goldenexperience.adapters.MealSelectedAdapter
import com.anhkhoa.goldenexperience.models.Meal
import com.anhkhoa.goldenexperience.models.MealInPlan
import com.anhkhoa.goldenexperience.models.MealPlan
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

class MealPlanActivity : AppCompatActivity() {
    val db = FirebaseFirestore.getInstance()
    private val selectedMeals = mutableListOf<Meal>() // 🔥 Sửa lại đúng
    private val auth = FirebaseAuth.getInstance()
    private lateinit var selectedAdapter: MealSelectedAdapter
    private lateinit var allMeals: MutableList<Meal>
    private lateinit var adapter: MealAdapter
    private val selectedMealInPlans = mutableListOf<MealInPlan>()
    private lateinit var recyclerViewMeals: RecyclerView
    var  mealType = ""
    var planId = ""
    var currentUserId = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_meal_plan)

        val addBtn = findViewById<Button>(R.id.addBtn)
        val saveBtn = findViewById<Button>(R.id.btnSaveMeal)
        currentUserId = auth.currentUser?.uid ?: return
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_meal_plan)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        recyclerViewMeals = findViewById(R.id.recyclerViewMeals)
        selectedAdapter = MealSelectedAdapter(
            selectedMealInPlans,
            onRemoveClick = { meal ->
                selectedAdapter.removeMeal(meal)
                Log.d("DEBUG_MEAL_Remove", "selectedMealInPlans: ")
                updateNutritionUI()
                Toast.makeText(this, "Đã xoá món: ${meal.name}", Toast.LENGTH_SHORT).show()
            },
            onQuantityChanged = {
                updateNutritionUI()
            }
        )
        recyclerViewMeals.layoutManager = LinearLayoutManager(this)
        recyclerViewMeals.adapter = selectedAdapter

        planId = intent.getStringExtra("plan_id").toString()
        mealType = intent.getStringExtra("meal_type") ?: ""
        findViewById<TextView>(R.id.tvMealPlanTitle).text = mealType
        Log.d("Lỗi tải kế hoạch", "Đây là hàm load ${planId}" )
        addBtn.setOnClickListener { showSearchMealDialog() }
        saveBtn.setOnClickListener { saveMealPlan() }

        if (planId != null) {
            db.collection("mealPlans").document(planId)
                .get()
                .addOnSuccessListener { doc ->
                    val mealPlan = doc.toObject(MealPlan::class.java)
                    mealPlan?.meals?.let {
                        selectedMealInPlans.clear()
                        selectedMealInPlans.addAll(it)
                        selectedAdapter.setMeals(it)
                        Log.d("DEBUG_MEAL", "selectedMealInPlans: ${selectedAdapter.getCurrentMeals()}")
                        updateNutritionUI()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Lỗi load kế hoạch", Toast.LENGTH_SHORT).show()
                }
        } else {
            loadMealsForToday(mealType)
        }
    }

    private fun saveMealPlan() {
        val mealInPlans = selectedAdapter.getCurrentMeals()

        val mealType = intent.getStringExtra("meal_type") ?: ""
        val planIdFromIntent = intent.getStringExtra("plan_id")
        val finalPlanId = planIdFromIntent ?: db.collection("mealPlans").document().id

        val plan = MealPlan(
            planId = finalPlanId,
            userId = currentUserId,
            planName = "Plan hôm nay",
            mealType = mealType,
            meals = mealInPlans,
            createdAt = System.currentTimeMillis()
        )

        db.collection("mealPlans").document(finalPlanId)
            .set(plan)
            .addOnSuccessListener {
                Log.d("Firestore", "Lưu kế hoạch thành công")
                Toast.makeText(this, "Lưu kế hoạch thành công!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Lỗi lưu plan: ${e.message}")
                Toast.makeText(this, "Lưu kế hoạch thất bại!", Toast.LENGTH_SHORT).show()
            }
    }


    private fun showSearchMealDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_search_meal, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()
        dialog.show()

        val searchEditText = dialogView.findViewById<EditText>(R.id.searchMealEditText)
        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.recyclerSearchMeal)

        val filteredList = mutableListOf<Meal>()
        adapter = MealAdapter(
            filteredList,
            mode = MealAdapterMode.SELECT_WITH_ADD,
            onAddClick = { meal ->
                Log.d("MealPlanActivity", "User muốn thêm món: ${meal.name} (id=${meal.id})")

                selectedMealInPlans.forEach {
                    Log.d("MealPlanActivity", "- Đã chọn: ${it.meal.name} (id=${it.meal.id})")
                }

                if (selectedMealInPlans.any { it.meal.id == meal.id }.not()) {
                    selectedAdapter.addMeal(meal)
                    Log.d("MealPlanActivity", "selectedMealInPlans hash: ${System.identityHashCode(selectedMealInPlans)}")
                    Log.d("MealPlanActivity", "Món đã chọn hiện tại có: ${selectedMealInPlans.size} món")
                    updateNutritionUI()
                    Log.d("MealPlanActivity", "Đã thêm món: ${meal.name}")
                } else {
                    Log.d("MealPlanActivity", "Món đã được chọn rồi: ${meal.name}")
                    Toast.makeText(this, "Món này được chọn rồi!", Toast.LENGTH_SHORT).show()
                }
            },
            onMealClick = { meal ->
                val intent = Intent(this, MealDetail::class.java)
                intent.putExtra("meal_data", meal)
                startActivity(intent)
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        FirebaseFirestore.getInstance().collection("meals")
            .get()
            .addOnSuccessListener { result ->
                allMeals = mutableListOf()
                for (document in result) {
                    val meal = document.toObject(Meal::class.java)
                    meal.id = document.id  // Gán document id cho meal.id để sau này so sánh chính xác
                    allMeals.add(meal)
                }
                filteredList.clear()
                filteredList.addAll(allMeals)
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseLoad", "Lỗi khi load meals từ Firebase", e)
            }

        searchEditText.addTextChangedListener {
            val query = it.toString().trim().lowercase()
            val matched = allMeals.filter { meal ->
                meal.name.lowercase().contains(query)
            }
            filteredList.clear()
            filteredList.addAll(matched)
            adapter.notifyDataSetChanged()
        }
    }

    private fun calculateTotalNutrition(): Map<String, Float> {
        var totalProtein = 0f
        var totalFat = 0f
        var totalCarb = 0f
        var totalCalories = 0f

        val mealInPlans = selectedAdapter.getCurrentMeals()

        for (mealInPlan in mealInPlans) {
            val meal = mealInPlan.meal
            val qty = mealInPlan.quantity
            val unit = mealInPlan.unit
            val factor = if (meal.unit == "100g") qty / 100f else qty

            totalProtein += (meal.protein ?: 0f) * factor
            totalFat += (meal.fat ?: 0f) * factor
            totalCarb += (meal.carbs ?: 0f) * factor
            totalCalories += (meal.calories) * factor
        }

        return mapOf(
            "protein" to totalProtein,
            "fat" to totalFat,
            "carb" to totalCarb,
            "calories" to totalCalories
        )
    }



    private fun updateNutritionUI() {
        val total = calculateTotalNutrition()

        findViewById<TextView>(R.id.sum_protein).text = "${total["protein"]?.toInt()}g"
        findViewById<TextView>(R.id.sum_fat).text = "${total["fat"]?.toInt()}g"
        findViewById<TextView>(R.id.sum_carb).text = "${total["carb"]?.toInt()}g"
        findViewById<TextView>(R.id.sum_kcal).text = "${total["calories"]?.toInt()}"
    }

    private fun loadMealsForToday(mealType: String) {
        val startOfToday = getStartOfToday()
        val endOfToday = getEndOfToday()
        db.collection("mealPlans")
            .document(planId) // dùng trực tiếp document ID
            .get()
            .addOnSuccessListener { document ->
                if (!document.exists()) {
                    Toast.makeText(this, "Chưa có kế hoạch $mealType hôm nay", Toast.LENGTH_SHORT)
                        .show()
                    selectedMealInPlans.clear()
                    selectedAdapter.setMeals(selectedMealInPlans)
                    updateNutritionUI()
                    return@addOnSuccessListener
                }else
                {
                    Toast.makeText(this, "Da co ke hoach tu truươc $mealType hôm nay", Toast.LENGTH_SHORT)
                }
                val plan = document.toObject(MealPlan::class.java)
                selectedMealInPlans.clear()

                plan?.meals?.let { mealList ->
                    mealList.forEach { mip ->
                        mip.meal.id =
                            mip.meal.id.ifEmpty { mip.meal.id } // vẫn để nếu mày có xử lý riêng
                    }
                    selectedMealInPlans.addAll(mealList)
                }

                selectedAdapter.setMeals(selectedMealInPlans)
            }
    }

                fun getStartOfToday(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    fun getEndOfToday(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.timeInMillis
    }
}
