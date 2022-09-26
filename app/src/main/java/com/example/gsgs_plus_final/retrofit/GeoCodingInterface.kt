package com.example.tmaptest.retrofit

import com.example.gsgs_plus_final.data.routes
import com.example.tmaptest.data.Coordinate
import com.example.tmaptest.data.start
import retrofit2.Call
import retrofit2.http.*


interface GeoCodingInterface {

    //Request URL : http://api.teamcarelab.com:18393/api/player?name=morris
    @GET("tmap/geo/fullAddrGeo")  //@GET 뒤에 기본 URL 뒤에 들어갈 경로가 들어간다.
    fun requestList(
        // @Query안에 ? 뒤에 들어갈 변수가 들어간다.
        @Query("version") version: String ,
        @Query("fullAddr") fullAddr: String,
        @Query("appKey") appKey: String,
    ) : Call<start>

    @FormUrlEncoded
    @POST("tmap/routes")
    fun getRoutes(
        @Field("endX") endX:Double,
        @Field("endY") endY:Double,
        @Field("startX") startX: Double,
        @Field("startY") startY: Double,
        @Field("appKey") appKey: String,
        @Field("totalValue") totalValue:Int
    ):Call<routes>
}