package com.authapp.authapp

import android.content.Context
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.authapp.authapp.databinding.FragmentFormDetailsBinding

class FormDetails : Fragment() {

    private var _binding: FragmentFormDetailsBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFormDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.btnFinish.setOnClickListener {
            validateAndSubmit()
        }

        binding.backButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }


    private fun validateAndSubmit() {

        val firstName = binding.firstNameEt.text.toString().trim()
        val lastName = binding.surnameEt.text.toString().trim()
        val email = binding.emailEt.text.toString().trim()
        val phone = binding.numberEt.text.toString().trim()
        val password = binding.passwordEt.text.toString().trim()
        val confirmPassword = binding.passwordEtType.text.toString().trim()

        // RESET ERRORS
        binding.firstName.error = null
        binding.surname.error = null
        binding.email.error = null
        binding.number.error = null
        binding.password.error = null
        binding.passwordRetype.error = null

        // VALIDATIONS
        if (firstName.isEmpty()) {
            binding.firstName.error = "Enter first name"
            return
        }

        if (lastName.isEmpty()) {
            binding.surname.error = "Enter surname"
            return
        }

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.email.error = "Enter a valid email"
            return
        }

        if (phone.isEmpty() || phone.length < 10) {
            binding.number.error = "Enter valid phone number"
            return
        }

        if (password.length < 6) {
            binding.password.error = "Password must be at least 6 characters"
            return
        }

        if (confirmPassword != password) {
            binding.passwordRetype.error = "Passwords do not match"
            return
        }

        if (!binding.checkTerms.isChecked) {
            Toast.makeText(requireContext(), "You must agree to Terms & Conditions", Toast.LENGTH_SHORT).show()
            return
        }

        // SHOW LOADING
        binding.loadingOverlay.visibility = View.VISIBLE

        // Simulate saving + redirect delay
        binding.loadingOverlay.postDelayed({

            saveUserPreference(firstName, lastName, email, phone)

            binding.loadingOverlay.visibility = View.GONE

            Toast.makeText(requireContext(), "Registration Successful!", Toast.LENGTH_LONG).show()

            // REDIRECT → Change this to your correct destination
            findNavController().navigate(R.id.action_formDetails_to_cameraRules)

        }, 1500)
    }


    private fun saveUserPreference(
        firstName: String,
        lastName: String,
        email: String,
        phone: String
    ) {
        val pref = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

        pref.edit()
            .putString("first_name", firstName)
            .putString("last_name", lastName)
            .putString("email", email)
            .putString("phone", phone)
            .apply()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
