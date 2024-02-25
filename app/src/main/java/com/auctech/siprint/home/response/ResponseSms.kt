package com.auctech.siprint.home.response

import com.google.gson.annotations.SerializedName

data class ResponseSms(

	@field:SerializedName("data")
	val data: List<SmsItem>? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("status")
	val status: Int? = null
)

data class SmsItem(

	@field:SerializedName("sourceID")
	val sourceID: String? = null,

	@field:SerializedName("owner")
	val owner: String? = null,

	@field:SerializedName("date")
	val date: String? = null,

	@field:SerializedName("smsID")
	val smsID: String? = null,

	@field:SerializedName("description")
	val description: String? = null,

	@field:SerializedName("label")
	val label: String? = null,

	@field:SerializedName("status")
	val status: String? = null
)
