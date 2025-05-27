package com.anhkhoa.goldenexperience

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.anhkhoa.goldenexperience.databinding.FragmentMealPrepBinding
import androidx.core.widget.addTextChangedListener
import com.anhkhoa.goldenexperience.databinding.ViewSearchBarBinding
import com.google.common.reflect.TypeToken
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.anhkhoa.goldenexperience.models.Meal

class MealPrepFragment : Fragment() {

    private var _binding: FragmentMealPrepBinding? = null
    private val binding get() = _binding!!

    private var allMeals = listOf(
        Meal("Phở bò", 500),
        Meal("Cơm gà", 600),
        Meal("Salad", 200),
        Meal("Trái cây", 250),
        Meal("Sữa chua", 150),
        Meal("Trứng luộc", 300),
        Meal("Phở gà", 480),
        Meal("Bún chả", 520),
    )

    private val filteredMeals = mutableListOf<Meal>()
    private lateinit var adapter: MealAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMealPrepBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        allMeals = readMealsFromAssets(requireContext()) // load từ file JSON
//        uploadMealsToFirestore(allMeals)
        val breakfastIcon = view.findViewById<ImageView>(R.id.breakfastIcon)
        breakfastIcon.setOnClickListener {
            val intent = Intent(requireContext(), MealPlanActivity::class.java)
            intent.putExtra("meal_type", "breakfast") // Gửi kèm dữ liệu nếu cần
            startActivity(intent)
        }
        // Khởi tạo adapter với mode hiện nút Add
        adapter = MealAdapter(filteredMeals,
            onAddClick = { meal ->
                // xử lý nút add
                Toast.makeText(context, "Add món: ${meal.name}", Toast.LENGTH_SHORT).show()
            },
            onMealClick = { meal ->
                // xử lý nút detail
                val intent = Intent(context, MealDetail::class.java)
                intent.putExtra("meal_data", meal)
                context?.startActivity(intent)
            }
        )


        binding.rvMeals.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMeals.adapter = adapter

        loadMealsFromFirestore()

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
        val db = FirebaseFirestore.getInstance()
        db.collection("meals")
            .get()
            .addOnSuccessListener { result ->
                val mealsFromFirebase = mutableListOf<Meal>()
                for (document in result) {
                    val meal = document.toObject(Meal::class.java)
                    mealsFromFirebase.add(meal)
                }
                allMeals = mealsFromFirebase
                filteredMeals.clear()
                filteredMeals.addAll(allMeals)
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.e("FirebaseLoad", "Error getting meals", exception)
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    fun uploadMealsToFirestore(meals: List<Meal>) {
        val db = FirebaseFirestore.getInstance()
        for (meal in meals) {
            db.collection("meals")
                .add(meal)
                .addOnSuccessListener {
                    Log.d("DEBUG_MEAL", meal.toString())
                    Log.d("Upload", "Added meal: ${meal.name}")
                }
                .addOnFailureListener { e ->
                    Log.e("Upload", "Error adding meal ${meal.name}", e)
                }
        }
    }
}
