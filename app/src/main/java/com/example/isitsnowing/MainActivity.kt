package com.example.isitsnowing

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import coil.size.Size
import com.example.isitsnowing.ui.theme.IsItSnowingTheme
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

data class City(val name: String, val lat: Double, val long: Double)

val cities = listOf(
    City("Portland, OR", 45.5152, -122.6784),
    City("Vancouver, WA", 45.6257, -122.6762),
    City("LA, CA", 34.0522, -118.2437),
    City("Durham, NC", 35.9940, -78.8986),
    City("Las Vegas, NV", 36.1699, -115.1398),
    City("Daegu, South Korea", 35.8714, 128.6014),
    City("Lincoln, ME", 45.3508, -68.5077),
)

class MainActivity : ComponentActivity() {
    private var weather by mutableStateOf("")
    private var selectedCity by mutableStateOf(cities[0])

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sendRequest()

        setContent {
            IsItSnowingTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = if (isItSnowing(weather)) Color.Red else Color.Green
                ) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        CityDropdownMenu(cities = cities) { city ->
                            // Handle city selection
                            // You can access city.name, city.latitude, and city.longitude here
                            selectedCity = city
                            sendRequest()
                        }
                        if (isItSnowing(weather)) CommencePanic() else NotSnowing()
                    }
                }
            }
        }
    }

    private fun sendRequest() {
        val request = Request.Builder()
            .url("https://api.openweathermap.org/data/2.5/weather?lat=${selectedCity.lat}&lon=${selectedCity.long}&appid=APP_API_KEY_HERE")
            .build()
        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Handle the failure case
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    // Handle the response
                    val responseData = response.body?.string()
                    if (responseData != null) {
                        // Now you can parse the string as JSON
                        val json = JSONObject(responseData)
                        // println(json)

                        // To access a specific field, like 'weather', you would do something like:
                        val weatherArray = json.getJSONArray("weather")
                        // Check if the array is not empty
                        if (weatherArray.length() > 0) {
                            // Get the first object from the weather array
                            val firstWeatherObject = weatherArray.getJSONObject(0)

                            // Extract the 'main' property from this object
                            val main = firstWeatherObject.getString("main")
                            weather = main
                        }
                    }

                    // Do something with the response data
                } else {
                    // Handle the error
                }
            }
        })
    }
}

@Composable
fun CityDropdownMenu(cities: List<City>, onCitySelected: (City) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    var selectedCity by remember { mutableStateOf(cities.first()) }

    Column {
        Text(text = "Is it snowing in:", style = TextStyle(fontSize = 20.sp, textAlign = TextAlign.Center))
        Box(modifier = Modifier
            .fillMaxWidth()
            .wrapContentSize(Alignment.TopStart)
            .align(Alignment.CenterHorizontally)
            .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
            .background(Color.White)) {
            Text(
                text = selectedCity.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = { expanded = true })
                    .padding(16.dp),
                color = Color.Black
            )
            Icon(
                imageVector = Icons.Filled.ArrowDropDown,
                contentDescription = null,
                modifier = Modifier.align(Alignment.CenterEnd)
            )
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                cities.forEach { city ->
                    DropdownMenuItem(
                        onClick = {
                            selectedCity = city
                            expanded = false
                            onCitySelected(city)
                        },
                        text = { Text(city.name) }
                    )
                }
            }
        }
    }
}


@Composable
fun CommencePanic() {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "YES!", style = TextStyle(fontSize = 100.sp, textAlign = TextAlign.Center))
        Text(text = "COMMENCE PANIC!", style = TextStyle(fontSize = 18.sp, textAlign = TextAlign.Center))
        GifImage()
    }
}

@Composable
fun NotSnowing() {
    Text(text = "No.", style = TextStyle(fontSize = 100.sp, textAlign = TextAlign.Center))
}

@Composable
fun isItSnowing(weather: String): Boolean {
    return weather == "Snow";
}

@Composable
fun GifImage(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val imageLoader = ImageLoader.Builder(context)
        .components {
            if (Build.VERSION.SDK_INT >= 28) {
                add(ImageDecoderDecoder.Factory())
            } else {
                add(GifDecoder.Factory())
            }
        }
        .build()
    Image(
        painter = rememberAsyncImagePainter(
            ImageRequest.Builder(context).data(data = R.drawable.snowflail).apply(block = {
                size(Size.ORIGINAL)
            }).build(), imageLoader = imageLoader
        ),
        contentDescription = null,
        modifier = Modifier.fillMaxWidth(),
    )
}