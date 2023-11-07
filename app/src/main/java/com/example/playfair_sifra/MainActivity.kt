// Import necessary packages
package com.example.playfair_sifra

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.invalidateGroupsWithKey
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.playfair_sifra.ui.theme.Playfair_SifraTheme
import java.text.Normalizer
import java.util.Locale

// Constant for replacing spaces in the text
const val mezera = "XMEZERAX"

// Function to perform Playfair cipher
fun playfairCipher(plainText: String, keyword: String, czechAlphabet: Boolean): String {
    // Create the Playfair matrix using the provided keyword and alphabet type
    val matrix = createPlayfairMatrix(keyword, czechAlphabet)

    val formattedText = plainText.replace(" ", "")
    // Initialize variables for the ciphered text
    val cipheredText = StringBuilder()
    var i = 0

    try {
        while (i < formattedText.length) {
            // Take two characters from the formatted text
            val char1 = formattedText[i]
            val char2 = formattedText[i + 1]

            Log.d("char1", char1.toString())
            Log.d("char2", char2.toString())

            // Find the positions of these characters in the matrix
            val (row1, col1) = findCharPosition(matrix, char1)
            val (row2, col2) = findCharPosition(matrix, char2)

            // Initialize variables for the new characters
            var newChar1: Char
            var newChar2: Char

            if (row1 == row2) {
                // Characters are in the same row
                newChar1 = matrix[row1][(col1 + 1) % 5]
                newChar2 = matrix[row2][(col2 + 1) % 5]
            } else if (col1 == col2) {
                // Characters are in the same column
                newChar1 = matrix[(row1 + 1) % 5][col1]
                newChar2 = matrix[(row2 + 1) % 5][col2]
            } else {
                // Characters are in different rows and columns
                newChar1 = matrix[row1][col2]
                newChar2 = matrix[row2][col1]
            }

            // Append the new characters to the ciphered text
            cipheredText.append(newChar1)
            cipheredText.append(newChar2)

            i += 2
        }
    } catch (e: Exception) {
        // Log the exception for debugging
        e.printStackTrace()
    }

    return cipheredText.toString().chunked(2).joinToString(" ")
}

// Function to find the position of a character in the Playfair matrix
fun findCharPosition(matrix: List<List<Char>>, char: Char): Pair<Int, Int> {
    for (i in matrix.indices) {
        for (j in matrix[i].indices) {
            if (matrix[i][j] == char) {
                return Pair(i, j)
            }
        }
    }
    // Handle an error case when the character is not found (e.g., replace with a placeholder)
    return Pair(0, 0)
}

// Function to create the Playfair matrix
fun createPlayfairMatrix(keyword: String, czechAlphabet: Boolean): List<List<Char>> {
    val alphabet = if (czechAlphabet) {
        "ABCDEFGHIJKLMNOPQRSTUVXYZ"
    } else {
        "ABCDEFGHIJKLMNPQRSTUVWXYZ"
    }

    val normalizedKeyword = preprocessText(keyword, czechAlphabet)
    val uniqueChars = (normalizedKeyword + alphabet).toSet().toList()

    // Initialize a 5x5 matrix filled with placeholders
    val matrix = List(5) { MutableList(5) { ' ' } }

    // Fill the matrix with the unique characters from the keyword and alphabet
    var charIndex = 0
    for (i in 0 until 5) {
        for (j in 0 until 5) {
            if (charIndex < uniqueChars.size) {
                matrix[i][j] = uniqueChars[charIndex]
                charIndex++
            }
        }
    }

    return matrix
}

// Function to remove diacritics from text
fun removeDiacritics(input: String): String {
    val normalizedString = Normalizer.normalize(input, Normalizer.Form.NFD)
    return normalizedString.replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
}

// Function to preprocess text
fun preprocessText(text: String, czechAlphabet: Boolean): String {
    var preprocessedText = replaceDigitsWithNames(text)
    val regex = Regex("[^a-zA-Z ]")
    preprocessedText = removeDiacritics(preprocessedText)
    preprocessedText = preprocessedText.replace(regex, "")
    Log.d("text", text)
    if (czechAlphabet) return preprocessedText.uppercase(Locale.getDefault()).replace("W", "V")
    return preprocessedText.uppercase(Locale.getDefault()).replace("O", "Q")
}

// Function to replace digits with their names
fun replaceDigitsWithNames(text: String): String {
    val digitToName = mapOf(
        '1' to "JEDNA",
        '2' to "DVA",
        '3' to "TRI",
        '4' to "CTYRI",
        '5' to "PET",
        '6' to "SEST",
        '7' to "SEDM",
        '8' to "OSM",
        '9' to "DEVET",
        '0' to "NULA"
    )
    return text.map { char ->
        if (char.isDigit()) {
            digitToName[char] ?: ""
        } else {
            char.toString()
        }
    }.joinToString("")
}

