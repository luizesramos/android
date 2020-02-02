package com.koalascent.catsncups.game

enum class GameActionType(val value: Int) {
    HIT(0),
    MISS(1),
    NONE(2)
}

class GameAction(val type: GameActionType, val correctCup: Int)