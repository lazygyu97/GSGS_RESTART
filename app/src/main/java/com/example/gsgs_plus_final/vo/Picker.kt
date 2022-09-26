package com.example.gsgs_plus_final.vo

data class Picker (

    val name : String,
    val sub_name : String,
    val id : String,
    val pwd : String,
    val p_num : String,
    val uid : String,
//    val card_img : String,
    val picker_addr : String,
    val bank_num : String,
    val pick_up_list : List<String>

)