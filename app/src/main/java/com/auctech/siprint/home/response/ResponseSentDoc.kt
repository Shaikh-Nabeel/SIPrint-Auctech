package com.auctech.siprint.home.response

import com.google.gson.annotations.SerializedName

data class ResponseSentDoc(

	@field:SerializedName("data")
	val data: List<SendDataItem>? = null,

	@field:SerializedName("status")
	val status: Int? = null,

	@field:SerializedName("message")
	val message: String
)

data class SendDataItem(

	@field:SerializedName("sourceID")
	val sourceID: Int? = null,

	@field:SerializedName("owner")
	val owner: Int? = null,

	@field:SerializedName("date")
	val date: String? = null,

	@field:SerializedName("file")
	val file: String? = null,

	@field:SerializedName("owner_name")
	val ownerName: String? = null,

	@field:SerializedName("docID")
	val docID: Int? = null,

	@field:SerializedName("owner_number")
	val ownerNumber: String? = null,

	@field:SerializedName("description")
	val description: String? = null,

	@field:SerializedName("label")
	val label: String? = null,

	@field:SerializedName("status")
	val status: String? = null
)
