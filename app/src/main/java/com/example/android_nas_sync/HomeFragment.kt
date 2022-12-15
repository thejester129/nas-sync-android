package com.example.android_nas_sync

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.android_nas_sync.R
import com.example.android_nas_sync.databinding.FragmentHomeBinding
import com.example.android_nas_sync.models.Mapping
import com.example.android_nas_sync.models.ShareType
import com.example.android_nas_sync.viewmodels.MappingsViewModel
import com.google.android.material.snackbar.Snackbar


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MappingsViewModel by activityViewModels()
    private var mappingAdapter:MappingRecyclerAdapter = MappingRecyclerAdapter(listOf()) { mapping ->
        onRecyclerItemClick(
            mapping
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view = binding.root

        initRecycler(view)
        initAddButton(view)
        initObservers()

        return binding.root
    }

    private fun initRecycler(view:View){
        val mappingRecycler = view.findViewById<View>(R.id.mapping_recycler) as RecyclerView
        mappingRecycler.layoutManager = LinearLayoutManager(view.context);
        mappingRecycler.adapter = mappingAdapter
    }

    private fun initAddButton(view: View) {
        val addButton = view.findViewById<View>(R.id.mapping_add_button)
        addButton.setOnClickListener { v ->
            val navController = findNavController()
            navController.navigate(R.id.AddMappingDialog)
        }
    }

    private fun initObservers(){
        viewModel.mappings.observe(viewLifecycleOwner) { mappings ->
            mappingAdapter.updateMappings(mappings)
        }
    }

    private fun onRecyclerItemClick(mapping: Mapping):Unit{
        viewModel.currentlyEditedMapping = mapping
        val navController = findNavController()
        navController.navigate(R.id.EditMappingDialog)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}