package com.auctech.siprint.wallet.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.auctech.siprint.R
import com.auctech.siprint.wallet.response.DataItem
import com.auctech.siprint.wallet.response.ResponseTransaction


class TransactionAdapter(private val transactions: List<DataItem>) :
    RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.adapter_transactions, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        try {
            val transaction = transactions[position]

            holder.amountTextView.text = "$${transaction.amount}"
            holder.transactionIdTextView.text = transaction.transactionId
            holder.dateTextView.text = transaction.date?.substring(0,12) ?: ""
            holder.statusTextView.text = transaction.status
            if (transaction.status == "SUCCESS") {
                holder.statusTextView.setTextColor(
                    ContextCompat.getColor(
                        holder.statusTextView.context,
                        R.color.green
                    )
                )
            } else {
                holder.statusTextView.setTextColor(
                    ContextCompat.getColor(
                        holder.statusTextView.context,
                        R.color.red
                    )
                )
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getItemCount(): Int {
        return transactions.size
    }

    class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val amountTextView: TextView = itemView.findViewById(R.id.amount)
        val transactionIdTextView: TextView = itemView.findViewById(R.id.transactionId)
        val dateTextView: TextView = itemView.findViewById(R.id.date)
        val statusTextView: TextView = itemView.findViewById(R.id.status)
    }
}
