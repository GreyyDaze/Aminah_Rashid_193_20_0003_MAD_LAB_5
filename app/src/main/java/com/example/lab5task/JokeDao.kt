package com.example.lab5task

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface JokeDao {
    @Query("SELECT * FROM jokes")
    fun getAllJokes(): List<Joke>

    @Insert
    fun insertJoke(joke: Joke)

    @Query("UPDATE jokes SET jokeText = :jokeText WHERE id = :id")
    fun updateJoke(jokeText: String, id: Long): Int

    @Delete
    fun deleteJoke(joke: Joke)
}
