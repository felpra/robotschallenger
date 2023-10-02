package com.example.robotsgame.ui.theme.utils

import androidx.compose.ui.graphics.Color
import com.example.robotsgame.ui.theme.enums.MatrixStates

object Utils {
    fun extractColor(matrixStates: MatrixStates?): Color {
        return when(matrixStates){
            MatrixStates.ROBOT1 -> Color.Blue
            MatrixStates.ROBOT2 -> Color.Green
            MatrixStates.UNVISITED -> Color.White
            MatrixStates.PRIZE -> Color.Yellow
            MatrixStates.R1L -> Color.Cyan
            MatrixStates.R2L -> Color.DarkGray
            null -> Color.White
        }
    }
}