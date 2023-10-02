package com.example.robotsgame.ui.theme

import com.example.robotsgame.ui.theme.enums.MatrixStates
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.Assertions.*

class MainViewModelTest {

    private lateinit var viewModel: MainViewModel
    private val testDispatcher = TestCoroutineDispatcher()


    @Before
    fun setUp() {
        Dispatchers.setMain(Dispatchers.Unconfined)
        viewModel = MainViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()
    }

    @Test
    fun `test setDefaultBoard`() = runBlocking {
        viewModel.setDefaultBoard()

        val gameState = viewModel.gameStateFlow.first()

        assertEquals(MatrixStates.R1L, gameState.gameState[0][0])
        assertEquals(MatrixStates.R2L, gameState.gameState[6][6])
        var prizeFound = false
        for (i in 0 until 7) {
            for (j in 0 until 7) {
                if (gameState.gameState[i][j] == MatrixStates.PRIZE) {
                    prizeFound = true
                    assert(!(i == 0 && j == 0))
                    assert(!(i == 6 && j == 6))
                }
            }
        }
        assert(prizeFound)
    }

}