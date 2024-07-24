package com.example.scramblewordgame.ui.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scramblewordgame.data.DataSource
import com.example.scramblewordgame.data.ScrambleUiState
import com.example.scramblewordgame.data.ScrambleWord
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScrambleViewModel @Inject constructor() : ViewModel() {
    private val _scrambleUiState = MutableStateFlow(ScrambleUiState())
    val scrambleUiState = _scrambleUiState.asStateFlow()


    private val _sharedEvent = MutableSharedFlow<String>()
    val sharedEvent = _sharedEvent.asSharedFlow()


    init {
        viewModelScope.launch {
            initialize()
        }
    }

    private fun initialize() {
        viewModelScope.launch {
            updateScrambleWord()
        }
    }

    fun handleEvents(event: Event) {
        viewModelScope.launch {
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
    }

    private fun onTextFieldChange(message: String) {
        _scrambleUiState.update {
            it.copy(textFieldValue = message)
        }
    }

    private fun onTextFieldClear() {
        _scrambleUiState.update {
            it.copy(textFieldValue = "")
        }
    }

    private fun showToast(message: String) {
        viewModelScope.launch {
            _sharedEvent.emit(message)
        }
    }

    private fun updateSet(scrambleWord: ScrambleWord) {
        _scrambleUiState.update {
            it.copy(
                set = it.set + scrambleWord.correctScramble
            )
        }
    }

    private fun updateScore() {
        _scrambleUiState.update {
            it.copy(
                score = _scrambleUiState.value.score + 20
            )
        }
    }

    private fun increaseCount() {
        _scrambleUiState.update {
            it.copy(
                count = _scrambleUiState.value.count + 1
            )
        }
    }

    private fun resetState() {
        _scrambleUiState.value = ScrambleUiState()
        initialize()
    }

    private fun updateScrambleWord() {
        val randomScrambleWord: ScrambleWord =
            DataSource.getRandomScrambleWord(_scrambleUiState.value.set)

        _scrambleUiState.update {
            it.copy(scrambleWord = randomScrambleWord)
        }
    }
}

sealed class Event {
    data class OnSubmit(val textFieldValue: String, val scrambleWord: ScrambleWord) : Event()
    data class ShowToast(val message: String) : Event()
    data class OnTextFieldChange(val text: String) : Event()
    data object OnResetState : Event()
    data object OnSkip : Event()
}
