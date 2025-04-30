package com.unluckygbs.recipebingo.ui.screen.recipe

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.unluckygbs.recipebingo.R
import com.unluckygbs.recipebingo.data.dataclass.RecipeById
import com.unluckygbs.recipebingo.data.entity.RecipeEntity


@Composable
fun RecipeDetailScreen(
    onBackClick: () -> Unit = {},
    onSaveClick: () -> Unit = {},
    onEatClick: () -> Unit = {},
    recipeById: RecipeById?
) {
    val nutrients = recipeById?.nutrition?.nutrient
        ?.map { "${it.name}: ${it.amount} ${it.unit}" }

    if (recipeById == null) {
        // Tampilkan indikator loading atau teks error
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(bottom = 80.dp) // Menambahkan padding untuk memberi ruang bagi FAB
        ) {
            // Header with back button and recipe image
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .background(Color(0xFFE0E0E0))
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.align(Alignment.TopStart)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }

                    if (recipeById.image.isNotEmpty()) {
                        AsyncImage(
                            model = recipeById.image,
                            contentDescription = "Recipe Image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .align(Alignment.Center)
                        )
                    } else {
                        // Placeholder jika gambar tidak tersedia
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .align(Alignment.Center)
                                .background(Color.Gray, shape = RoundedCornerShape(16.dp))
                        )
                    }
                }
            }

            // Title
            item {
                Text(
                    text = recipeById.title,
                    style = TextStyle(textIndent = TextIndent(firstLine = 17.sp)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                )
            }

            item { Divider() }

            // Ingredients section
            item {
                SectionWithDots(title = "Ingredients", items = recipeById.extendedIngredients.map { it.original })
            }

            item { Divider() }

            // Steps section
            recipeById.analyzedInstruction.flatMap { it.steps }.map { it.step }.let { steps ->
                item {
                    SectionWithDots(title = "Steps", items = steps)
                }
            }

            item { Divider() }

            nutrients?.let {
                item {
                        SectionWithDots(title = "Nutrition", items = it)
                }
            }
        }

        // Bottom buttons tetap di bawah layar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            FloatingActionButton(
                onClick = onSaveClick,
                backgroundColor = Color(0xFF00C853)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Bookmark")
            }
            FloatingActionButton(
                onClick = onEatClick,
                backgroundColor = Color(0xFF00C853),
                shape = RoundedCornerShape(50)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically, // Vertikal centering untuk ikon dan teks
                    horizontalArrangement = Arrangement.Center, // Menjaga jarak antar elemen
                    modifier = Modifier.padding(horizontal = 8.dp) // Menambahkan sedikit padding agar teks tidak terlalu rapat
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp)) // Memberikan ruang antara ikon dan teks
                    Text("Eat")
                }
            }

        }
    }
}



@Composable
fun SectionWithDots(title: String, items: List<String>) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = title, style = MaterialTheme.typography.subtitle1)
        Spacer(modifier = Modifier.height(8.dp))
        items.forEach { item ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(Color(0xFF00C853), CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = item)
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}
