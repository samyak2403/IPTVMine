package com.samyak2403.iptvmine.screens

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

import com.samyak2403.iptvmine.R
import com.samyak2403.iptvmine.databinding.FragmentAboutBinding
import com.samyak2403.iptvmine.utils.ThemeManager

import kotlin.jvm.java

class AboutFragment : Fragment() {

    private var _binding: FragmentAboutBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAboutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews()
        setupClickListeners()
    }

    private fun setupViews() {
        // Display app version
        binding.tvAppVersion.text = getAppVersion()

        // Set dark mode switch state
        binding.switchDarkMode.setChecked(ThemeManager.isDarkModeEnabled(requireContext()))
    }

    private fun setupClickListeners() {
        // Dark mode switch - automatically updates colors by recreating activity
        binding.switchDarkMode.setOnCheckChangeListener(object : com.samyak.custom_switch.MaterialCustomSwitch.OnCheckChangeListener {
            override fun onCheckChanged(isChecked: Boolean) {
                // Save theme preference
                ThemeManager.setDarkMode(requireContext(), isChecked)
                
                // Show feedback message
                val message = if (isChecked) "Dark mode enabled" else "Light mode enabled"
                android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_SHORT).show()
                
                // Recreate activity to apply theme changes immediately
                // This automatically updates all colors based on the new theme
                requireActivity().recreate()
            }
        })

        // About App Card
        binding.cardAboutApp.setOnClickListener {
            val intent = Intent(requireContext(), AppInfoActivity::class.java)
            startActivity(intent)
        }
    }



    private fun getAppVersion(): String {
        return try {
            val packageInfo =
                requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
            "Version: ${packageInfo.versionName} (${packageInfo.versionCode})"
        } catch (e: PackageManager.NameNotFoundException) {
            "Version info not available"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