// Function to process text for ciphering
fun processText(plainText: String): String {
    val formattedText = plainText.replace(" ", "").uppercase(Locale.getDefault())

    // Split the text into pairs
    val textPairs = mutableListOf<String>()
    var i = 0
    while (i < formattedText.length) {
        val char1 = formattedText[i]
        val char2 = if (i + 1 < formattedText.length) formattedText[i + 1] else ' '

        if (char1 == char2) {
            // Handle repeated characters by adding 'X', 'Q', or 'W'
            val paddingChar = getPaddingChar(char1)
            textPairs.add("$char1$paddingChar")
            i += 1 // Skip one character
        } else {
            textPairs.add("$char1$char2")
            i += 2
        }

    }

    if (textPairs.last().replace(" ", "").length == 1) {
        val lastPair = textPairs.last()
        textPairs[textPairs.size - 1] = lastPair.replace(" ", "") + getPaddingChar(lastPair[0])
        Log.d("textPairsLast", textPairs.last().toString())
    }
    Log.d("textPairs", textPairs.toString())
    return textPairs.joinToString(" ")
}

// Function to deprocess text after deciphering
fun deprocessText(cipherText: String): String {
    var deprocessedText = cipherText
    if(deprocessedText.contains("JEDNA")) deprocessedText = deprocessedText.replace("JEDNA", "1")
    if(deprocessedText.contains("DVA")) deprocessedText = deprocessedText.replace("DVA", "2")
    if(deprocessedText.contains("TRI")) deprocessedText = deprocessedText.replace("TRI", "3")
    if(deprocessedText.contains("CTYRI")) deprocessedText = deprocessedText.replace("CTYRI", "4")
    if(deprocessedText.contains("PET")) deprocessedText = deprocessedText.replace("PET", "5")
    if(deprocessedText.contains("SEST")) deprocessedText = deprocessedText.replace("SEST", "6")
    if(deprocessedText.contains("SEDM")) deprocessedText = deprocessedText.replace("SEDM", "7")
    if(deprocessedText.contains("OSM")) deprocessedText = deprocessedText.replace("OSM", "8")
    if(deprocessedText.contains("DEVET")) deprocessedText = deprocessedText.replace("DEVET", "9")
    if(deprocessedText.contains("NULA")) deprocessedText = deprocessedText.replace("NULA", "0")
    if(deprocessedText.contains("XMEZERAX")) deprocessedText = deprocessedText.replace("XMEZERAX", " ")
    return deprocessedText
}

// Function to get padding character based on the last character
fun getPaddingChar(lastChar: Char): Char {
    // Choose 'X', 'Q', or 'W' based on the last character
    return when (lastChar) {
        'X' -> 'Q'
        'Q' -> 'W'
        'W' -> 'X'
        else -> 'X' // Default padding character
    }
}

// Function to perform Playfair decipher
fun playfairDecipher(cipherText: String, keyword: String, czechAlphabet: Boolean): String {
    // Create the Playfair matrix using the provided keyword and alphabet type
    val matrix = createPlayfairMatrix(keyword, czechAlphabet)

    // Remove spaces and convert to uppercase
    val formattedText = cipherText.replace(" ", "").uppercase(Locale.getDefault())

    // Initialize variables for the deciphered text
    val decipheredText = StringBuilder()
    var i = 0

    while (i < formattedText.length) {
        val char1 = formattedText[i]
        val char2 = formattedText[i + 1]

        // Find the positions of these characters in the matrix
        val (row1, col1) = findCharPosition(matrix, char1)
        val (row2, col2) = findCharPosition(matrix, char2)

        // Initialize variables for the new characters
        var newChar1: Char
        var newChar2: Char

        if (row1 == row2) {
            // Characters are in the same row
            newChar1 = matrix[row1][(col1 - 1 + 5) % 5]
            newChar2 = matrix[row2][(col2 - 1 + 5) % 5]
        } else if (col1 == col2) {
            // Characters are in the same column
            newChar1 = matrix[(row1 - 1 + 5) % 5][col1]
            newChar2 = matrix[(row2 - 1 + 5) % 5][col2]
        } else {
            // Characters are in different rows and columns
            newChar1 = matrix[row1][col2]
            newChar2 = matrix[row2][col1]
        }

        // Append the new characters to the deciphered text
        decipheredText.append(newChar1)
        decipheredText.append(newChar2)

        i += 2
    }
    Log.d("decipheredText", decipheredText.toString())
    return decipheredText.toString()
}

// Entry point of the app
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Playfair_SifraTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SimpleOutlinedTextFieldSample()
                }
            }
        }
    }
}

