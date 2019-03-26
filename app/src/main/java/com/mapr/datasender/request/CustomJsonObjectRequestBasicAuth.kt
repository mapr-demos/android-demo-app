package com.mapr.datasender.request

import android.util.Base64
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import org.json.JSONObject
import com.android.volley.ParseError
import org.json.JSONException
import com.android.volley.toolbox.HttpHeaderParser
import com.android.volley.NetworkResponse
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset

class CustomJsonObjectRequestBasicAuth(
    method: Int, url: String,
    jsonObject: JSONObject?,
    listener: Response.Listener<JSONObject>,
    errorListener: Response.ErrorListener,
    credentials: String
) : JsonObjectRequest(method, url, jsonObject, listener, errorListener) {

    private var mCredentials: String = credentials

    @Throws(AuthFailureError::class)
    override fun getHeaders(): Map<String, String> {
        val headers = HashMap<String, String>()
        headers["Content-Type"] = "application/json"
        val auth = "Basic " + Base64.encodeToString(
            mCredentials.toByteArray(),
            Base64.NO_WRAP
        )
        headers["Authorization"] = auth
        return headers
    }

    override fun parseNetworkResponse(response: NetworkResponse): Response<JSONObject> {
        try {
            val jsonString = String(
                response.data,
                Charset.forName(HttpHeaderParser.parseCharset(response.headers, PROTOCOL_CHARSET))
            )

            var result: JSONObject? = null

            if (jsonString.isNotEmpty())
                result = JSONObject(jsonString)

            return Response.success(
                result,
                HttpHeaderParser.parseCacheHeaders(response)
            )
        } catch (e: UnsupportedEncodingException) {
            return Response.error(ParseError(e))
        } catch (je: JSONException) {
            return Response.error(ParseError(je))
        }

    }

}