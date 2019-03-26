package com.mapr.datasender.request

import android.annotation.SuppressLint
import com.mapr.datasender.config.Config
import java.lang.String.format

object RequestFormatUtility {
    @JvmStatic
    fun getCredentials(config: Config): String = format("%s:%s", config.username, config.password)

    @SuppressLint("DefaultLocale")
    @JvmStatic
    fun getUrl(config: Config, androidId: String, dataType: String): String =
        format("http://%s:%d/data/%s/%s", config.host, config.port, androidId, dataType)
}