package com.dokubots.firportal
data class Station(val display: String, val psCode: String, val districtCode: String, val psName: String) {
    override fun toString(): String = display
}
data class ArchiveItem(
    val filename: String,
    val ps_name: String,
    val fir_no: String,
    val year: Int,
    val file_id: String,
    val size: String
)
