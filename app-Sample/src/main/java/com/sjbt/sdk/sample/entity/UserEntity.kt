package com.sjbt.sdk.sample.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(indices = [Index(value = ["name"], unique = true)])
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long, //user id
    val name: String, //unique username
    val password: String, //user password
    val height: Int, //user height(cm)
    val weight: Int, //user weight(kg)
    val sex: Boolean, //True for male, false for female
    val birthYear: Int, //user birth year
    val birthMonth: Int, //user birth month
    val birthDay: Int //user birth day
)