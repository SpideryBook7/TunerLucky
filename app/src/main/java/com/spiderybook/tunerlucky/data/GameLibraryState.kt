package com.spiderybook.tunerlucky.data

data class GameLibraryState(

    val games: List<GameInfo> = emptyList(),

    val selectedGame: GameInfo? = null
)