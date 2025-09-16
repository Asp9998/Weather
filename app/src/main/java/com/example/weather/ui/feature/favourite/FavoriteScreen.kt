package com.example.weather.ui.feature.favourite

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.weather.R
import com.example.weather.data.local.db.entity.FavouriteEntity
import com.example.weather.ui.navigation.WeatherScreen
import com.example.weather.ui.feature.favourite.FavoriteViewModel
import com.example.weather.core.util.AppColor

@Composable
fun FavoriteScreen(favoriteViewModel: FavoriteViewModel = hiltViewModel(),
                   navController: NavController){

    val favorites = favoriteViewModel.favList.collectAsState(initial = emptyList())


    LazyColumn(modifier = Modifier) {
        items(items = favorites.value) {favoriteList ->
            FavoriteRow(favoriteList,
                favoriteViewModel,
                navController)
        }
    }
}

@SuppressLint("SetTextI18n")
@Composable
fun FavoriteRow(data: FavouriteEntity,
                favoriteViewModel: FavoriteViewModel,
                navController: NavController) {

    val context = LocalContext.current


   Surface(modifier = Modifier
       .padding(horizontal = 8.dp, vertical = 1.dp)
       .fillMaxWidth(),
       color = Color.Transparent
   ) {

       Row (verticalAlignment = Alignment.CenterVertically,
           horizontalArrangement = Arrangement.Absolute.SpaceBetween,
           modifier = Modifier.height(50.dp)
       ) {
           Column(verticalArrangement = Arrangement.Center,
               modifier = Modifier
                   .height(50.dp)
                   .fillMaxWidth(0.7f)
                   .clip(shape = RoundedCornerShape(25.dp))
                   .clickable {

                        navController.navigate("${WeatherScreen.MainScreen.base}?lat=${data.cityLat}&lon=${data.cityLon}&label=${data.cityLabel}") {
                            popUpTo(WeatherScreen.MainScreen.base) { inclusive = true }
                            launchSingleTop = true
                        }
                   }
           ) {
               Text(text = data.cityLabel,
                   color = AppColor.text,
                   modifier = Modifier.padding(15.dp)
               )
           }

           IconButton(onClick = {
               favoriteViewModel.delete(data.locationId)

               // custom Toast
               val inflater = LayoutInflater.from(context)
               val layout = inflater.inflate(R.layout.toast, null)
               val textView = layout.findViewById<TextView>(R.id.toast_text)
               textView.text = "${data.cityLabel} has been removed"

               val apply = Toast(context).apply {
                   duration = Toast.LENGTH_SHORT
                   view = layout
                   show()
               }
           }) {
               Icon(imageVector = Icons.Rounded.Delete,
                   contentDescription = "menu icon",
                   tint = AppColor.text)
           }

       }

   }

}