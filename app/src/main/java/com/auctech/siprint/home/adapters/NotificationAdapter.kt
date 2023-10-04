package com.auctech.siprint.home.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.auctech.siprint.R
import com.auctech.siprint.home.response.ItemNotifications

class NotificationAdapter(private val notifications: List<ItemNotifications>) :


    RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    inner class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val titleTv: TextView = itemView.findViewById(R.id.title)
        private val descriptionTv: TextView = itemView.findViewById(R.id.description)

        fun bind(notification: ItemNotifications) {
            try {
                titleTv.text = notification.title
                descriptionTv.text = notification.description
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val itemView =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.adapter_notifications, parent, false)
        return NotificationViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = notifications[position]
        holder.bind(notification)
    }

    override fun getItemCount(): Int {
        return notifications.size
    }
}
