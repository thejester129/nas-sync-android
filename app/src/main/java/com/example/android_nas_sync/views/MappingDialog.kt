package com.example.android_nas_sync.views

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.android_nas_sync.R
import com.example.android_nas_sync.databinding.MappingDialogBinding
import com.example.android_nas_sync.models.Mapping
import com.example.android_nas_sync.viewmodels.MappingsViewModel

class MappingDialog : Fragment() {
    private var _binding: MappingDialogBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MappingsViewModel by activityViewModels()
    private var FOLDER_PICK_ACTIVITY_CODE = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(inflater, R.layout.mapping_dialog, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        val view = binding.root
        initBindings(view)
        return binding.root
    }


    private fun initBindings(view: View){
        val saveButton = view.findViewById<View>(R.id.add_dialog_save_button)
        saveButton.setOnClickListener{
            run {
                if(correctFields(viewModel.currentlyEditedMapping.value)){
                    viewModel.updateCurrentEdited()
                    findNavController().navigate(R.id.HomeFragment)
                }
               else{
                    Toast.makeText(activity, "Missing fields in mapping", Toast.LENGTH_SHORT).show()
                }
            }
        }

        val dismissButton = view.findViewById<View>(R.id.add_dialog_dismiss_button)
        dismissButton.setOnClickListener {
            run {
                viewModel.currentlyEditedMapping.value = null
                findNavController().navigate(R.id.HomeFragment)
            }
        }

        val filePickButton = view.findViewById<View>(R.id.mapping_dialog_file_picker_button)
        filePickButton.setOnClickListener {
            run{
                openDirectory()
            }
        }

        val deleteButton = view.findViewById<View>(R.id.mapping_dialog_delete_button);
        if(viewModel.canDeleteCurrentlyEdited){
            deleteButton.setOnClickListener {
                run {
                    viewModel.deleteCurrentlyEdited()
                    findNavController().navigate(R.id.HomeFragment)
                }
            }
        }
        else{
            deleteButton.visibility = View.GONE
        }
    }

    private fun correctFields(mapping: Mapping?):Boolean{
        return mapping != null &&
                !mapping.destinationShare.isNullOrEmpty() &&
                !mapping.serverIp.isNullOrEmpty() &&
                !mapping.sourceFolder.isNullOrEmpty()
    }
    private fun openDirectory() {
        // Choose a directory using the system's file picker.
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE, ).apply {
            // Optionally, specify a URI for the directory that should be opened in
            // the system file picker when it loads.
            //putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri)
            addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        }

        startActivityForResult(activity as Activity,intent, FOLDER_PICK_ACTIVITY_CODE, null )
    }

}
