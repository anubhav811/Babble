package com.anubhav.babble.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.anubhav.babble.models.User
import java.util.ArrayList

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(user: UserEntity)

    @Query("SELECT * FROM user_table")
    fun getUsers(): LiveData<List<User>>
}
