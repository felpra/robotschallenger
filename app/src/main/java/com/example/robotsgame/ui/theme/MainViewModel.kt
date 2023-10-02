package com.example.robotsgame.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.robotsgame.ui.theme.enums.MatrixStates
import com.example.robotsgame.ui.theme.enums.Movements
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex

class MainViewModel: ViewModel() {
    private var score: Pair<Int, Int> = Pair(0, 0)
    private val mutex = Mutex()
    private var prizeSet: Boolean = false
    private var r1Wins = false
    private var r2Wins = false
    private var currentRobot = 1 //The last one to move will be the first to move in the next round
    private var boardState = Array(7) { Array(7) { MatrixStates.UNVISITED } }
    private val _gameStateFlow = MutableStateFlow(UiState(boardState))
    val gameStateFlow: StateFlow<UiState> get() = _gameStateFlow

    companion object{
        val listMov = listOf(Movements.LEFT, Movements.DOWN, Movements.RIGHT, Movements.TOP)
    }

    init {
        setDefaultBoard()
    }

    private fun startGame(){
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                while (gameStateFlow.value.movementsLeft >= 0) {
                    mutex.lock()
                    if (currentRobot == 1) {
                        mov1()
                        currentRobot = 2
                    } else {
                        mov2()
                        currentRobot = 1
                    }
                    mutex.unlock()
                }
            }
        }
    }

    private suspend fun mov1(){
        moveRobot(1)
    }

    private suspend fun mov2(){
        moveRobot(2)
    }

    private suspend fun moveRobot(robotNumber: Int) {
        delay(500) //This delay was added only to allow a clearer visualization of the robots' movement.
        val position = if (robotNumber == 1) gameStateFlow.value.positionR1 else gameStateFlow.value.positionR2
        val currentState = if (robotNumber == 1) MatrixStates.R1L else MatrixStates.R2L
        val robotState = if (robotNumber == 1) MatrixStates.ROBOT1 else MatrixStates.ROBOT2
        var wins = if (robotNumber == 1) r1Wins else r2Wins
        val otherRobotScore = if (robotNumber == 1) gameStateFlow.value.score.second else gameStateFlow.value.score.first

        val movement = drawMovement(position.second, position.first, gameStateFlow.value.gameState)

        if (movement != Movements.NO_MOVEMENT) {
            val newPosition = when (movement) {
                Movements.RIGHT -> Pair(position.first, position.second + 1)
                Movements.TOP -> Pair(position.first - 1, position.second)
                Movements.LEFT -> Pair(position.first, position.second - 1)
                Movements.DOWN -> Pair(position.first + 1, position.second)
                else -> position
            }

            if (boardState[newPosition.first][newPosition.second] == MatrixStates.PRIZE) {
                wins = true
                score = if (robotNumber == 1) Pair(gameStateFlow.value.score.first + 1, otherRobotScore) else Pair(otherRobotScore, gameStateFlow.value.score.second + 1)
            }

            boardState[position.first][position.second] = currentState
            boardState[newPosition.first][newPosition.second] = robotState

            _gameStateFlow.update {
                it.copy(
                    gameState = boardState,
                    positionR1 = if (robotNumber == 1) newPosition else gameStateFlow.value.positionR1,
                    positionR2 = if (robotNumber == 2) newPosition else gameStateFlow.value.positionR2,
                    r1Wins = if (robotNumber == 1) wins else gameStateFlow.value.r1Wins,
                    r2Wins = if (robotNumber == 2) wins else gameStateFlow.value.r2Wins,
                    movementsLeft = gameStateFlow.value.movementsLeft - 1,
                    score = score
                )
            }
        } else {
            _gameStateFlow.update {
                if (robotNumber == 1) it.copy(r1Stuck = true) else it.copy(r2Stuck = true)
            }
        }
    }

    private fun drawMovement(i: Int, j: Int, boardState: Array<Array<MatrixStates>>): Movements {
        val defaultList = listMov.toMutableList()
        if(i-1 < 0 || (boardState[j][i-1] != MatrixStates.UNVISITED && boardState[j][i-1] != MatrixStates.PRIZE))
            defaultList.remove(Movements.LEFT)
        if(i+1 == 7 || (boardState[j][i+1] != MatrixStates.UNVISITED && boardState[j][i+1] != MatrixStates.PRIZE))
            defaultList.remove(Movements.RIGHT)
        if(j-1 < 0 || (boardState[j-1][i] != MatrixStates.UNVISITED && boardState[j-1][i] != MatrixStates.PRIZE))
            defaultList.remove(Movements.TOP)
        if(j+1 == 7 || (boardState[j+1][i] != MatrixStates.UNVISITED && boardState[j+1][i] != MatrixStates.PRIZE))
            defaultList.remove(Movements.DOWN)

        if (defaultList.isNotEmpty()) defaultList.shuffle()

        return if(defaultList.isEmpty()) Movements.NO_MOVEMENT else defaultList[0]
    }

    private fun placePrize(){
        while(!prizeSet) {
            val rndsX = (0..6).random()
            val rndsY = (0..6).random()
            if ((rndsX != 0 || rndsY != 0) && (rndsX != 6 || rndsY != 6)) {
                boardState[rndsX][rndsY] = MatrixStates.PRIZE
                prizeSet = true
            }
        }
    }

    private fun placeRobots(){
        boardState[0][0] = MatrixStates.R1L
        boardState[6][6] = MatrixStates.R2L
    }

    fun setDefaultBoard(){
        boardState = Array(7) { Array(7) { MatrixStates.UNVISITED } }
        prizeSet = false
        r1Wins = false
        r2Wins = false
        placePrize()
        placeRobots()
        _gameStateFlow.update {
            it.copy(
                gameState = boardState, positionR1 =
                Pair(
                    0,
                    0
                ),
                positionR2 = Pair(6, 6),
                r1Stuck = false, r2Stuck = false, r1Wins = false, r2Wins = false, movementsLeft = 48,
                score = score
            )
        }
        startGame()
    }

}

data class UiState(
    val gameState: Array<Array<MatrixStates>>,
    val positionR1: Pair<Int, Int> = Pair(0, 0),
    val positionR2: Pair<Int, Int> = Pair(0, 0),
    val movementsLeft: Int = 48,
    val r1Stuck: Boolean = false,
    val r2Stuck: Boolean = false,
    val r1Wins: Boolean = false,
    val r2Wins: Boolean = false,
    val score: Pair<Int, Int> = Pair(0, 0),
    val boardSize: Int = 7
)
