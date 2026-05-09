package com.example.koshailagbe.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.koshailagbe.R

class LiveStatusTrackingFragment : Fragment() {

    private val handler = Handler(Looper.getMainLooper())
    private var etaMinutes = 20

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_live_status_tracking, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<TextView>(R.id.btnBack).setOnClickListener {
            findNavController().popBackStack()
        }

        setupTabs(view)
        setupCardClicks(view)
        setupContactButton(view)
        setupBottomNav(view)
        startEtaCountdown()
    }

    private fun setupTabs(view: View) {
        val tabAll    = view.findViewById<TextView>(R.id.tabAllBookings)
        val tabActive = view.findViewById<TextView>(R.id.tabActiveBookings)

        tabAll.setOnClickListener {
            tabAll.setBackgroundColor(requireContext().getColor(R.color.primary_green))
            tabAll.setTextColor(requireContext().getColor(android.R.color.white))
            tabActive.setBackgroundColor(requireContext().getColor(android.R.color.white))
            tabActive.setTextColor(requireContext().getColor(R.color.text_gray))
        }
        tabActive.setOnClickListener {
            tabActive.setBackgroundColor(requireContext().getColor(R.color.primary_green))
            tabActive.setTextColor(requireContext().getColor(android.R.color.white))
            tabAll.setBackgroundColor(requireContext().getColor(android.R.color.white))
            tabAll.setTextColor(requireContext().getColor(R.color.text_gray))
        }
    }

    private fun setupCardClicks(view: View) {
        view.findViewById<CardView>(R.id.cardButcherStatus).setOnClickListener {
            toast("Butcher is on the way — ETA $etaMinutes min")
        }
        view.findViewById<CardView>(R.id.cardToolsMeatStatus).setOnClickListener {
            toast("Tools & Meat delivery — ETA 10 min")
        }
    }

    private fun startEtaCountdown() {
        val tvEtaMain = view?.findViewById<TextView>(R.id.tvButcherEtaMain)
        val tvEtaSub  = view?.findViewById<TextView>(R.id.tvButcherEtaSubtitle)

        handler.postDelayed(object : Runnable {
            override fun run() {
                if (!isAdded) return
                if (etaMinutes > 1) {
                    etaMinutes--
                    tvEtaMain?.text = getString(R.string.value_minutes_format, etaMinutes)
                    tvEtaSub?.text = getString(R.string.label_arriving_format, etaMinutes)
                    handler.postDelayed(this, 60_000)
                } else {
                    tvEtaMain?.text = getString(R.string.value_minutes_format, 0)
                    tvEtaSub?.text = getString(R.string.butcher_arrived_status)
                    toast(getString(R.string.butcher_arrived_msg))
                }
            }
        }, 60_000)
    }

    private fun setupContactButton(view: View) {
        view.findViewById<Button>(R.id.btnContactButcher).setOnClickListener {
            toast("Calling butcher...")
        }
    }

    private fun setupBottomNav(view: View) {
        view.findViewById<LinearLayout>(R.id.navHome).setOnClickListener {
            findNavController().navigate(R.id.dashboardFragment)
        }
        view.findViewById<LinearLayout>(R.id.navBookings).setOnClickListener {
            // Already here
        }
        view.findViewById<LinearLayout>(R.id.navStore).setOnClickListener {
            findNavController().navigate(R.id.meatShopFragment)
        }
        view.findViewById<LinearLayout>(R.id.navCharity).setOnClickListener {
            findNavController().navigate(R.id.charityDistributionFragment)
        }
        view.findViewById<LinearLayout>(R.id.navProfile).setOnClickListener {
            findNavController().navigate(R.id.profileFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacksAndMessages(null)
    }

    private fun toast(msg: String) =
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
}