package com.example.android_nas_sync

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.android_nas_sync.common.TimeUtils
import com.example.android_nas_sync.models.Mapping

class MappingRecyclerAdapter( private var mappings:List<Mapping>) : RecyclerView.Adapter<MappingRecyclerAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val source = itemView.findViewById<TextView>(R.id.mapping_recycler_item_source)!!
        val destination = itemView.findViewById<TextView>(R.id.mapping_recycler_item_destination_ip)!!
        val lastSynced = itemView.findViewById<TextView>(R.id.mapping_recycler_item_last_sync)!!
    }

    fun updateMappings(newMappings:List<Mapping>){
        this.mappings = newMappings
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MappingRecyclerAdapter.ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val contactView = inflater.inflate(R.layout.mapping_recycler_item, parent, false)
        return ViewHolder(contactView)
    }

    override fun onBindViewHolder(viewHolder: MappingRecyclerAdapter.ViewHolder, position: Int) {
        val mapping: Mapping = mappings[position]
        val sourceText = "Source: " + mapping.sourceFolder
        viewHolder.source.text = sourceText

        val destinationText = "Destination: " + mapping.serverIp + "/" +
                mapping.destinationShare + "/" + mapping.destinationPath
        viewHolder.destination.text = destinationText

        val lastSyncTime =  if (mapping.lastSynced == null)  "never"
                            else TimeUtils.unixTimestampToHoursAndMins(mapping.lastSynced)
        val lastSyncText = "Last synced: $lastSyncTime"
        viewHolder.lastSynced.text = lastSyncText
    }

    override fun getItemCount(): Int {
        return mappings.size
    }
}