package com.auctech.siprint.initials.response

import com.google.gson.annotations.SerializedName

data class ResponseSignup(

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("status")
	val status: Int? = null
)
