package com.example.ejemplo_retrofit2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    val urlBase="https://api.openrouteservice.org/";//v2/directions/driving-car?api_key=5b3ce3597851110001cf624892cba9b6b1a942faab1ef50861b1da04&start=8.681495,49.41461&end=8.687872,49.420318

    private lateinit var map:GoogleMap
    private lateinit var btnCalcular:Button
    private var start:String = ""
    private var end:String = ""

    var poly: Polyline? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnCalcular = findViewById<Button>(R.id.btnCalcularRoute)


        btnCalcular.setOnClickListener {
            start = ""
            end = ""
            poly?.remove()
            poly = null
            Toast.makeText(this, "Selecciona punto de origen y final", Toast.LENGTH_SHORT).show()

            if(::map.isInitialized){
                map.setOnMapClickListener {
                    if(start.isNotEmpty()){
                        start= "${it.longitude},${it.latitude}";
                    }else if(end.isNotEmpty()){
                        end= "${it.longitude},${it.latitude}";
                        createRoute()
                    }else{

                    }
                }
            }
        }

        val mapFragment = supportFragmentManager.findFragmentById(R.id.mimapa) as SupportMapFragment
        mapFragment.getMapAsync(this)

    }
    override fun onMapReady(map: GoogleMap) {
        this.map=map;
    }
    fun getRetrofit():Retrofit{
        return Retrofit.Builder()
            .baseUrl(urlBase)
            .addConverterFactory(GsonConverterFactory.create())
            .client(OkHttpClient.Builder().build())
            .build();
    }
    fun createRoute(){
        CoroutineScope(Dispatchers.IO).launch {
            val retrofit = getRetrofit().create(OpenRouteService::class.java);
            val call=retrofit.getRoute("5b3ce3597851110001cf624892cba9b6b1a942faab1ef50861b1da04",start,end);
            if(call.isSuccessful){
                drawRoute(call.body())
            }else{
                Log.i("MENSAJE","NOP")
            }
        }
    }
    private fun drawRoute(routeResponse: RouteResponse?) {
        val polyLineOptions = PolylineOptions()
        routeResponse?.features?.first()?.geometry?.coordinates?.forEach {
            polyLineOptions.add(LatLng(it[1], it[0]))
        }
        runOnUiThread {
            poly = map.addPolyline(polyLineOptions)
        }
    }


}