package com.anhkhoa.goldenexperience

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.anhkhoa.goldenexperience.databinding.ItemMealBinding
import com.anhkhoa.goldenexperience.models.Meal
import com.bumptech.glide.Glide

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
            loadMealImage(meal.imageUrl, imgMeal)
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
    fun loadMealImage(imageUrl: String, imageView: ImageView) {
        if (imageUrl.isBlank()) {
            // Load ảnh mặc định
            imageView.setImageResource(R.drawable.meal)
        } else {
            // Load ảnh từ URL bằng Glide
            Glide.with(imageView.context)
                .load(imageUrl)
                .placeholder(R.drawable.meal) // ảnh hiện tạm khi load
                .error(R.drawable.meal) // nếu load lỗi cũng hiện ảnh mặc định
                .into(imageView)
        }
    }

    override fun getItemCount(): Int = meals.size
}

