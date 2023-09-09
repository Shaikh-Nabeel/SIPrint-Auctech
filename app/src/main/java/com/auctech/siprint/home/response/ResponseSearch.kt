package com.auctech.siprint.home.response

import com.google.gson.annotations.SerializedName

data class ResponseSearch(

	@field:SerializedName("data")
	val data: List<InvoicesItem>,

	@field:SerializedName("message")
	val message: String,

	@field:SerializedName("status")
	val status: Int
)
