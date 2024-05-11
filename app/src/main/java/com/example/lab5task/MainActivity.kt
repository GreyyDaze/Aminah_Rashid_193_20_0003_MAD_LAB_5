import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
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

val CONTENT_URI_JOKES = "content://your.package.name.provider/jokes"
val CONTENT_URI_JOKE_ID: Uri = Uri.parse("content://your.package.name.provider/jokes/#")

private fun insertJoke(jokeText: String, context: Context) {
    val contentValues = ContentValues().apply {
        put("jokeText", jokeText)
    }

    // Perform insert operation using content resolver
    val uri = Uri.parse(CONTENT_URI_JOKES)
    val resultUri = context.contentResolver.insert(uri, contentValues)
    // Note: resultUri will contain the URI of the newly inserted joke if successful
}

// Function to delete a joke
private fun deleteJoke(jokeId: Long, context: Context): Int {
    // Perform delete operation using content resolver
    val uri = Uri.withAppendedPath(CONTENT_URI_JOKE_ID, jokeId.toString())
    return context.contentResolver.delete(uri, null, null)
}

// Function to update a joke
private fun updateJoke(jokeId: Long, jokeText: String, context: Context): Int {
    // Perform update operation using content resolver
    val uri = Uri.withAppendedPath(CONTENT_URI_JOKE_ID, jokeId.toString())
    val values = ContentValues().apply {
        put("jokeText", jokeText)
    }
    return context.contentResolver.update(uri, values, null, null)
}

// Function to query jokes
private fun queryJokes(context: Context): Cursor? {
    // Perform query operation using content resolver
    return context.contentResolver.query(Uri.parse(CONTENT_URI_JOKES), null, null, null, null)
}

// Function to extract the ID and text from the joke state
private fun extractJokeInfo(joke: String): Pair<Long?, String?> {
    // Assuming the joke is in the format "ID: JokeText"
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
            MainUI()
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
                    val result = insertJoke(joke.value, context)
                    val isSuccess = result != null

                    // Display toast message based on the result
                    val message = if (isSuccess) "Joke inserted successfully" else "Failed to insert joke"
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
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
                    // Perform delete operation
                    val ( jokeId, jokeText) = extractJokeInfo(joke.value)
                    if (jokeId != null) {
                        val result = deleteJoke(jokeId, context)
                        val isSuccess = result > 0
                        if (isSuccess) {
                            Toast.makeText(context, "Joke deleted successfully", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Failed to delete joke", Toast.LENGTH_SHORT).show()
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
                    // Perform update operation
                    // Extract joke ID and text
                    val (jokeId, jokeText) = extractJokeInfo(joke.value)
                    if (jokeId != null && jokeText != null) {
                        val result = updateJoke(jokeId, jokeText, context)
                        val isSuccess = result > 0
                        if (isSuccess) {
                            Toast.makeText(context, "Joke deleted successfully", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Failed to delete joke", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Yellow)
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
