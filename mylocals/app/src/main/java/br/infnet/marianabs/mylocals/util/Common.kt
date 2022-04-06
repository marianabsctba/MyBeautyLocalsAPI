package br.infnet.marianabs.mylocals.util

import br.infnet.marianabs.mylocals.remote.IGoogleAPIService
import br.infnet.marianabs.mylocals.remote.RetrofitClient

object Common {
    private val GOOGLE_API_URL="https://maps.googleapis.com/"

    val googleApiService: IGoogleAPIService
        get()= RetrofitClient.getClient(GOOGLE_API_URL).create(IGoogleAPIService::class.java)
}