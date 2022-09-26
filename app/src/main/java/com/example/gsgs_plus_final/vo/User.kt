package com.example.gsgs_plus_final.vo

data class User (

    val name : String,
    val sub_name : String,
    val id : String,
    val pwd : String,
    val p_num : String,
    val uid : String,
    val picker_flag : String,
    val doing_flag : String,
    val pick_up_list : List<String>
)