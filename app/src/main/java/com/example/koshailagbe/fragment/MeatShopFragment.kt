package com.example.koshailagbe.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.koshailagbe.R

class MeatShopFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_meat_shop, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<TextView>(R.id.btnBack).setOnClickListener {
            findNavController().popBackStack()
        }

        view.findViewById<TextView>(R.id.btnCart).setOnClickListener {
            toast("Cart — Coming soon")
        }

        // Category tabs
        view.findViewById<TextView>(R.id.tabCategorie).setOnClickListener {
            toast("Category")
        }
        view.findViewById<TextView>(R.id.tabMutton).setOnClickListener {
            toast("Mutton")
        }
        view.findViewById<TextView>(R.id.tabRibs).setOnClickListener {
            toast("Ribs")
        }
        view.findViewById<TextView>(R.id.tabKeema).setOnClickListener {
            toast("Keema")
        }

        // Add to Cart buttons
        view.findViewById<Button>(R.id.btnAddBeef).setOnClickListener {
            toast("Beef added to cart — ৳390")
        }
        view.findViewById<Button>(R.id.btnAddMutton).setOnClickListener {
            toast("Mutton added to cart — ৳370")
        }
        view.findViewById<Button>(R.id.btnAddRibs).setOnClickListener {
            toast("Ribs added to cart — ৳420")
        }
        view.findViewById<Button>(R.id.btnAddKeema).setOnClickListener {
            toast("Keema added to cart — ৳340")
        }

        // Bottom ADD TO CART
        view.findViewById<Button>(R.id.btnAddToCart).setOnClickListener {
            toast("Items added to cart ✓")
        }

        // View All
        view.findViewById<TextView>(R.id.tvViewAll).setOnClickListener {
            toast("All products — Coming soon")
        }
    }

    private fun toast(msg: String) =
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
}