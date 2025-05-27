package com.anhkhoa.goldenexperience

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.anhkhoa.goldenexperience.databinding.ItemMealBinding
import com.anhkhoa.goldenexperience.models.Meal
enum class MealAdapterMode {
    DEFAULT,        // Mặc định: có nút "Chi tiết"
    SELECT_WITH_ADD // Activity khác: có nút "Add"
}

class MealAdapter(
    private val meals: List<Meal>,
    private val mode: MealAdapterMode = MealAdapterMode.DEFAULT,
    private val onAddClick: (Meal) -> Unit,
    private val onMealClick: (Meal) -> Unit
) : RecyclerView.Adapter<MealAdapter.MealViewHolder>() {

    inner class MealViewHolder(val binding: ItemMealBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MealViewHolder {
        val binding = ItemMealBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MealViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MealViewHolder, position: Int) {
        val meal = meals[position]
        with(holder.binding) {
            txtMealName.text = meal.name
            txtCalories.text = "${meal.calories} Kcal"
            txtType.text = "Type: ${meal.type}"

            when (mode) {
                MealAdapterMode.DEFAULT -> {
                    DetailBtn.visibility = View.VISIBLE
                    AddBtn.visibility = View.GONE
                    DetailBtn.setOnClickListener {
                        onMealClick(meal)
                    }
                }

                MealAdapterMode.SELECT_WITH_ADD -> {
                    AddBtn.visibility = View.VISIBLE
                    DetailBtn.visibility = View.VISIBLE
                    AddBtn.setOnClickListener {
                        onAddClick(meal)
                    }
                    DetailBtn.setOnClickListener {
                        onMealClick(meal)
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int = meals.size
}

