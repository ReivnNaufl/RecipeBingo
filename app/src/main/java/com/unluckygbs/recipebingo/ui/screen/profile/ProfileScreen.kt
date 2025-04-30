package com.unluckygbs.recipebingo.ui.screen.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.firebase.firestore.FirebaseFirestore
import com.unluckygbs.recipebingo.viewmodel.auth.AuthState
import com.unluckygbs.recipebingo.viewmodel.auth.AuthViewModel

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel
) {
    val authState = authViewModel.authState.observeAsState()
    val context = LocalContext.current
    val userId = authViewModel.getCurrentUserUid()

    var displayName by remember { mutableStateOf("Loading...") }
    var email by remember { mutableStateOf("No Email") }
    var photoUrl by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(userId) {
        userId?.let {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(it)
                .get()
                .addOnSuccessListener { doc ->
                    displayName = doc.getString("username") ?: "No Name"
                    email = doc.getString("email") ?: "No Email"
                    photoUrl = doc.getString("profileImageBase64")
                        ?.takeIf { it.isNotBlank() && it != "null" }
                }
                .addOnFailureListener {
                    displayName = "No Name"
                    email = "Error"
                }
        }
    }

    // Tampilkan UI menggunakan displayName, email, dan photoUrl
    ProfileContent(
        modifier = modifier,
        displayName = displayName,
        email = email,
        photoUrl = photoUrl,
        onLogoutClick = { authViewModel.signout(context) }
    )
}

@Composable
fun ProfileContent(
    modifier: Modifier = Modifier,
    displayName: String,
    email: String,
    photoUrl: String?,
    onLogoutClick: () -> Unit
)
{
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF7F1FB))
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(4.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 24.dp, horizontal = 16.dp)
            ) {
                // Row untuk gambar + teks
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (photoUrl != null) {
                        AsyncImage(
                            model = photoUrl,
                            contentDescription = "Profile Picture",
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Default Profile Icon",
                            tint = Color.Gray,
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(Color.LightGray)
                                .padding(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp)) // Jarak antara foto dan teks

                    Column {
                        Text(text = displayName, fontSize = 18.sp, fontWeight = FontWeight.Bold)

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(text = email, fontSize = 14.sp, color = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Tombol di tengah
                Button(
                    onClick = { /* TODO: Navigate to Edit Profile */ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Edit Profile", color = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            Text("No Activities Here...", color = Color.DarkGray)
        }
    }
}
