package com.auctech.siprint.initials.response

import com.google.gson.annotations.SerializedName

data class ResponseEmailOtpVerification(

	@field:SerializedName("data")
	val data: DataEmailOtp? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("status")
	val status: Int? = null
)

data class DataEmailOtp(

	@field:SerializedName("id")
	val id: String? = null
)
