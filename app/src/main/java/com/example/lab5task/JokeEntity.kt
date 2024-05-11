package com.example.lab5task
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "jokes")
data class Joke(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val jokeText: String
)
