package com.auctech.siprint.home.response

import com.google.gson.annotations.SerializedName

data class UserData(

	@field:SerializedName("date")
	val date: String? = null,

	@field:SerializedName("gender")
	val gender: String? = null,

	@field:SerializedName("photo")
	val photo: String? = null,

	@field:SerializedName("otp")
	val otp: Int? = null,

	@field:SerializedName("selfUpload")
	val selfUpload: String? = null,

	@field:SerializedName("userID")
	val userID: String? = null,

	@field:SerializedName("password")
	val password: String? = null,

	@field:SerializedName("phone")
	val phone: String? = null,

	@field:SerializedName("credits")
	val credits: Int? = null,

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("hostDate")
	val hostDate: String? = null,

	@field:SerializedName("partyUpload")
	val partyUpload: String? = null,

	@field:SerializedName("id")
	val id: String? = null,

	@field:SerializedName("userType")
	val userType: String? = null,

	@field:SerializedName("AddedBy")
	val addedBy: String? = null,

	@field:SerializedName("isLimitAvailable")
	val isLimitAvailable: String? = null,

	@field:SerializedName("fcmToken")
	val fcmToken: String? = null,

	@field:SerializedName("email")
	val email: String? = null,

	@field:SerializedName("status")
	val status: String? = null
)
