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
import androidx.core.widget.addTextChangedListener
import com.anhkhoa.goldenexperience.adapters.MealSelectedAdapter
import com.anhkhoa.goldenexperience.models.Meal
import com.google.firebase.firestore.FirebaseFirestore

class MealPlanActivity : AppCompatActivity() {
    private val selectedMeals = mutableListOf<Meal>()
    private lateinit var selectedAdapter: MealSelectedAdapter
    private lateinit var allMeals: MutableList<Meal> // Dữ liệu từ Firebase
    private lateinit var adapter: MealAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_meal_plan)

        val addBtn = findViewById<Button>(R.id.addBtn)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_meal_plan)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        addBtn.setOnClickListener {
            showSearchMealDialog()
        }
        val recyclerViewMeals = findViewById<RecyclerView>(R.id.recyclerViewMeals)
        selectedAdapter = MealSelectedAdapter(
            selectedMeals
        )
        recyclerViewMeals.layoutManager = LinearLayoutManager(this)
        recyclerViewMeals.adapter = selectedAdapter


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
        adapter = MealAdapter(filteredList,mode = MealAdapterMode.SELECT_WITH_ADD,
            onAddClick = { meal ->
                selectedAdapter.addMeal(meal)
                Toast.makeText(this, "Add món: ${meal.name}", Toast.LENGTH_SHORT).show()
            },
            onMealClick = { meal ->
                val intent = Intent(this, MealDetail::class.java)
                intent.putExtra("meal_data", meal)
                this?.startActivity(intent)
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Load data từ Firebase
        FirebaseFirestore.getInstance().collection("meals")
            .get()
            .addOnSuccessListener { result ->
                allMeals = mutableListOf()
                for (document in result) {
                    val meal = document.toObject(Meal::class.java)
                    allMeals.add(meal)
                }
                filteredList.clear()
                filteredList.addAll(allMeals)
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseLoad", "Lỗi khi load meals từ Firebase", e)
            }

        // Search filter
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
}
