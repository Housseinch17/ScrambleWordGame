package com.example.scramblewordgame.data

import kotlin.random.Random

object DataSource {
    private val scrambleWordSet: Set<ScrambleWord> = setOf(
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

    private fun shuffleString(input: String): String {
        // Convert the string to a mutable list of characters
        val characters = input.toMutableList()

        // Shuffle the list of characters
        characters.shuffle(Random(System.currentTimeMillis()))

        // Convert the shuffled list back to a string
        return characters.joinToString("")
    }


    fun getRandomScrambleWord(excludeSet: Set<String>): ScrambleWord {
        val allScrambleWords = scrambleWordSet

        // Filter out scramble words that are already in the excludeSet
        val availableWords = allScrambleWords.filterNot { it.correctScramble in excludeSet }

        // Return a random word from the available words
        return availableWords.random()
    }
    }