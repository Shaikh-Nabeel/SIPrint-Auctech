package com.auctech.siprint.wallet.adapter
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.auctech.siprint.R

import com.auctech.siprint.wallet.response.DataItemUsage
import java.lang.Exception


class UsageAdapter(private val context: Context, private val usageList: List<DataItemUsage>) :
    RecyclerView.Adapter<UsageAdapter.UsageViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): UsageViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.adapter_usage, parent, false)
        return UsageViewHolder(view)
    }

    override fun onBindViewHolder(holder: UsageViewHolder, position: Int) {
        try{
            val (date, usedLimit, addedLimit) = usageList[position]

            // Set date
            holder.dateTextView.text = date

            // Check if added_limit is null, if yes, set used_limit
            if (addedLimit.isNullOrEmpty()) {
                holder.limitTextView.text = usedLimit
                holder.usedOrAddTextView.text = "Credit Used"
            } else {
                // Set added_limit
                holder.limitTextView.text = addedLimit
                holder.usedOrAddTextView.text = "Credit Added"
            }
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    override fun getItemCount(): Int {
        return usageList.size
    }

    class UsageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var dateTextView: TextView
        var limitTextView: TextView
        var usedOrAddTextView: TextView

        init {
            dateTextView = itemView.findViewById(R.id.date)
            limitTextView = itemView.findViewById(R.id.limit)
            usedOrAddTextView = itemView.findViewById(R.id.usedOrAdd)
        }
    }
}
