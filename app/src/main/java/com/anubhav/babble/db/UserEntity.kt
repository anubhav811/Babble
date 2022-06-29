package com.anubhav.babble.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "user_table")
data class UserEntity (
    @PrimaryKey
    var uid: String,
    var name: String,
    var phoneNumber : String,
    var profileImage: String,
    var token : String,

    ): Serializable {
    constructor(): this("","","","","")
}