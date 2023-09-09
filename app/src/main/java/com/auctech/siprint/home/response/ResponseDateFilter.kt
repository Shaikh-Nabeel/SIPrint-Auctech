package com.auctech.siprint.home.response

import com.google.gson.annotations.SerializedName

data class ResponseDateFilter(

	@field:SerializedName("data")
	val data: List<InvoicesItem>,

	@field:SerializedName("message")
	val message: String,

	@field:SerializedName("status")
	val status: Int
)

data class DataItem(

	@field:SerializedName("sourceID")
	val sourceID: Int,

	@field:SerializedName("owner")
	val owner: Int,

	@field:SerializedName("date")
	val date: String,

	@field:SerializedName("file")
	val file: String,

	@field:SerializedName("docID")
	val docID: Int,

	@field:SerializedName("description")
	val description: String,

	@field:SerializedName("label")
	val label: String,

	@field:SerializedName("source_name")
	val sourceName: String,

	@field:SerializedName("status")
	val status: String
)
