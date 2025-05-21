package com.example.profile

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.collection.mutableFloatListOf
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.profile.ui.theme.ProfileTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Let content draw behind status and nav bars
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Hide the bottom navigation bar (black bar)
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.hide(WindowInsetsCompat.Type.navigationBars())
        insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        // Set dark background color to status bar here (for pre-compose fallback)
        window.statusBarColor = android.graphics.Color.parseColor("#161616") // dark gray/black

        setContent {
            ProfileTheme {
                val systemUiController = rememberSystemUiController()

                SideEffect {
                    // Dark background, white icons
                    systemUiController.setStatusBarColor(
                        color = Color(0xFF161616),
                        darkIcons = false // false = white icons
                    )
                }

                val navController = rememberNavController()
                var showSplashScreen by remember { mutableStateOf(true) }
                val splashAlpha = remember { Animatable(1f) }
                val profileAlpha = remember { Animatable(0f) }

                // Animation timing
                LaunchedEffect(Unit) {
                    // Wait for the splash screen's internal animation (lines falling and fading)
                    delay(3000) // Total duration of splash screen animation (lines fade from 2000ms to 3000ms)

                    // Fade out splash screen
                    splashAlpha.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(durationMillis = 1000, easing = LinearEasing)
                    )
                    showSplashScreen = false

                    // Immediately start fading in the profile screen
                    profileAlpha.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(durationMillis = 1000, easing = LinearEasing)
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black) // Ensure the background is always black
                ) {
                    if (showSplashScreen) {
                        SplashScreen(
                            modifier = Modifier.alpha(splashAlpha.value),
                            onTimeout = { /* No-op, handled by LaunchedEffect */ }
                        )
                    }

                    NavHost(navController = navController, startDestination = "profile") {
                        composable("profile") {
                            ProfileScreen(
                                modifier = Modifier.alpha(profileAlpha.value)
                            )
                        }
                    }
                }
            }
        }
    }
}

// Data class to hold variable data for profile screen
data class UserData(
    val creditScore: Int = 0,
    val lifetimeCashback: Int = 0,
    val bankBalance: String = "check",
    val cashbackBalance: Int = 0,
    val coins: Int = 0,
    val referralAmount: String = "refer and earn",
    val profileImageUrl: String? = null,
    val name: String = "Unknown User",
    val memberSince: String = "Unknown Date"
)

// ViewModel for profile screen
class ProfileViewModel : ViewModel() {
    var userData by mutableStateOf(UserData())
        private set

    private val database = FirebaseDatabase.getInstance().getReference("data")

    init {
        fetchData()
    }

    private fun fetchData() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val data = snapshot.getValue(UserData::class.java)
                if (data != null) {
                    Log.d("ProfileViewModel", "Data fetched successfully: $data")
                    userData = data
                } else {
                    Log.e("ProfileViewModel", "Failed to deserialize data: $snapshot")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ProfileViewModel", "Database error: ${error.message}")
            }
        })
    }
}

// Splash Screen Composable
@Composable
fun SplashScreen(modifier: Modifier = Modifier, onTimeout: () -> Unit) {
    // Animation constants
    val i = 15
    val totalAnimationDuration = 2000L
    val fadeOutDuration = 1000L
    val fallDuration = 1000

    // Define 15 shades of blue for the rain effect
    val blueShades = List(i) { index ->
        val intensity = 0xFF - (index * 20)
        Color(0xFF1565C0 + (intensity shl 16))
    }

    // Animation states
    val lineOffsets = List(i) { remember { Animatable(-1000f) } }
    val lineAlphas = List(i) { remember { Animatable(0f) } }
    val lineXPositions = List(i) { Random.nextInt(-200, 200).toFloat() }
    val textScale = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }
    var shouldFadeOut by remember { mutableStateOf(false) }
    var shouldNavigate by remember { mutableStateOf(false) }

    val glowEffect = rememberInfiniteTransition()
    val glow by glowEffect.animateFloat(
        initialValue = 0.8f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Start animation timing
    LaunchedEffect(Unit) {
        delay(totalAnimationDuration - fadeOutDuration)
        shouldFadeOut = true
        delay(fadeOutDuration)
        shouldNavigate = true
    }

    // Animate lines and text
    LaunchedEffect(shouldFadeOut) {
        lineOffsets.forEachIndexed { index, offset ->
            launch {
                while (!shouldFadeOut) {
                    delay(Random.nextLong(0, 1500))
                    offset.snapTo(-1000f)
                    offset.animateTo(
                        targetValue = 1000f,
                        animationSpec = tween(
                            durationMillis = fallDuration,
                            easing = LinearEasing
                        )
                    )
                }
                lineAlphas[index].animateTo(
                    targetValue = 0f,
                    animationSpec = tween(1000, easing = LinearEasing)
                )
            }
            launch {
                while (!shouldFadeOut) {
                    delay(Random.nextLong(0, 1500))
                    lineAlphas[index].snapTo(0f)
                    lineAlphas[index].animateTo(
                        targetValue = 0.5f,
                        animationSpec = tween(600, easing = LinearEasing)
                    )
                }
            }
        }

        textScale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = LinearEasing)
        )
        textAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = LinearEasing)
        )
    }

    // Navigate when ready
    LaunchedEffect(shouldNavigate) {
        if (shouldNavigate) {
            onTimeout()
        }
    }

    // Define off-white color
    val offWhite = Color(0xFFCBCBCB)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        // Draw the vertical lines (rain effect)
        lineOffsets.forEachIndexed { index, offset ->
            Box(
                modifier = Modifier
                    .offset(x = lineXPositions[index].dp, y = offset.value.dp)
                    .width(10.dp)
                    .height(2000.dp)
                    .background(blueShades[index])
                    .alpha(lineAlphas[index].value)
            )
        }

        // Draw the glowing text with fade-out
        Text(
            text = "",
            color = offWhite,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .scale(textScale.value)
                .alpha(textAlpha.value * if (shouldFadeOut) 1f - (textAlpha.value * glow) else glow)
        )
    }
}

