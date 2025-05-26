package com.anhkhoa.goldenexperience

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.anhkhoa.goldenexperience.databinding.ItemMealBinding
import com.anhkhoa.goldenexperience.models.Meal

class MealAdapter(private val meals: List<Meal>) :
    RecyclerView.Adapter<MealAdapter.MealViewHolder>() {

    inner class MealViewHolder(val binding: ItemMealBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MealViewHolder {
        val binding = ItemMealBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MealViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MealViewHolder, position: Int) {
        val meal = meals[position]
        holder.binding.txtMealName.text = meal.name
        holder.binding.txtCalories.text = "${meal.calories} Kcal"
        holder.binding.txtType.text = "Type: ${meal.type}"
        holder.binding.txtCalories.text = "${meal.calories} Kcal"
    }

    override fun getItemCount(): Int = meals.size
}
