package org.ndp.ip_test_ns.bean

import com.squareup.moshi.Json

data class MQResult(
    @Json(name = "task-id") val taskID: Int,
    val result: List<IP>,
    val status: Int,
    val desc: String
)