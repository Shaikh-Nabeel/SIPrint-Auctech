package com.auctech.siprint.home.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.auctech.siprint.R
import com.auctech.siprint.home.interfaces.Mvp
import com.auctech.siprint.home.response.SmsItem
import java.text.SimpleDateFormat
import java.util.Locale

class SmsAdapter(var smsList: List<SmsItem>,val listener: Mvp) :
    RecyclerView.Adapter<SmsAdapter.SmsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SmsViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.adapter_notifications, parent, false)
        return SmsViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: SmsViewHolder, position: Int) {
        val currentItem = smsList[position]

        holder.notificationIcon.visibility = View.GONE
        holder.dateTime.visibility = View.VISIBLE

        // Set values to views
        holder.title.text = currentItem.label
        holder.dateTime.text = formatDate(currentItem.date!!)
        holder.description.text = currentItem.description

        // Set click listener
        holder.itemView.setOnClickListener {
            listener.onClick(position)
        }
    }

    override fun getItemCount() = smsList.size

    class SmsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val notificationIcon: ImageView = itemView.findViewById(R.id.notificationIcon)
        val title: TextView = itemView.findViewById(R.id.title)
        val dateTime: TextView = itemView.findViewById(R.id.dateTime)
        val description: TextView = itemView.findViewById(R.id.description)
    }

    private fun formatDate(inputDate: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd/MM/yy hh:mm a", Locale.getDefault())
        val date = inputFormat.parse(inputDate)
        return outputFormat.format(date!!)
    }
}
