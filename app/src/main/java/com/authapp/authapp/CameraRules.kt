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
    private val CAMERA_REQUEST_CODE = 101


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
        checkCameraPermissionAndStart()
    }

    /** Check permission before launching camera */
    private fun checkCameraPermissionAndStart() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            startLivenessCheck()
        } else {
            showPermissionDialog()
        }
    }

    private fun showPermissionDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Camera Permission Needed")
            .setMessage("We need your camera to perform a quick liveness check.")
            .setCancelable(false)
            .setPositiveButton("Grant") { _, _ ->
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.CAMERA),
                    CAMERA_REQUEST_CODE
                )
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                Toast.makeText(requireContext(), "Camera permission denied", Toast.LENGTH_SHORT)
                    .show()
            }
            .show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_REQUEST_CODE &&
            grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            startLivenessCheck()
        } else {
            Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    /** Launch QoreID liveness */
    private fun startLivenessCheck() {
        val sharedPreferences = requireContext().getSharedPreferences("info", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getString("uu_id", "") ?: ""
        val userEmail = sharedPreferences.getString("emailAddress", "") ?: ""
        val firstName = sharedPreferences.getString("firstName", "") ?: ""
        val lastName = sharedPreferences.getString("lastName", "") ?: ""

        val inputData = mapOf(
            "first_name" to firstName,
            "last_name" to lastName,
            "email" to userEmail
        )

        // ✅ Build parameters using v2.x chainable API
        val qoreIDParams = QoreIDParams()
            .collection("liveness")
            .clientId("XZKHY139CQBD2QQ083PG")


        // Register + Launch SDK flow
        QoreIDSdk.params(qoreIDParams)
            .registerForResult(activityResultLauncher)
            .launch(requireActivity())
    }

    /** Handle results */
    private val activityResultLauncher: ActivityResultLauncher<android.content.Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == QORE_ID_RESULT_CODE && result.data != null) {
                val qoreIdResult =
                    result.data!!.getSerializableExtra(QORE_ID_RESULT_EXTRA_KEY) as QoreIDResult

                when (qoreIdResult) {
                    is SuccessResult -> {
                        Toast.makeText(requireContext(), "Liveness successful!", Toast.LENGTH_LONG)
                            .show()
                        findNavController().popBackStack()
                    }

                    is ErrorResult -> {
                        Toast.makeText(
                            requireContext(),
                            "Liveness failed: ${qoreIdResult.message}",
                            Toast.LENGTH_LONG
                        ).show()
                        findNavController().popBackStack()
                    }
                }
            }
        }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}