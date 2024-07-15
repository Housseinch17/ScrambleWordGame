package com.example.scramblewordgame

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.scramblewordgame.ui.theme.ScrambleWordGameTheme
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ScrambleWordGameTheme {
                val scrambleViewModel: ScrambleViewModel = viewModel()
                HomeScreen(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    viewModel = scrambleViewModel,
                )
            }
        }
    }
}

data class ScrambleWord(val scrambleWord: String, val correctScramble: String)

data class InputUiState(
    val count: Int = 0,
    val scrambleWord: ScrambleWord = ScrambleWord("", ""),
    val score: Int = 0,
    val set: Set<String> = emptySet(),
    val textFieldValue: String = "",
)

sealed class Event {
    data class OnSubmit(val textFieldValue: String, val scrambleWord: ScrambleWord) : Event()
    data class ShowToast(val message: String) : Event()
    data class OnTextFieldChange(val text: String) : Event()
    data object OnResetState : Event()
    data object OnSkip : Event()
}

@HiltViewModel
class ScrambleViewModel @Inject constructor() : ViewModel() {
    private val _inputUiState = MutableStateFlow(InputUiState())
    val inputUiState = _inputUiState.asStateFlow()


    private val _sharedEvent = MutableSharedFlow<String>()
    val sharedEvent = _sharedEvent.asSharedFlow()


    init {
        initialize()
    }

    private fun initialize() {
        viewModelScope.launch {
            updateScrambleWord()
        }
    }

    fun handleEvents(event: Event) {
        when (event) {
            is Event.OnSubmit -> {
                updateSet(event.scrambleWord)
                increaseCount()
                if (event.textFieldValue == event.scrambleWord.correctScramble)
                    updateScore()
                updateScrambleWord()
                onTextFieldClear()
            }

            is Event.OnSkip -> {
                onTextFieldClear()
                updateScrambleWord()
            }

            is Event.OnTextFieldChange -> onTextFieldChange(event.text)
            is Event.ShowToast -> showToast(event.message)
            is Event.OnResetState -> resetState()
        }
    }

    private fun onTextFieldChange(message: String) {
        _inputUiState.update {
            it.copy(textFieldValue = message)
        }
    }

    private fun onTextFieldClear() {
        _inputUiState.update {
            it.copy(textFieldValue = "")
        }
    }

    private fun showToast(message: String) {
        viewModelScope.launch {
            _sharedEvent.emit(message)
        }
    }

    private fun updateSet(scrambleWord: ScrambleWord) {
        _inputUiState.update {
            it.copy(
                set = it.set + scrambleWord.correctScramble
            )
        }
    }

    private fun updateScore() {
        _inputUiState.update {
            it.copy(
                score = _inputUiState.value.score + 20
            )
        }
    }

    private fun increaseCount() {
        _inputUiState.update {
            it.copy(
                count = _inputUiState.value.count + 1
            )
        }
    }

    private fun resetState() {
        _inputUiState.value = InputUiState()
        initialize()
    }

    private fun updateScrambleWord() {
        val randomScrambleWord: ScrambleWord = getRandomScrambleWord(getSet())
        if (_inputUiState.value.set.contains(randomScrambleWord.correctScramble)) {
            updateScrambleWord()
        } else {
            _inputUiState.update {
                it.copy(
                    scrambleWord = ScrambleWord(
                        randomScrambleWord.scrambleWord,
                        randomScrambleWord.correctScramble
                    )
                )
            }
        }
    }

}

@Composable
fun HomeScreen(modifier: Modifier, viewModel: ScrambleViewModel) {
    val context = LocalContext.current
    val inputUiState = viewModel.inputUiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel.sharedEvent) {
        viewModel.sharedEvent.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }

    MyScreen(
        modifier = modifier,
        inputUiState,
        {
            viewModel.handleEvents(
                Event.OnSubmit(
                    inputUiState.value.textFieldValue,
                    inputUiState.value.scrambleWord
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
    inputUiState: State<InputUiState>,
    onSubmit: () -> Unit,
    onSkip: () -> Unit,
    showToast: () -> Unit,
    onResetState: () -> Unit,
    onTextFieldChange: (String) -> Unit,
) {
    val count = inputUiState.value.count
    val scramble = inputUiState.value.scrambleWord
    val score = inputUiState.value.score
    val textFieldValue = inputUiState.value.textFieldValue


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

fun shuffleString(input: String): String {
    // Convert the string to a mutable list of characters
    val characters = input.toMutableList()

    // Shuffle the list of characters
    characters.shuffle(Random(System.currentTimeMillis()))

    // Convert the shuffled list back to a string
    return characters.joinToString("")
}


fun getSet(): Set<ScrambleWord> {
    return setOf(
        ScrambleWord(shuffleString("animal"), "animal"),
        ScrambleWord(shuffleString("auto"), "auto"),
        ScrambleWord(shuffleString("anecdote"), "anecdote"),
        ScrambleWord(shuffleString("alphabet"), "alphabet"),
        ScrambleWord(shuffleString("balloon"), "balloon"),
        ScrambleWord(shuffleString("basket"), "basket"),
        ScrambleWord(shuffleString("bench"), "bench"),
        ScrambleWord(shuffleString("zoology"), "zoology"),
        ScrambleWord(shuffleString("zeal"), "zeal"),
        ScrambleWord(shuffleString("name"), "name"),
        ScrambleWord(shuffleString("happy"), "happy"),
    )
}

fun getRandomScrambleWord(scrambleWords: Set<ScrambleWord>): ScrambleWord {
    return scrambleWords.random()
}
