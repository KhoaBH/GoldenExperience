package com.anhkhoa.goldenexperience

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import com.anhkhoa.goldenexperience.models.Meal
class AddMealFragment : Fragment() {

    private lateinit var edtName: TextInputEditText
    private lateinit var edtCalories: TextInputEditText
    private lateinit var edtCarbs: TextInputEditText
    private lateinit var edtProtein: TextInputEditText
    private lateinit var edtFat: TextInputEditText
    private lateinit var edtDescription: TextInputEditText
    private lateinit var edtImageUrl: TextInputEditText

    private lateinit var spnUnit: Spinner
    private lateinit var spnType: Spinner
    private lateinit var btnSaveMeal: MaterialButton

    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_meal, container, false)

        edtName = view.findViewById(R.id.edtName)
        edtCalories = view.findViewById(R.id.edtCalories)
        edtCarbs = view.findViewById(R.id.edtCarbs)
        edtProtein = view.findViewById(R.id.edtProtein)
        edtFat = view.findViewById(R.id.edtFat)
        edtDescription = view.findViewById(R.id.edtDescription)
        edtImageUrl = view.findViewById(R.id.edtImageUrl)

        spnUnit = view.findViewById(R.id.spnUnit)
        spnType = view.findViewById(R.id.spnType)

        btnSaveMeal = view.findViewById(R.id.btnSaveMeal)
        btnSaveMeal.setOnClickListener {
            saveMeal()
        }

        return view
    }

    private fun saveMeal() {
        val name = edtName.text.toString().trim()
        val calories = edtCalories.text.toString().trim()
        val carbs = edtCarbs.text.toString().trim()
        val protein = edtProtein.text.toString().trim()
        val fat = edtFat.text.toString().trim()
        val description = edtDescription.text.toString().trim()
        val imageUrl = edtImageUrl.text.toString().trim()
        val unit = spnUnit.selectedItem?.toString() ?: ""
        val type = spnType.selectedItem?.toString() ?: ""

        if (name.isEmpty() || calories.isEmpty() || carbs.isEmpty() || protein.isEmpty() || fat.isEmpty() || imageUrl.isEmpty()) {
            Toast.makeText(requireContext(), "Điền đầy đủ thông tin nha!", Toast.LENGTH_SHORT).show()
            return
        }

        val meal = Meal(
            id = "", // để trống cho Firestore tự tạo ID
            name = name,
            calories = calories.toIntOrNull() ?: 0,
            carbs = carbs.toFloatOrNull() ?: 0f,
            protein = protein.toFloatOrNull() ?: 0f,
            fat = fat.toFloatOrNull() ?: 0f,
            description = description,
            imageUrl = imageUrl,
            unit = unit,
            type = type
        )

        db.collection("meals")
            .add(meal)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Lưu meal thành công!", Toast.LENGTH_SHORT).show()
                clearInputs()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Lưu meal thất bại: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e("AddMealFragment", "Lưu meal lỗi", e)
            }
    }
    private fun clearInputs() {
        edtName.setText("")
        edtCalories.setText("")
        edtCarbs.setText("")
        edtProtein.setText("")
        edtFat.setText("")
        edtDescription.setText("")
        edtImageUrl.setText("")
        spnUnit.setSelection(0)
        spnType.setSelection(0)
    }
}
