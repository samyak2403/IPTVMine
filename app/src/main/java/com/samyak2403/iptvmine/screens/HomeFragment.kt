///*
// * Created by Samyak kamble on 8/14/24, 11:33 AM
// *  Copyright (c) 2024 . All rights reserved.
// *  Last modified 8/14/24, 11:33 AM
// */

package com.samyak2403.iptvmine.screens

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.samyak2403.iptvmine.R
import com.samyak2403.iptvmine.adapter.ChannelsAdapter
import com.samyak2403.iptvmine.model.Channel
import com.samyak2403.iptvmine.provider.ChannelsProvider

class HomeFragment : Fragment() {

    private lateinit var channelsProvider: ChannelsProvider
    private lateinit var searchEditText: EditText
    private lateinit var searchIcon: ImageView
    private lateinit var progressBar: ProgressBar
    private lateinit var recyclerView: RecyclerView
    private lateinit var chipGroup: ChipGroup
    private lateinit var adapter: ChannelsAdapter

    private var debounceHandler: Handler? = null
    private var isSearchVisible: Boolean = false
    private var allChannels: List<Channel> = emptyList()
    private var selectedCategory: String? = "All"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        channelsProvider = ViewModelProvider(this)[ChannelsProvider::class.java]
        searchEditText = view.findViewById(R.id.searchEditText)
        searchIcon = view.findViewById(R.id.search_icon)
        progressBar = view.findViewById(R.id.progressBar)
        recyclerView = view.findViewById(R.id.recyclerView)
        chipGroup = view.findViewById(R.id.categoryChipGroup)

        adapter = ChannelsAdapter { channel: Channel ->
            PlayerActivity.start(requireContext(), channel)
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        setupObservers()
        fetchData()

        searchIcon.setOnClickListener {
            toggleSearchBar()
        }

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                debounceHandler?.removeCallbacksAndMessages(null)
                debounceHandler = Handler(Looper.getMainLooper())
                debounceHandler?.postDelayed({
                    filterChannels(s.toString())
                }, 300)
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        return view
    }

    private fun setupObservers() {
        channelsProvider.channels.observe(viewLifecycleOwner) { data ->
            allChannels = data
            progressBar.visibility = View.GONE
            if (searchEditText.text.toString().isEmpty() && selectedCategory == "All") {
                adapter.updateChannels(data)
            }
        }

        channelsProvider.filteredChannels.observe(viewLifecycleOwner) { data ->
            adapter.updateChannels(data)
        }

        channelsProvider.categories.observe(viewLifecycleOwner) { categories ->
            updateCategoryChips(categories)
        }

        channelsProvider.isLoading.observe(viewLifecycleOwner) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        channelsProvider.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchData() {
        progressBar.visibility = View.VISIBLE
        channelsProvider.fetchM3UFile()
    }

    private fun filterChannels(query: String) {
        if (query.isEmpty() && (selectedCategory.isNullOrEmpty() || selectedCategory == "All")) {
            adapter.updateChannels(allChannels)
        } else {
            channelsProvider.filterChannelsByQueryAndCategory(query, selectedCategory)
        }
    }

    private fun updateCategoryChips(categories: List<String>) {
        chipGroup.removeAllViews()
        
        categories.forEach { category ->
            val chip = Chip(requireContext()).apply {
                text = category
                isCheckable = true
                isChecked = category == selectedCategory
                
                setOnClickListener {
                    selectedCategory = category
                    val query = searchEditText.text.toString()
                    channelsProvider.filterChannelsByQueryAndCategory(query, category)
                }
            }
            chipGroup.addView(chip)
        }
    }

    private fun toggleSearchBar() {
        if (isSearchVisible) {
            searchEditText.visibility = View.GONE
            searchEditText.text.clear()
            isSearchVisible = false
            channelsProvider.filterChannelsByCategory(selectedCategory)
        } else {
            searchEditText.visibility = View.VISIBLE
            searchEditText.requestFocus()
            isSearchVisible = true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        debounceHandler?.removeCallbacksAndMessages(null)
    }
}