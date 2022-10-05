package com.example.gsgs_plus_final.vo

//픽업 할게요 리스트
data class pick_list(
    val item_name : String?,
    val addr_start_f: String?,
    val addr_end_f: String?,
    val addr_start: String?,
    val addr_end: String?,
    val startX : String?,
    val startY : String?,
    val endx : String?,
    val endY : String?,
    val request_cost : String?,
    val document_id : String?,
    val pick_up_check_flag : String?
)