package com.auctech.siprint.initials.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.auctech.siprint.R
import com.auctech.siprint.initials.IClickListener
import org.json.JSONArray

class CountryCodeLV(context: Context, private val listener: IClickListener, private val countryList: JSONArray) :
    ArrayAdapter<JSONArray>(context, 0, mutableListOf()) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var listItem = convertView
        if (listItem == null) {
            listItem = LayoutInflater.from(context).inflate(R.layout.listview_country_code, parent, false)
        }

        val country = countryList.getJSONObject(position)
        val countryName = listItem!!.findViewById<TextView>(R.id.countryName)
        val dialCode = listItem.findViewById<TextView>(R.id.dialCode)

        countryName.text = country.getString("name")
        dialCode.text = country.getString("dial_code")
        listItem.setOnClickListener{
            listener.onCountrySelected(country.getString("dial_code"),country.getString("name"))
        }
        return listItem
    }

    override fun getCount(): Int {
        return countryList.length()
    }
}