// Composable function for the user interface
@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun SimpleOutlinedTextFieldSample() {
    // State variables to store user input and settings
    var plainText by remember { mutableStateOf("") }
    var key by remember { mutableStateOf("") }
    var textFormated by remember { mutableStateOf("") }
    var textProcessed by remember { mutableStateOf("") }
    var ciphredText by remember { mutableStateOf("") }
    var decipheredText by remember { mutableStateOf("") }
    var czechAlphabet by remember { mutableStateOf(false) }
    var plaindecipheredText by remember { mutableStateOf("Plain deciphered text: ") }
    var showDialog by remember { mutableStateOf(false) }

    // UI layout
    Box(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val context = LocalContext.current
            OutlinedTextField(
                value = plainText,
                onValueChange = { plainText = it },
                label = {
                    Text(
                        "Plain text"
                    )
                }
            )
            Column {
                OutlinedTextField(
                    value = key,
                    onValueChange = {
                        if (key.length > 25) key =
                            it.drop(1).uppercase().filter { it.isLetter() } else key =
                            it.uppercase().filter { it.isLetter() }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    label = { Text("Key") },
                )
            }

            Row(horizontalArrangement = Arrangement.Absolute.SpaceBetween) {
                Text(text = textFormated, fontWeight = FontWeight.Bold)
            }
            Text(text = textProcessed, fontWeight = FontWeight.Bold)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(onClick = {
                    if (isPlayfairKeyValid(key)) {
                        Toast.makeText(context, "Key is valid", Toast.LENGTH_SHORT).show()
                    } else {
                        showDialog = true
                    }
                }) {
                    Text(text = "Check KEY")
                }
                Text(
                    modifier = Modifier.padding(10.dp),
                    textAlign = TextAlign.Center,
                    text = "Eng"
                )

                Switch(
                    checked = czechAlphabet,
                    onCheckedChange = {
                        czechAlphabet = it
                    }

                )
                Text(
                    modifier = Modifier.padding(10.dp),
                    textAlign = TextAlign.Center,
                    text = "Cz"
                )
            }

            Button(onClick = {
                textFormated =
                    "Preproccesed text: " + preprocessText(plainText, czechAlphabet).replace(
                        " ",
                        mezera
                    )
                Log.d("textFormated", textFormated)
                textProcessed = "Processed text: " + processText(textFormated.drop(19))
                Log.d("textProcessed", textProcessed)
                ciphredText = playfairCipher(textProcessed.drop(16), key, czechAlphabet)
                Log.d("ciphredText", ciphredText)
            }) {
                Text(text = "Cipher")
                Modifier.fillMaxWidth(0f)
            }
            OutlinedTextField(
                value = ciphredText,
                onValueChange = { ciphredText = it },
                label = { Text("Ciphered text") }
            )
            Button(onClick = {
                plaindecipheredText =
                    "Plain deciphered text: " + playfairDecipher(ciphredText, key, czechAlphabet).uppercase()
                decipheredText = deprocessText(plaindecipheredText.drop(23))
            }) {
                Text(text = "Decipher")
            }
            Text(text = plaindecipheredText, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = decipheredText,
                onValueChange = { decipheredText = it },
                label = { Text("Deciphered text") })
            Spacer(modifier = Modifier.padding(10.dp))
            PlayfairMatrixUI(key, czechAlphabet)
            if (showDialog) {
                AlertDialogExample(
                    onDismissRequest = { showDialog = false },
                    onConfirmation = {
                        // Handle confirmation logic here
                        showDialog = false
                    },
                    dialogTitle = "Špatný klíč",
                    dialogText = "Klíč musí být delší než 10 znaků a musí obsahovat pouze unikátní znaky.",
                    icon = Icons.Default.Warning // Use the appropriate icon
                )
            }
        }
    }
}

fun isPlayfairKeyValid(key: String): Boolean {
    if (key.length > 10 && hasUniqueCharacters(key)) return true
    return false
}

fun hasUniqueCharacters(input: String): Boolean {
    // Create a set to store characters that have been seen
    val seenChars = mutableSetOf<Char>()

    for (char in input) {
        if (seenChars.contains(char)) {
            // Character is repeated; the string does not have unique characters
            return false
        }
        seenChars.add(char)
    }

    // All characters are unique
    return true
}


// Composable function to display the Playfair matrix
@Composable
fun PlayfairMatrixUI(keyword: String, czechAlphabet: Boolean) {
    val matrix = createPlayfairMatrix(keyword, czechAlphabet)

    LazyColumn {
        items(matrix.size) { rowIndex ->
            val row = matrix[rowIndex]
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                row.forEach { char ->
                    MatrixCell(char)
                }
            }
        }
    }
}

// Composable function to display a cell of the Playfair matrix
@Composable
fun MatrixCell(char: Char) {
    Surface(
        modifier = Modifier.size(40.dp),
        shape = MaterialTheme.shapes.small,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = char.toString(), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun AlertDialogExample(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    dialogTitle: String,
    dialogText: String,
    icon: ImageVector
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(text = dialogTitle) },
        text = { Text(text = dialogText) },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmation()
                }
            ) {
                Text(text = "OK")
            }
        },
        icon = {
            Icon(imageVector = icon, contentDescription = null)
        }
    )
}

@Preview(showBackground = true)
@Composable
fun PlayfairMatrixPreview() {
    PlayfairMatrixUI("KEYWORD", true)
}