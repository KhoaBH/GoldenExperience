package com.anhkhoa.goldenexperience

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
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

       // allMeals = readMealsFromAssets(requireContext()) // load từ file JSON
      //  uploadMealsToFirestore(allMeals)

        adapter = MealAdapter(filteredMeals)
        binding.rvMeals.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMeals.adapter = adapter

        loadMealsFromFirestore()


        adapter = MealAdapter(filteredMeals)
        binding.rvMeals.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMeals.adapter = adapter

        val searchEditText = binding.foodSearchBar.searchEditText
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim().lowercase()
                filteredMeals.clear()
                if (query.isEmpty()) {
                    // Không có từ tìm, ẩn RecyclerView đi
                    binding.rvMeals.visibility = View.GONE
                } else {
                    // Có từ tìm, lọc và hiện RecyclerView
                    filteredMeals.addAll(allMeals.filter { it.name.lowercase().startsWith(query) })
                    binding.rvMeals.visibility = View.VISIBLE
                }
                adapter.notifyDataSetChanged()
            }
        })

    }

    // Hàm đọc file JSON trong assets
    fun readMealsFromAssets(context: Context, fileName: String = "mealprep_gym_100.json"): List<Meal> {
        val jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
        val gson = Gson()
        val mealListType = object : TypeToken<List<Meal>>() {}.type
        return gson.fromJson(jsonString, mealListType)
    }

    // Hàm upload món ăn lên Firestore
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

}
