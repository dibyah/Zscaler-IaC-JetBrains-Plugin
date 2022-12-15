package com.zscaler.services

import retrofit2.Retrofit

class CWPClient {

    companion object {
        @Volatile
        private var client: Retrofit? = null

        @Throws(Exception::class)
        private fun initRetrofit() {
            client = ClientUtils.createRetrofit("https://main.dev.api.zscwp.io", ClientUtils.getAuthInterceptor())
        }

        @Throws(Exception::class)
        fun getClient(): Retrofit? {
            if (client == null) {
                synchronized(CWPClient::class.java) {
                    if (client == null) {
                        initRetrofit()
                    }
                }
            }
            return client
        }
    }
}