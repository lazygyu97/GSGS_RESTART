package com.example.gsgs_plus_final.vo

data class PickUpRequest(

        val name: String,
        val id: String,
        val p_num: String,
        val uid: String,
        val pick_up_item_name: String,
//        val pick_up_item_img : String,
        val pick_up_item_addr_start: String,
        val pick_up_item_addr_end: String,
        val pick_up_item_request: String,
        val pick_up_item_cost: String,
        val pick_up_check_flag: String,
        val startX: String?,
        val startY: String?,
        val endX: String?,
        val endY: String?
        )