package com.anhkhoa.goldenexperience.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.anhkhoa.goldenexperience.R
import com.anhkhoa.goldenexperience.models.Meal

class MealSelectedAdapter(
    private val meals: MutableList<Meal>,
//    private val onRemoveClick: (Meal) -> Unit
) : RecyclerView.Adapter<MealSelectedAdapter.SearchViewHolder>() {

    inner class SearchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mealNameTextView: TextView = itemView.findViewById(R.id.tvMealName)
        //val addButton: Button = itemView.findViewById(R.id.btnAddMeal)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_meal_selected, parent, false)
        return SearchViewHolder(view)
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        val meal = meals[position]
        holder.mealNameTextView.text = meal.name
//        DetailBtn.setOnClickListener {
//            onRemoveClick(meal)
//        }
    }
    fun addMeal(meal: Meal) {
        meals.add(meal)
        notifyItemInserted(meals.size - 1)
    }
    override fun getItemCount(): Int = meals.size
}