// Helper function to format numbers in Indian style (e.g., 1234567 -> 12,34,567)
fun formatIndianNumber(number: Int): String {
    val numberStr = number.toString()
    val length = numberStr.length
    return if (length <= 3) {
        numberStr
    } else {
        val lastThree = numberStr.substring(length - 3)
        val remaining = numberStr.substring(0, length - 3)
        val reversed = remaining.reversed()
        val grouped = reversed.chunked(2).joinToString(",").reversed()
        "$grouped,$lastThree"
    }
}

// Profile Screen Composable
@Composable
fun ProfileScreen(viewModel: ProfileViewModel = viewModel(), modifier: Modifier = Modifier) {
    val userData by viewModel::userData

    // Define colors for the two sections
    val topSectionBackground = Color(0xFF161616)
    val bottomSectionBackground = Color(0xFF0D0D0D)
    val borderColor = Color(0xFF666666)
    val valueTextColor = Color(0xFF6A6A6A)
    val offWhite = Color(0xFFCBCBCB)

    // Use Column with scrolling for the entire screen
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(topSectionBackground)
            .systemBarsPadding()
    ) {
        // Top Section (from top bar to CRED garage)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            // Top bar with proper padding
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.Absolute.Left,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.long_arrow_alt_left_solid_svgrepo_com),
                        contentDescription = "Back",
                        modifier = Modifier.size(34.dp),
                        contentScale = ContentScale.Crop,
                        colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(offWhite)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Profile",
                    color = offWhite,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))

                val lightGray = Color(0xFFCCCCCC)

                OutlinedButton(
                    modifier = Modifier.padding(3.dp),
                    onClick = { /* handle support click */ },
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, Color(0xFF666666)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = lightGray
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 9.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.chat_line_svgrepo_com__1_),
                            contentDescription = "chat",
                            modifier = Modifier.size(28.dp),
                            contentScale = ContentScale.Crop,
                            colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(offWhite)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Support", fontSize = 14.sp, color = offWhite)
                }
            }

            // Profile Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Profile Image (Far Left)
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color.Gray)
                ) {
                    AsyncImage(
                        model = userData.profileImageUrl ?: "https://via.placeholder.com/100",
                        contentDescription = "Profile Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Name and Member Since (Stacked Vertically, to the Right of the Image)
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = userData.name,
                        color = offWhite,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = userData.memberSince,
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }

                // Edit Icon (Far Right, with Circular Background and Border, non-clickable)
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Profile",
                    tint = offWhite,
                    modifier = Modifier
                        .size(38.dp)
                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                        .border(1.dp, borderColor, CircleShape)
                        .padding(4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // CRED Garage Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(bottomSectionBackground)
                    .border(1.dp, Color.DarkGray)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Car Icon (Left, Vertically Centered, with Circular Border)
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .border(1.dp, borderColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.car),
                        contentDescription = "Car Icon",
                        modifier = Modifier.size(24.dp),
                        contentScale = ContentScale.Crop,
                        colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(offWhite)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Text Section (Stacked Vertically)
                Column(
                    modifier = Modifier.weight(1f).padding(vertical = 13.5.dp)
                ) {
                    Text(
                        text = "get to know your vehicles, inside out",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                    Row(modifier = Modifier.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "CRED garage",
                            color = offWhite,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        // Long Right Arrow
                        Image(
                            modifier = Modifier.padding(start = 11.dp).size(34.dp),
                            painter = painterResource(id = R.drawable.long_arrow_alt_right_solid_svgrepo_com),
                            contentDescription = "arrow right",
                            contentScale = ContentScale.Crop,
                            colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(offWhite)
                        )
                    }
                }


            }

            Spacer(modifier = Modifier.height(16.dp))

            // Data Sections with Images and Dividers
            DataRow(
                label = "credit score",
                value = "${userData.creditScore}",
                extraLabel = " * REFRESH AVAILABLE",
                iconResId = R.drawable.credit_card_svgrepo_com
            )
            Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color.Gray.copy(alpha = 0.3f), thickness = 1.dp)
            DataRow(
                label = "lifetime cashback",
                value = "₹ ${userData.lifetimeCashback}",
                iconResId = R.drawable.rupee_sign_svgrepo_com
            )
            Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color.Gray.copy(alpha = 0.3f), thickness = 1.dp)
            DataRow(
                label = "bank balance",
                value = userData.bankBalance,
                iconResId = R.drawable.paypal_alt_svgrepo_com
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Bottom Section (from YOUR REWARDS & BENEFITS downwards)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(bottomSectionBackground)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "YOUR REWARDS & BENEFITS",
                color = Color.Gray,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Data Rows with Dividers
            DataRow(
                label = "cashback balance",
                value = "₹ ${userData.cashbackBalance}",
                stackValueBelowLabel = true
            )
            Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color.Gray.copy(alpha = 0.3f), thickness = 1.dp)
            DataRow(
                label = "coins",
                value = formatIndianNumber(userData.coins),
                stackValueBelowLabel = true
            )
            Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color.Gray.copy(alpha = 0.3f), thickness = 1.dp)
            DataRow(
                label = "win up to Rs 1000*",
                value = userData.referralAmount.toString(),
                extraLabel = null,
                stackValueBelowLabel = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "TRANSACTIONS & SUPPORT",
                color = Color.Gray,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(8.dp))

            DataRow("all transactions", "")

            // Add extra space at the bottom to ensure scrolling doesn't cut off content
            Spacer(modifier = Modifier.height(160.dp))
        }
    }
}

