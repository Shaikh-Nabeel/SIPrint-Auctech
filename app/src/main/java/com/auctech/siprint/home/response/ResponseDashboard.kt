package com.auctech.siprint.home.response

import com.google.gson.annotations.SerializedName

data class ResponseDashboard(

	@field:SerializedName("data")
	val data: Data? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("status")
	val status: Int? = null
)

data class InvoicesItem(

	@field:SerializedName("sourceID")
	val sourceID: Int? = null,

	@field:SerializedName("owner")
	val owner: Int? = null,

	@field:SerializedName("date")
	val date: String? = null,

	@field:SerializedName("file")
	val file: String? = null,

	@field:SerializedName("docID")
	val docID: Int? = null,

	@field:SerializedName("description")
	val description: String? = null,

	@field:SerializedName("label")
	val label: String? = null,

	@field:SerializedName("source_name")
	val sourceName: String? = null,

	@field:SerializedName("status")
	val status: String? = null
)

//data class UserData(
//
//	@field:SerializedName("date")
//	val date: String? = null,
//
//	@field:SerializedName("photo")
//	val photo: String? = null,
//
//	@field:SerializedName("otp")
//	val otp: Int? = null,
//
//	@field:SerializedName("userID")
//	val userID: String? = null,
//
//	@field:SerializedName("phone")
//	val phone: String? = null,
//
//	@field:SerializedName("credits")
//	val credits: Int? = null,
//
//	@field:SerializedName("name")
//	val name: String? = null,
//
//	@field:SerializedName("id")
//	val id: Int? = null,
//
//	@field:SerializedName("userType")
//	val userType: String? = null,
//
//	@field:SerializedName("AddedBy")
//	val addedBy: String? = null,
//
//	@field:SerializedName("isLimitAvailable")
//	val isLimitAvailable: Int? = null,
//
//	@field:SerializedName("email")
//	val email: String? = null,
//
//	@field:SerializedName("status")
//	val status: String? = null
//)

data class Data(

	@field:SerializedName("invoices")
	val invoices: List<InvoicesItem?>? = null,

	@field:SerializedName("user_data")
	val userData: UserData? = null
)
