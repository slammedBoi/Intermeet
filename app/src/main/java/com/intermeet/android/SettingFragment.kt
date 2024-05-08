package com.intermeet.android

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.provider.Settings
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.appcompat.widget.Toolbar

class SettingsFragment : Fragment() {
    companion object {
        private const val TAG = "SettingFragment"
        fun newInstance(): SettingsFragment {
            return SettingsFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_setting, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar: Toolbar = view.findViewById(R.id.toolbar)
        val emailButton: Button = view.findViewById(R.id.Email)
        val privacyPolicyButton: Button = view.findViewById(R.id.privacyPolicyButton)
        val notificationButton: Button = view.findViewById(R.id.PushNotifications)
        val tosButton: Button = view.findViewById(R.id.TOS)


        // Handle back press explicitly
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Custom back navigation, navigating back to the profile fragment
                findNavController().navigate(R.id.action_settingsFragment_to_profileFragment)
            }
        })

        // Assume navController is set up with the toolbar for navigation
        toolbar.setNavigationOnClickListener {
            val navController = findNavController()

            Log.d("Navigation", "Current Fragment: ${navController.currentDestination?.label}")
            findNavController().navigateUp()
        }

        // Navigate using NavController
        emailButton.setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_profileEmailFragment)
        }
        tosButton.setOnClickListener {
            val intent = Intent(
                Intent.ACTION_VIEW, Uri.parse(
                    "https://intermeetiate.github.io/TermsOfService/"
                )
            )
            startActivity(intent)
        }
        //OPEN PRIVACY POLICY LINK
        privacyPolicyButton.setOnClickListener{
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(
                "https://intermeetiate.github.io/InterMeetiatePrivatePolicy"))
            startActivity(intent)
        }
        notificationButton.setOnClickListener {
            openNotificationSettingsForApp()
        }

    }
    override fun onResume() {
        super.onResume()
        Log.d("NavigationStatus", "${this::class.java.simpleName} is now visible")
    }
    private fun openNotificationSettingsForApp() {
        val intent = Intent().apply {
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                    action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                    putExtra(Settings.EXTRA_APP_PACKAGE, requireContext().packageName)
                }
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> {
                    action = "android.settings.APP_NOTIFICATION_SETTINGS"
                    putExtra("app_package", requireContext().packageName)
                    putExtra("app_uid", requireContext().applicationInfo.uid)
                }
                else -> {
                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    data = Uri.parse("package:${requireContext().packageName}")
                }
            }
        }
        startActivity(intent)
    }
}
