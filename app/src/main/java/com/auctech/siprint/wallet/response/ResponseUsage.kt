package com.auctech.siprint.wallet.response

import com.google.gson.annotations.SerializedName

data class ResponseUsage(

    @field:SerializedName("data")
    val data: List<DataItemUsage>? = null,

    @field:SerializedName("status")
    val status: Int? = null,

    @field:SerializedName("message")
    val message: String? = null
)

data class DataItemUsage(

    @field:SerializedName("date")
    val date: String? = null,

    @field:SerializedName("used_limit")
    val usedLimit: String? = null,

    @field:SerializedName("added_limit")
    val addedLimit: String? = null
)
