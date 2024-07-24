package com.example.scramblewordgame.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.scramblewordgame.data.ScrambleUiState
import com.example.scramblewordgame.ui.viewModels.Event
import com.example.scramblewordgame.ui.viewModels.ScrambleViewModel

@Composable
fun HomeScreen(modifier: Modifier, viewModel: ScrambleViewModel) {
    val context = LocalContext.current
    val scrambleUiState = viewModel.scrambleUiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel.sharedEvent) {
        viewModel.sharedEvent.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }

    MyScreen(
        modifier = modifier,
        scrambleUiState,
        {
            viewModel.handleEvents(
                Event.OnSubmit(
                    scrambleUiState.value.textFieldValue,
                    scrambleUiState.value.scrambleWord
                )
            )
        },
        { viewModel.handleEvents(Event.OnSkip) },
        { viewModel.handleEvents(Event.ShowToast("Game Over")) },
        { viewModel.handleEvents(Event.OnResetState) },
        { viewModel.handleEvents(Event.OnTextFieldChange(it)) },
    )
}

@Composable
fun MyScreen(
    modifier: Modifier,
    scrambleUiState: State<ScrambleUiState>,
    onSubmit: () -> Unit,
    onSkip: () -> Unit,
    showToast: () -> Unit,
    onResetState: () -> Unit,
    onTextFieldChange: (String) -> Unit,
) {
    val count = scrambleUiState.value.count
    val scramble = scrambleUiState.value.scrambleWord
    val score = scrambleUiState.value.score
    val textFieldValue = scrambleUiState.value.textFieldValue


    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(
            6.dp,
            Alignment.CenterVertically
        ), // Center and space components
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (count >= 10) {
            DialogComponent(
                modifier = Modifier.fillMaxWidth(), { showToast() },
                { onResetState() }, score
            )
        }

        TitleComponent(modifier = Modifier.fillMaxWidth(), title = "Unscramble")
        CardComponent(
            modifier = Modifier.fillMaxWidth(),
            count = count,
            wordToGuess = scramble.scrambleWord,
            value = textFieldValue
        ) {
            onTextFieldChange(it)
        }
        Button(
            onClick = {
                onSubmit()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            colors = ButtonDefaults.buttonColors(Color.Blue),
            shape = RoundedCornerShape(25.dp),
            enabled = count < 10 && textFieldValue.isNotEmpty(),
        ) {
            Text(
                text = "Submit",
                color = Color.White,
                modifier = Modifier,
                textAlign = TextAlign.Center
            )
        }
        Button(
            onClick = {
                onSkip()
            },
            modifier = Modifier
                .fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(Color.White),
            shape = RoundedCornerShape(25.dp),
            border = BorderStroke(1.dp, Color.Black),
            enabled = count < 10
        ) {
            Text(
                text = "Skip",
                color = Color.Blue,
                modifier = Modifier,
                textAlign = TextAlign.Center
            )
        }
        Text(
            text = "Score: $score",
            modifier = Modifier
                .background(Color.LightGray)
                .padding(vertical = 6.dp, horizontal = 4.dp),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun TitleComponent(modifier: Modifier, title: String) {
    Text(
        text = title,
        modifier = modifier,
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.titleLarge
    )
}

@Composable
fun DialogComponent(
    modifier: Modifier,
    onDismissButton: () -> Unit,
    onConfirmation: () -> Unit,
    score: Int
) {
    AlertDialog(
        modifier = modifier,
        title = {
            Text("The game is over", style = MaterialTheme.typography.headlineLarge)
        },
        text = {
            Text(
                text = "Finally, you've completed the game successfully with a score of $score",
                style = MaterialTheme.typography.bodySmall
            )
        },
        onDismissRequest = {

        },
        confirmButton = {
            Button(onClick = onConfirmation) {
                Text("Play Again", style = MaterialTheme.typography.bodySmall)
            }
        },
        dismissButton = {
            Button(onClick = onDismissButton) {
                Text("Exit", style = MaterialTheme.typography.bodySmall)
            }
        }
    )
}

@Composable
fun CardComponent(
    modifier: Modifier,
    count: Int,
    wordToGuess: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(Color.LightGray),
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$count/10",
                modifier = Modifier
                    .background(Color.Blue, shape = RoundedCornerShape(10.dp))
                    .padding(vertical = 6.dp, horizontal = 10.dp)
                    .align(Alignment.End),
                color = Color.White,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = wordToGuess,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "Unscramble the word using all the letters.",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall
            )
            TextField(
                value = value, onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, Color.Black, RoundedCornerShape(20.dp)),
                maxLines = 1,
            )
        }
    }
}




