package com.anhkhoa.goldenexperience

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.anhkhoa.goldenexperience.models.Meal

class MealDetail : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_meal_detail)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // Lấy intent data
        // Gán View
        val meal = intent.getSerializableExtra("meal_data") as? Meal

        val btnBack = findViewById<Button>(R.id.btnBack)
        val imgMeal = findViewById<ImageView>(R.id.imgMeal)
        val mealName = findViewById<TextView>(R.id.meal_name)
        val calories = findViewById<TextView>(R.id.calories)
        val mealType = findViewById<TextView>(R.id.meal_type)
        val protein = findViewById<TextView>(R.id.protein)
        val fat = findViewById<TextView>(R.id.fat)
        val carb = findViewById<TextView>(R.id.carb)
        val mealDescription = findViewById<TextView>(R.id.meal_description)

        // Gán data vào view
        meal?.let {
            mealName.text = it.name
            calories.text = "${it.calories} Kcal"
            mealType.text = it.type
            protein.text = "Protein ${it.protein}g"
            fat.text = "Fat ${it.fat}g"
            carb.text = "Carb ${it.carbs}g"
            mealDescription.text = it.description
        }

        // Nút back
        btnBack.setOnClickListener {
            finish()
        }
    }
    }