package com.auctech.siprint.home.response

import com.google.gson.annotations.SerializedName

data class ResponseSearchUser(

	@field:SerializedName("data")
	val data: List<UserItem>,

	@field:SerializedName("message")
	val message: String,

	@field:SerializedName("status")
	val status: Int
)

data class UserItem(

	@field:SerializedName("phone")
	val phone: String,

	@field:SerializedName("name")
	val name: String,

	@field:SerializedName("photo")
	val photo: String,

	@field:SerializedName("id")
	val id: Int
)
