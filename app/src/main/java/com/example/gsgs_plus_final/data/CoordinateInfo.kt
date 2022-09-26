package com.example.tmaptest.data

data class CoordinateInfo(
    val addressFlag: String,
    val coordType: String,
    val coordinate: List<Coordinate>,
    val count: String,
    val page: String,
    val totalCount: String
)