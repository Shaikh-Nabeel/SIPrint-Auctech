package com.auctech.siprint.wallet.response

import com.google.gson.annotations.SerializedName

data class ResponseTransaction(

	@field:SerializedName("data")
	val data: List<DataItem>? = null,

	@field:SerializedName("status")
	val status: Int? = null,

	@field:SerializedName("message")
	val message: String? = null
)

data class DataItem(

	@field:SerializedName("date")
	val date: String? = null,

	@field:SerializedName("userId")
	val userId: String? = null,

	@field:SerializedName("transactionId")
	val transactionId: String? = null,

	@field:SerializedName("status")
	val status: String? = null,

	@field:SerializedName("amount")
	val amount: Int? = null
)
