package com.mapr.datasender.photo

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.mapr.datasender.config.Config
import com.mapr.datasender.request.CustomJsonObjectRequestBasicAuth
import com.mapr.datasender.request.RequestFormatUtility.getCredentials
import com.mapr.datasender.request.RequestFormatUtility.getUrl
import com.mapr.datasender.request.RequestTag
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*
import java.util.logging.Logger
import javax.inject.Inject
import javax.inject.Singleton

import java.lang.String.format

@Singleton
class PhotoService @Inject constructor(private val requestQueue: RequestQueue, private val androidId: String) {
    private val log = Logger.getLogger(PhotoService::class.java.name)

    fun sendPhoto(currentPhotoPath: String, config: Config, onError: (String) -> Unit) {
        doAsync {
            val bmOptions = getOptions(currentPhotoPath)
            val byteArray = convertToArray(currentPhotoPath, bmOptions, 100)
            File(currentPhotoPath).delete()
            formAndSend(byteArray, config, onError)
        }.execute()
    }

    private fun getOptions(currentPhotoPath: String): BitmapFactory.Options {
        return BitmapFactory.Options().apply {
            inJustDecodeBounds = true
            BitmapFactory.decodeFile(currentPhotoPath, this)
            val photoW: Int = outWidth
            val photoH: Int = outHeight

            val scaleFactor: Int = Math.max(photoW / 1000, photoH / 1000)

            inJustDecodeBounds = false
            inSampleSize = scaleFactor
            inPurgeable = true
        }
    }

    @SuppressLint("DefaultLocale")
    private fun formAndSend(byteArray: ByteArray?, config: Config, onError: (String) -> Unit) {
        val body = JSONObject()
        body.put("androidId", androidId)
        body.put("timestamp", Date().toInstant().epochSecond)
        body.put("format", "jpeg")
        body.put("photo", JSONArray(byteArray))

        val request = CustomJsonObjectRequestBasicAuth(
            Request.Method.PUT, getUrl(config, androidId, RequestTag.PHOTO.tag), body,
            Response.Listener { response ->
                if (response != null)
                    log.info(response.toString())
            },
            Response.ErrorListener { error ->
                run {
                    if (error.message != null) {
                        log.warning(error.message.toString())
                        onError(error.message.toString())
                    } else {
                        val msg: String =
                            format("Failed to connect, code: %d", error.networkResponse?.statusCode)
                        log.warning(msg)
                        onError(msg)
                    }
                }
            }, getCredentials(config)
        )
        request.tag = RequestTag.PHOTO
        requestQueue.add(request)
    }

    private fun convertToArray(currentPhotoPath: String, bmOptions: BitmapFactory.Options, quality: Int): ByteArray? {
        var byteArray: ByteArray? = null
        BitmapFactory.decodeFile(currentPhotoPath, bmOptions)?.also { bitmap ->
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
            byteArray = stream.toByteArray()
        }
        return byteArray
    }

    class doAsync(val handler: () -> Unit) : AsyncTask<Void, Void, Void>() {
        override fun doInBackground(vararg params: Void?): Void? {
            handler()
            return null
        }
    }

}