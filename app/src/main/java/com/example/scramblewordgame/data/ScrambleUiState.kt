package com.example.scramblewordgame.data

data class ScrambleUiState(
    val count: Int = 0,
    val scrambleWord: ScrambleWord = ScrambleWord("", ""),
    val score: Int = 0,
    val set: Set<String> = emptySet(),
    val textFieldValue: String = "",
)

