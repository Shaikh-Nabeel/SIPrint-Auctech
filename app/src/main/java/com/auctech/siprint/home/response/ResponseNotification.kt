package com.auctech.siprint.home.response

import com.google.gson.annotations.SerializedName

data class ResponseNotification(

	@field:SerializedName("data")
	val data: List<ItemNotifications>? = null,

	@field:SerializedName("status")
	val status: Int ,

	@field:SerializedName("message")
	val message: String
)

data class ItemNotifications(

	@field:SerializedName("date")
	val date: String,

	@field:SerializedName("description")
	val description: String,

	@field:SerializedName("title")
	val title: String
)
