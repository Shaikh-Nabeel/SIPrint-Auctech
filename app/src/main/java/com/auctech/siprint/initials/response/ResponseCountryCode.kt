package com.auctech.siprint.initials.response

import com.google.gson.annotations.SerializedName

data class ResponseCountryCode(

	@field:SerializedName("code")
	val code: String,

	@field:SerializedName("dial_code")
	val dialCode: String,

	@field:SerializedName("name")
	val name: String
)
