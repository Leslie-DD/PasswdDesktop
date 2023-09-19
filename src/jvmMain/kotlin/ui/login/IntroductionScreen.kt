package ui.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import model.Res

@Composable
fun IntroductionBox() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AppSymbolBox(modifier = Modifier.wrapContentHeight())
        Text(
            modifier = Modifier.padding(16.dp),
            maxLines = 8,
            overflow = TextOverflow.Ellipsis,
            fontSize = 14.sp,
            text = "Glad you are here! Passwd is a password management which encrypt by AES algorithm both in client and " +
                    "server. It means it's safe even there's no HTTPS since the data transferred between client and server is " +
                    "ciphertext and the server will always don't know your plaintext. But DO REMEMBER that WRITE DOWN your " +
                    "SECRET KEY cause if you forget that, passwords you stored will decrypted incorrectly!"
        )
    }
}

@Composable
fun AppSymbolBox(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        Image(
            painter = painterResource(Res.Drawable.APP_ICON_ROUND_CORNER),
            contentDescription = null,
            modifier = Modifier.size(45.dp)
        )
        Spacer(modifier = Modifier.height(30.dp))
        Text(
            text = "Passwd",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(text = "Compose for Multiplatform", fontSize = 12.sp)
    }
}