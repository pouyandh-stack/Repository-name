package com.example.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.model.LocationPoint
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.RoyalBlue

/**
 * Interactive map component using Leaflet/Google Maps tiles via WebView.
 * Allows tapping anywhere on the map to drop a GPS red marker pin,
 * automatically extracting and saving latitude/longitude without manual typing.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun InteractiveLocationPickerMap(
    initialLatitude: Double = 35.6892,
    initialLongitude: Double = 51.3890,
    isReadOnly: Boolean = false,
    onLocationSelected: (Double, Double) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    var currentLat by remember { mutableDoubleStateOf(initialLatitude) }
    var currentLng by remember { mutableDoubleStateOf(initialLongitude) }

    val mapHtml = remember(initialLatitude, initialLongitude, isReadOnly) {
        """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8" />
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
            <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
            <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
            <style>
                html, body, #map {
                    height: 100%;
                    width: 100%;
                    margin: 0;
                    padding: 0;
                    background-color: #f0f2f5;
                }
                .leaflet-popup-content {
                    font-family: sans-serif;
                    direction: rtl;
                    text-align: right;
                }
            </style>
        </head>
        <body>
            <div id="map"></div>
            <script>
                var map = L.map('map', {
                    zoomControl: true,
                    attributionControl: false
                }).setView([$initialLatitude, $initialLongitude], 14);

                L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                    maxZoom: 19
                }).addTo(map);

                // Custom Red Pin Icon
                var redIcon = L.icon({
                    iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-red.png',
                    shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.7/images/marker-shadow.png',
                    iconSize: [25, 41],
                    iconAnchor: [12, 41],
                    popupAnchor: [1, -34],
                    shadowSize: [41, 41]
                });

                var marker = L.marker([$initialLatitude, $initialLongitude], {
                    icon: redIcon,
                    draggable: !${isReadOnly}
                }).addTo(map);

                marker.bindPopup("<b>موقعیت انتخاب شده آگهی</b>").openPopup();

                function updatePosition(lat, lng) {
                    marker.setLatLng([lat, lng]);
                    if (window.AndroidInterface) {
                        window.AndroidInterface.onCoordinatesPicked(lat, lng);
                    }
                }

                if (!${isReadOnly}) {
                    map.on('click', function(e) {
                        updatePosition(e.latlng.lat, e.latlng.lng);
                    });

                    marker.on('dragend', function(e) {
                        var pos = marker.getLatLng();
                        updatePosition(pos.lat, pos.lng);
                    });
                }
            </script>
        </body>
        </html>
        """.trimIndent()
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
    ) {
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.cacheMode = WebSettings.LOAD_DEFAULT
                    webViewClient = WebViewClient()

                    addJavascriptInterface(object {
                        @JavascriptInterface
                        fun onCoordinatesPicked(lat: Double, lng: Double) {
                            currentLat = lat
                            currentLng = lng
                            onLocationSelected(lat, lng)
                        }
                    }, "AndroidInterface")

                    loadDataWithBaseURL(
                        "https://maps.google.com",
                        mapHtml,
                        "text/html",
                        "UTF-8",
                        null
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Overlay Guide Badge
        Surface(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp),
            shape = RoundedCornerShape(8.dp),
            color = Color.White.copy(alpha = 0.92f),
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    Icons.Filled.LocationOn,
                    contentDescription = null,
                    tint = Color.Red,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = if (isReadOnly) "موقعیت روی نقشه" else "برای انتخاب روی هر نقطه نقشه لمس کنید",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
            }
        }
    }
}

/**
 * Direct Google Maps navigation trigger with automatic destination coordinates.
 * Generates https://www.google.com/maps/dir/?api=1&destination=lat,lng
 */
fun openGoogleMapsDirections(context: Context, location: LocationPoint) {
    val uriStr = "https://www.google.com/maps/dir/?api=1&destination=${location.latitude},${location.longitude}"
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uriStr)).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(uriStr))
        context.startActivity(browserIntent)
    }
}
