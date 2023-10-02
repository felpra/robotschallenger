package com.example.robotsgame

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.robotsgame.ui.theme.MainViewModel
import com.example.robotsgame.ui.theme.RobotsgameTheme
import com.example.robotsgame.ui.theme.UiState
import com.example.robotsgame.ui.theme.utils.Utils

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RobotsgameTheme {
                Surface(color = MaterialTheme.colors.background) {
                    val gameState = viewModel.gameStateFlow.collectAsState()

                    if(gameState.value.r1Wins){
                        Toast.makeText(LocalContext.current, "R1 Wins", Toast.LENGTH_LONG).show()
                        viewModel.setDefaultBoard()
                    }
                    if(gameState.value.r2Wins){
                        Toast.makeText(LocalContext.current, "R2 Wins", Toast.LENGTH_LONG).show()
                        viewModel.setDefaultBoard()
                    }
                    if(gameState.value.r1Stuck && gameState.value.r2Stuck){
                        Toast.makeText(LocalContext.current, "Robots stuck! Restarting...", Toast.LENGTH_LONG).show()
                        viewModel.setDefaultBoard()
                    }

                    Board(gameState.value)

                    Row(
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RectangleShape)
                                .background(Color.Blue)
                        ) {
                            Text(
                                text = gameState.value.score.first.toString(),
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                        Spacer(modifier = Modifier.size(70.dp))
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RectangleShape)
                                .background(Color.Green)
                        ) {
                            Text(
                                text = gameState.value.score.second.toString(),
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Board(gameState: UiState) {
    Column(
        Modifier
            .fillMaxWidth()
            .fillMaxHeight(80f),
        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        for (i in 0 until gameState.boardSize) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                repeat(gameState.boardSize) { j ->
                    CircleUnit(shape = CircleShape, state = gameState, i = i, j = j)
                }
            }
        }
    }
}

@Composable
fun CircleUnit(shape: Shape, state: UiState, i: Int, j: Int){
    Column(modifier = Modifier.wrapContentSize(Alignment.Center)) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(shape)
                    .background(
                        Utils.extractColor(
                            state.gameState[i][j]
                        )
                    )
            )
        }
}

