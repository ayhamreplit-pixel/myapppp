package com.example.model

data class XtreamCategory(
  val categoryId: String,
  val categoryName: String
)

data class XtreamChannel(
  val streamId: String,
  val name: String,
  val iconUrl: String? = null,
  val categoryId: String? = null,
  val playUrl: String
)

data class XtreamAccountInfo(
  val username: String,
  val status: String,
  val expDate: String?,
  val serverUrl: String
)
