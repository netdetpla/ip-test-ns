package org.ndp.ip_test_ns.bean

import com.squareup.moshi.Json


data class MQTask(
    @Json(name = "task-id") val taskID: Int,
    @Json(name = "full-image-name") val fullImageName: String,
    val param: String
)