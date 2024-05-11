package com.example.lab5task

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.UriMatcher
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri

class JokeContentProvider : ContentProvider() {

    private lateinit var jokeDao: JokeDao
    private lateinit var uriMatcher: UriMatcher

    companion object {
        const val AUTHORITY = "content://com.example.lab5task"
        val CONTENT_URI_JOKES: Uri = Uri.parse("content://$AUTHORITY/jokes")
        val CONTENT_URI_JOKE_ID: Uri = Uri.parse("content://$AUTHORITY/jokes/#")

        // Define URI matcher constants
        private const val JOKES = 1
        private const val JOKE_ID = 2
    }


    override fun onCreate(): Boolean {
        context?.let {
            val database = JokeDatabase.getDatabase(it)
            jokeDao = database.jokeDao()
        }

        uriMatcher = UriMatcher(UriMatcher.NO_MATCH)
        uriMatcher.addURI(AUTHORITY, "jokes", JOKES)
        uriMatcher.addURI(AUTHORITY, "jokes/#", JOKE_ID)
        return true
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri {
        val id = jokeDao.insertJoke(Joke(jokeText = values?.getAsString("jokeText") ?: ""))
        return ContentUris.withAppendedId(CONTENT_URI_JOKES, id.toString().toLong())
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor {
        val cursor = MatrixCursor(arrayOf("_id", "jokeText")) // Define the columns for the cursor

        val jokes = jokeDao.getAllJokes() // Get the list of jokes from the database

        for (joke in jokes) {
            // Add each joke to the cursor
            cursor.addRow(arrayOf(joke.id, joke.jokeText))
        }

        return cursor
    }


    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        val count: Int
        when (uriMatcher.match(uri)) {
            JOKE_ID -> {
                val jokeId = ContentUris.parseId(uri)
                val jokeText = values?.getAsString("jokeText")

                // Update the joke using DAO method
                count = jokeDao.updateJoke(jokeText ?: "", jokeId)

                // Notify ContentResolver of the change
                context?.contentResolver?.notifyChange(uri, null)
            }
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
        return count
    }



    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        val count: Int
        when (uriMatcher.match(uri)) {
            JOKE_ID -> {
                val jokeId = ContentUris.parseId(uri)

                // Delete the joke using DAO method
                jokeDao.deleteJoke(Joke(id = jokeId, jokeText = ""))

                // Notify ContentResolver of the change
                context?.contentResolver?.notifyChange(uri, null)

                count = 1 // Since we deleted one joke
            }
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
        return count
    }


    override fun getType(uri: Uri): String? {
        // Return the MIME type of the data for the given URI
        return null
    }
}
