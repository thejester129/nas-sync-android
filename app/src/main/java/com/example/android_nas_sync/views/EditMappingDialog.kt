package com.example.android_nas_sync.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.android_nas_sync.R
import com.example.android_nas_sync.databinding.FragmentHomeBinding
import com.example.android_nas_sync.databinding.MappingDialogBinding
import com.example.android_nas_sync.models.Mapping
import com.example.android_nas_sync.viewmodels.MappingsViewModel

class EditMappingDialog : Fragment() {
    private var _binding: MappingDialogBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MappingsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = MappingDialogBinding.inflate(inflater, container, false)
        val view = binding.root
        initBindings(view)
        return binding.root
    }

    private fun initBindings(view: View){
        val mapping = viewModel.currentlyEditedMapping!!
        val ipAddress = view.findViewById<EditText>(R.id.add_dialog_ip)
        val share = view.findViewById<EditText>(R.id.add_dialog_share)
        val folderPath = view.findViewById<EditText>(R.id.add_dialog_folder_path)
        val saveButton = view.findViewById<View>(R.id.add_dialog_save_button)

        ipAddress.setText(mapping.serverIp, TextView.BufferType.EDITABLE)
        share.setText(mapping.destinationShare, TextView.BufferType.EDITABLE)
        folderPath.setText(mapping.destinationPath, TextView.BufferType.EDITABLE)

        saveButton.setOnClickListener{
            run {
                val newMapping = Mapping("", ipAddress.text.toString(),
                    share.text.toString(), folderPath.text.toString())
                newMapping.id = mapping.id
                viewModel.updateMapping(newMapping)
                findNavController().navigate(R.id.HomeFragment)
            }
        }

        val dismissButton = view.findViewById<View>(R.id.add_dialog_dismiss_button)
        dismissButton.setOnClickListener {
            run {
                findNavController().navigate(R.id.HomeFragment)
            }
        }


        val deleteButton = view.findViewById<View>(R.id.mapping_dialog_delete_button);
        deleteButton.setOnClickListener { run{
            viewModel.deleteMapping(mapping)
            findNavController().navigate(R.id.HomeFragment)
        } }
    }
}
