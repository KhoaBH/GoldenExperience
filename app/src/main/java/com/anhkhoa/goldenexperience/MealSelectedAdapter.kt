package com.anhkhoa.goldenexperience.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.RecyclerView
import com.anhkhoa.goldenexperience.R
import com.anhkhoa.goldenexperience.models.Meal
import com.anhkhoa.goldenexperience.models.MealInPlan

class MealSelectedAdapter(
    private val mealInPlans: MutableList<MealInPlan>,
    private val onRemoveClick: (Meal) -> Unit,
    private val onQuantityChanged: () -> Unit
) : RecyclerView.Adapter<MealSelectedAdapter.SearchViewHolder>() {

    inner class SearchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mealNameTextView: TextView = itemView.findViewById(R.id.tvMealName)
        val protein: TextView = itemView.findViewById(R.id.protein)
        val fat: TextView = itemView.findViewById(R.id.fat)
        val carb: TextView = itemView.findViewById(R.id.carb)
        val kcal: TextView = itemView.findViewById(R.id.kcal)
        val Quantity: EditText =  itemView.findViewById(R.id.etQuantity)
        val Unit: TextView =  itemView.findViewById(R.id.Unit)
        val removeButton: Button = itemView.findViewById(R.id.DeleteBtn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_meal_selected, parent, false)
        return SearchViewHolder(view)
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        val mealInPlan = mealInPlans[position]
        val meal = mealInPlan.meal

        holder.mealNameTextView.text = meal.name

        holder.Quantity.setText(mealInPlan.quantity.toInt().toString())
        holder.Unit.text = mealInPlan.unit

        holder.protein.text = (meal.protein ).toInt().toString()
        holder.fat.text = (meal.fat ).toInt().toString()
        holder.carb.text = (meal.carbs ).toInt().toString()
        holder.kcal.text = (meal.calories).toInt().toString()
        holder.removeButton.setOnClickListener {
            onRemoveClick(meal)
        }
        holder.Quantity.doOnTextChanged { text, _, _, _ ->
            val qty = text.toString().toFloatOrNull() ?: 1f
            mealInPlans[position] = mealInPlan.copy(quantity = qty)
            onQuantityChanged()
        }
    }
    fun addMeal(meal: Meal) {
        val (defaultQty, defaultUnit) = when (meal.unit.lowercase()) {
            "1 serving" -> 1f to "Serving"
            "100g", "g" -> 100f to "Gram"
            else -> 100f to "Gram" // fallback
        }
        val newMealInPlan = MealInPlan(meal = meal, quantity = defaultQty, unit = defaultUnit)
        mealInPlans.add(newMealInPlan)
        notifyItemInserted(mealInPlans.size - 1)
    }



    fun setMeals(newMeals: List<MealInPlan>) {
        mealInPlans.clear()
        mealInPlans.addAll(newMeals)
        notifyDataSetChanged()
    }

    fun removeMeal(meal: Meal) {
        val index = mealInPlans.indexOfFirst { it.meal == meal }
        Log.d("DEBUG_MEAL_Adapter", "selectedMealInPlans: ${meal}")
        if (index != -1) {
            mealInPlans.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    fun getCurrentMeals(): List<MealInPlan> = mealInPlans
    override fun getItemCount(): Int = mealInPlans.size
}
