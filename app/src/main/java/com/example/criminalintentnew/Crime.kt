package com.example.criminalintentnew

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.net.FileNameMap
import java.util.*

@Entity
data class Crime(
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    var title: String = "",
    var date: Date = Date(),
    var isSolved: Boolean = false,
    var requiresPolice: Int = 0,
    var suspect: String ="",
    var idSuspect: Int = 0,
    var phoneSuspect: String = "") {
    val photoFileName
        get() = "IMG_$id.jpg"
}