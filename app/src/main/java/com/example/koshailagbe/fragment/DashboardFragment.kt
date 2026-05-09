package com.example.koshailagbe.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.koshailagbe.R


class DashboardFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_dashboard, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<View>(R.id.btnMeatShop).setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_meatShop)
        }

        view.findViewById<View>(R.id.btnBookButcher).setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_butcherBooking)
        }

        view.findViewById<View>(R.id.btnUrgentButcher).setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_butcherBooking)
        }

        view.findViewById<View>(R.id.btnToolsGear).setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_toolsAndGear)
        }

        view.findViewById<View>(R.id.btnCleaningService).setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_cleaningService)
        }

        view.findViewById<View>(R.id.btnFullService).setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_fullServicePackage)
        }

        view.findViewById<View>(R.id.btnLiveStatus).setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_liveStatusTracking)
        }

        setupBottomNav(view)
    }

    private fun setupBottomNav(view: View) {
        view.findViewById<View>(R.id.navHome).setOnClickListener {
            // Already here
        }
        view.findViewById<View>(R.id.navBookings).setOnClickListener {
            findNavController().navigate(R.id.liveStatusTrackingFragment)
        }
        view.findViewById<View>(R.id.navStore).setOnClickListener {
            findNavController().navigate(R.id.meatShopFragment)
        }
        view.findViewById<View>(R.id.navCharity).setOnClickListener {
            findNavController().navigate(R.id.charityDistributionFragment)
        }
        view.findViewById<View>(R.id.navProfile).setOnClickListener {
            findNavController().navigate(R.id.profileFragment)
        }
    }

    private fun toast(msg: String) =
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
}