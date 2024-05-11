package com.example.lab5task

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.lab5task.ui.theme.Lab5taskTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

const val CONTENT_URI_JOKES = "content://com.example.lab5task/jokes"
val CONTENT_URI_JOKE_ID: Uri = Uri.parse("content://com.example.lab5task/jokes/#")

suspend fun insertJoke(jokeText: String, context: Context) {
    withContext(Dispatchers.IO) {
        val contentValues = ContentValues().apply {
            put("jokeText", jokeText)
        }
        val uri = Uri.parse(CONTENT_URI_JOKES)
        context.contentResolver.insert(uri, contentValues)
    }
}

suspend fun deleteJoke(jokeId: Long, context: Context): Int {
    return withContext(Dispatchers.IO) {
        val uri = Uri.withAppendedPath(CONTENT_URI_JOKE_ID, jokeId.toString())
        context.contentResolver.delete(uri, null, null)
    }
}

suspend fun updateJoke(jokeId: Long, jokeText: String, context: Context): Int {
    return withContext(Dispatchers.IO) {
        val uri = Uri.withAppendedPath(CONTENT_URI_JOKE_ID, jokeId.toString())
        val values = ContentValues().apply {
            put("jokeText", jokeText)
        }
        context.contentResolver.update(uri, values, null, null)
    }
}

suspend fun queryJokes(context: Context): Cursor? {
    return withContext(Dispatchers.IO) {
        context.contentResolver.query(Uri.parse(CONTENT_URI_JOKES), null, null, null, null)
    }
}

fun extractJokeInfo(joke: String): Pair<Long?, String?> {
    val parts = joke.split(":")
    return if (parts.size == 2) {
        val id = parts[0].trim().toLongOrNull()
        val text = parts[1].trim()
        Pair(id, text)
    } else {
        Pair(null, null)
    }
}


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Lab5taskTheme {
                MainUI()
            }
        }
    }
}


@Composable
fun MainUI() {
    val context = LocalContext.current
    val joke = remember { mutableStateOf("") }
    val jokesState = remember { mutableStateOf<List<String>>(emptyList()) }
    LaunchedEffect(Unit) {
        val cursor = queryJokes(context)
        if (cursor != null) {
            val jokes = mutableListOf<String>()
            while (cursor.moveToNext()) {
                val jokeText = cursor.getString(cursor.getColumnIndexOrThrow("jokeText"))
                jokes.add(jokeText)
            }
            cursor.close()
            jokesState.value = jokes
        }
    }



    Surface(color = Color.White) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {


            // TextField for entering joke text
            TextField(
                value = joke.value,
                onValueChange = { joke.value = it },
                label = { Text("Enter Joke Text") },
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done
                ),

                )

            Spacer(modifier = Modifier.height(16.dp))

            // Button for inserting joke (black color)
            Button(
                onClick = {
                    CoroutineScope(Dispatchers.IO).launch {
                        insertJoke(joke.value, context)
                        // Display toast message based on the result
                        Toast.makeText(context, "Joke inserted successfully", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
            ) {
                Text(text = "Insert", color = Color.White)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Button for deleting joke (red color)
            Button(
                onClick = {
                    CoroutineScope(Dispatchers.IO).launch {
                        val (jokeId, _) = extractJokeInfo(joke.value)
                        if (jokeId != null) {
                            deleteJoke(jokeId, context)
                            // Display toast message based on the result
                            Toast.makeText(context, "Joke deleted successfully", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text(text = "Delete", color = Color.White)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Button for updating joke (yellow color)
            Button(
                onClick = {
                    CoroutineScope(Dispatchers.IO).launch {
                        val (jokeId, jokeText) = extractJokeInfo(joke.value)
                        if (jokeId != null && jokeText != null) {
                            updateJoke(jokeId, jokeText, context)
                            // Display toast message based on the result
                            Toast.makeText(context, "Joke updated successfully", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Green)
            ) {
                Text(text = "Update", color = Color.White)
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn {
                items(items = jokesState.value) { joke ->
                    Text(text = joke, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MainUI()
}
