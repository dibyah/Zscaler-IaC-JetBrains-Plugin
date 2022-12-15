package com.zscaler.services

import okhttp3.ResponseBody

import retrofit2.Call

import retrofit2.http.Body

import retrofit2.http.POST


interface CWPService {
    @POST("/iac/onboarding/v1/cli/download")
    fun downloadScanner(@Body body: String?): Call<ResponseBody?>?
}