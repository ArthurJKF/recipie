package com.example.recipie

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.recipie.ui.theme.ReciPieTheme
import com.example.recipie.GeladeiraScreen
import com.example.recipie.FogaoScreen

//pra mostrar a lista só
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.LaunchedEffect
import com.example.recipie.data.local.database.RecipieDatabaseProvider
import com.example.recipie.data.local.entity.RecipeEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
	//define o db
	val database = RecipieDatabaseProvider.getDatabase(applicationContext)
        enableEdgeToEdge()
        setContent {
            ReciPieTheme {
                // Set up navigation controller
                val navController = rememberNavController()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        BottomNavigation(navController)
                    }
                ) { innerPadding ->
                    // Main content area with background color
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .background(Color(0xFF2B2B2B)), // Dark gray background
                    ) {
                        NavHost(
                            navController = navController,
                            startDestination = "main"
                        ) {
                            composable("left") { GeladeiraScreen() }
                            composable("main") { MainScreen(
				loadRecipes = {
					withContext(Dispatchers.IO){
	database.recipeDao().getFirst20Recipes()
					}
				}
			    ) }
                            composable("right") { FogaoScreen() }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BottomNavigation(navController: NavHostController) {
    var currentRoute by remember { mutableStateOf("main") }

    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(Color.Black)
    ) {
        // Left tab
        Column(
            modifier = Modifier
                .clickable {
                    currentRoute = "left"
                    navController.navigate("left") {
                        launchSingleTop = true
                        restoreState = true
                    }
                }
                .size(50.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "◉",
                color = if (currentRoute == "left") Color.White else Color.Gray
            )
            Text(
                text = "Geladeira",
                fontSize = 10.sp,
                color = if (currentRoute == "left") Color.White else Color.Gray
            )
        }

        // Main tab
        Column(
            modifier = Modifier
                .clickable {
                    currentRoute = "main"
                    navController.navigate("main") {
                        launchSingleTop = true
                        restoreState = true
                    }
                }
                .size(50.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "◉",
                color = if (currentRoute == "main") Color.White else Color.Gray
            )
            Text(
                text = "RECIPIE",
                fontSize = 10.sp,
                color = if (currentRoute == "main") Color.White else Color.Gray
            )
        }

        // Right tab
        Column(
            modifier = Modifier
                .clickable {
                    currentRoute = "right"
                    navController.navigate("right") {
                        launchSingleTop = true
                        restoreState = true
                    }
                }
                .size(50.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "◉",
                color = if (currentRoute == "right") Color.White else Color.Gray
            )
            Text(
                text = "Fogão",
                fontSize = 10.sp,
                color = if (currentRoute == "right") Color.White else Color.Gray
            )
        }
    }
}


@Composable
fun MainScreen(
    loadRecipes: suspend () -> List<RecipeEntity> = { emptyList() }
) {
    var recipes by remember { mutableStateOf<List<RecipeEntity>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            recipes = loadRecipes()
        } catch (e: Exception) {
            error = e.message ?: e.toString()
        } finally {
            loading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "RECIPIE",
            fontSize = 24.sp,
            color = Color.White,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Text(
            text = "aura",
            fontSize = 16.sp,
            color = Color.LightGray,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
	Text(
            text = "carregar os 20 da lista de receitas (35k)",
            fontSize = 16.sp,
            color = Color.LightGray,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )


        when {
            loading -> CircularProgressIndicator()

            error != null -> Text(
                text = "Database error: $error",
                color = Color.Red
            )

            recipes.isEmpty() -> Text(
                text = "No recipes found.",
                color = Color.LightGray
            )

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(recipes) { recipe ->
                    Text(
                        text = recipe.name.toString(),
                        color = Color.White,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    )
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun GeladeiraScreenPreview() {
    ReciPieTheme {
        GeladeiraScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    ReciPieTheme {
        MainScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun FogaoScreenPreview() {
    ReciPieTheme {
        FogaoScreen()
    }
}