@Composable
fun DataRow(
    label: String,
    value: String,
    extraLabel: String? = null,
    iconResId: Int? = null,
    stackValueBelowLabel: Boolean = false
) {
    val borderColor = Color(0xFF666666)
    val valueTextColor = Color(0xFF6A6A6A)
    val offWhite = Color(0xFFCBCBCB)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Add image if provided, with circular border
        if (iconResId != null) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .border(1.dp, borderColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = iconResId),
                    contentDescription = label,
                    modifier = Modifier.size(21.dp),
                    contentScale = ContentScale.Crop,
                    colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(offWhite)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        if (stackValueBelowLabel) {
            // Stack label and value in a Column for specified fields
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = label,
                        color = offWhite,
                        fontSize = 16.sp,
                        modifier = Modifier.weight(1f)
                    )
                    if (extraLabel != null) {
                        Text(
                            text = extraLabel,
                            color = Color(0xFF14a003),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                }
                Text(
                    text = value,
                    color = valueTextColor,
                    fontSize = 16.sp
                )
            }
        } else {
            // Original layout for other fields
            Text(
                text = label,
                color = offWhite,
                fontSize = 13.5.sp,
                modifier = Modifier.weight(1f)
            )
            if (extraLabel != null) {
                Text(
                    text = extraLabel,
                    color = Color(0xFF139603),
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(end = 34.dp)
                )
            }
            Text(
                text = value,
                color = offWhite,
                fontSize = 16.sp,
                textAlign = TextAlign.End,
                modifier = Modifier.padding(end = 8.dp)
            )
        }

        Image(
            painter = painterResource(id = if (stackValueBelowLabel) R.drawable.greaterthan_svgrepo_com else R.drawable.long_arrow_alt_right_solid_svgrepo_com),
            contentDescription = "Navigate",
            modifier = Modifier.size(if (stackValueBelowLabel) 26.dp else 24.dp),
            contentScale = ContentScale.Crop,
            colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(borderColor)
        )
    }
}