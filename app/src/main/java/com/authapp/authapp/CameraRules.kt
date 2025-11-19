package com.authapp.authapp

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.authapp.authapp.databinding.FragmentCameraRulesBinding
import com.qoreid.sdk.core.QoreIDParams
import com.qoreid.sdk.core.QoreIDSdk
import com.qoreid.sdk.core.QoreIDSdk.QORE_ID_RESULT_CODE
import com.qoreid.sdk.core.QoreIDSdk.QORE_ID_RESULT_EXTRA_KEY
import com.qoreid.sdk.core.models.ErrorResult
import com.qoreid.sdk.core.models.QoreIDResult
import com.qoreid.sdk.core.models.SuccessResult


class CameraRules : Fragment() {
    private var _binding: FragmentCameraRulesBinding? = null
    private val binding get() = _binding!!
        private val TAG = "CameraRules"

      private lateinit var activityResultLauncher: ActivityResultLauncher<android.content.Intent>

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (!granted) {
                Toast.makeText(requireContext(), "Camera permission denied", Toast.LENGTH_SHORT)
                    .show()
            } else {
                Log.d(TAG, "Camera permission granted")
            }
        }
           override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                handleQoreIdResult(result)
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentCameraRulesBinding.inflate(inflater, container, false)

        QoreIDSdk.initialize(requireActivity())


        return binding.root
    }

   
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPrefs = requireContext().getSharedPreferences("info", Context.MODE_PRIVATE)
        val firstName = sharedPrefs.getString("firstName", "") ?: ""
        val lastName = sharedPrefs.getString("lastName", "") ?: ""
        val phone = sharedPrefs.getString("phoneNumber", "") ?: ""
        val clientId = "XZKHY139CQBD2QQ083PG"
        val customerReference = "eaf93b5e-9571-41e0-82fe-7247b6f78560"
        val productCode = "liveness"

        val applicantData = ApplicantData(firstName, lastName, phone)
        val inputData = InputData(applicantData)



        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.qoreIDButton.setOnClickListener {
            checkCameraPermission()


            try {
                val qoreIDParams = QoreIDParams()
                    .clientId(clientId)
                    .customerReference(customerReference)
                    // .inputData(inputData)
                    .collection("liveness")


                QoreIDSdk.params(qoreIDParams).launch(requireNotNull(requireActivity()))
                Log.d(TAG, "Launching QoreID verification with params: $qoreIDParams")


            } catch (e: Exception) {
                Log.d(TAG, "QoreID launch failed", e)
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun checkCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> Log.d(TAG, "Camera permission granted")

            else -> showPermissionDialog()
        }
    }

    private fun showPermissionDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Camera Permission Needed")
            .setMessage("Please allow camera access to continue verification.")
            .setCancelable(false)
            .setPositiveButton("Grant") { _, _ ->
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun handleQoreIdResult(result: androidx.activity.result.ActivityResult) {
        val data = result.data ?: return
        val qoreIdResult =
            data.getSerializableExtra(com.qoreid.sdk.core.QoreIDSdk.QORE_ID_RESULT_EXTRA_KEY) as? QoreIDResult
                ?: return

        when (qoreIdResult) {
            is SuccessResult -> {
                Log.i(TAG, "Verification success: ${qoreIdResult.data}")
                Toast.makeText(requireContext(), "Verification successful!", Toast.LENGTH_LONG)
                    .show()
            }

            is ErrorResult -> {
                Log.e(TAG, "Verification failed: ${qoreIdResult.message}")
                Toast.makeText(
                    requireContext(),
                    "Verification failed: ${qoreIdResult.message}",
                    Toast.LENGTH_LONG
                ).show()
            }

            else -> Log.w(
                TAG,
                "Unhandled QoreID result type: ${qoreIdResult::class.java.simpleName}"
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
