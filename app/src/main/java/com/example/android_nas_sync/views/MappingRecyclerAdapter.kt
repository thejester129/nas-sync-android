package com.example.android_nas_sync.views

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.android_nas_sync.R
import com.example.android_nas_sync.common.TimeUtils
import com.example.android_nas_sync.models.Mapping

class MappingRecyclerAdapter( private var mappings:List<Mapping>,
                              private var onClick: (Mapping) -> Unit) : RecyclerView.Adapter<MappingRecyclerAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val source = itemView.findViewById<TextView>(R.id.mapping_recycler_item_source)!!
        val destination = itemView.findViewById<TextView>(R.id.mapping_recycler_item_destination_ip)!!
        val infoMessage = itemView.findViewById<TextView>(R.id.mapping_recycler_item_last_sync)!!
    }

    fun updateMappings(newMappings:List<Mapping>){
        this.mappings = newMappings
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.mapping_recycler_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val mapping: Mapping = mappings[position]

        viewHolder.itemView.setOnClickListener {
            run {
                onClick(mapping)
            }
        }

        val sourceText = "Source: " + mapping.sourceFolder
        viewHolder.source.text = sourceText

        val destinationText = "Destination: " + mapping.serverIp + "/" +
                mapping.destinationShare + "/" + mapping.destinationPath
        viewHolder.destination.text = destinationText

        val lastSyncTime =  if (mapping.lastSynced == null)  "never"
                            else TimeUtils.unixTimestampToHoursAndMins(mapping.lastSynced!!) + " ago"
        val lastSyncText = "Last synced: $lastSyncTime"
        if(mapping.error != null){
            viewHolder.infoMessage.text = "Error: " + mapping.error
        }
        else{
            viewHolder.infoMessage.text = lastSyncText
        }
    }

    override fun getItemCount(): Int {
        return mappings.size
    }
}