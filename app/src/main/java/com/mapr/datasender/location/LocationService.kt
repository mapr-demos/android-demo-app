package com.mapr.datasender.location

import android.location.Location
import android.location.LocationListener
import android.os.Bundle
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import org.json.JSONObject
import javax.inject.Inject

class LocationService(var url: String) : LocationListener {

    @Inject
    lateinit var requestQueue: RequestQueue

    override fun onLocationChanged(location: Location?) {
        val body = JSONObject()
        body.put("latitude", location?.latitude)
        body.put("longitude", location?.longitude)
        val request = JsonObjectRequest(Request.Method.PUT, url, body, { }, { })
        requestQueue.add(request)
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
    }

    override fun onProviderEnabled(provider: String?) {
    }

    override fun onProviderDisabled(provider: String?) {
    }

}