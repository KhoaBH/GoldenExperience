package com.anhkhoa.goldenexperience

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.anhkhoa.goldenexperience.databinding.FragmentMealPrepBinding

class MealPrepFragment : Fragment() {

    private var _binding: FragmentMealPrepBinding? = null
    private val binding get() = _binding!!

    private val targetCalories = 2200

    // Danh sách món ăn đã chọn, có thể thay đổi sau này
    private val selectedMeals = mutableListOf(
        Meal("Cơm gà", 600),
        Meal("Salad", 200),
        Meal("Trái cây", 250),
        Meal("Sữa chua", 150),
        Meal("Trứng luộc", 300)
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMealPrepBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateCaloriesUI()
    }

    // Hàm tính tổng calo hiện tại
    private fun getCurrentCalories(): Int = selectedMeals.sumOf { it.calories }

    // Cập nhật progress bar và text calo
    private fun updateCaloriesUI() {
        val currentCalories = getCurrentCalories()
        val percent = ((currentCalories * 100f) / targetCalories).toInt().coerceIn(0, 100)

        binding.apply {
            circleProgress.setProgressCompat(percent, true)
            txtPercent.text = "$percent%"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    data class Meal(val name: String, val calories: Int)
}
