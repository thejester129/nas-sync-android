package com.example.android_nas_sync.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.android_nas_sync.R
import com.example.android_nas_sync.databinding.AddMappingDialogBinding
import com.example.android_nas_sync.databinding.FragmentHomeBinding
import com.example.android_nas_sync.viewmodels.MappingsViewModel

class AddMappingDialog : Fragment() {
    private var _binding: AddMappingDialogBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MappingsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = AddMappingDialogBinding.inflate(inflater, container, false)
        val view = binding.root
        initBindings(view)
        return binding.root
    }

    private fun initBindings(view: View){
        val saveButton = view.findViewById<View>(R.id.add_dialog_save_button)
        val dismissButton = view.findViewById<View>(R.id.add_dialog_dismiss_button)
        dismissButton.setOnClickListener {
            run {
                findNavController().navigate(R.id.HomeFragment)
            }
        }
    }
}