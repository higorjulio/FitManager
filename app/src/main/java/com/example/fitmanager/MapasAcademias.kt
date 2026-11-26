
package com.example.fitmanager

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity

class MapasAcademias : AppCompatActivity() {

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mapa_academias)

        val mapa = findViewById<WebView>(R.id.mapa)

        mapa.webViewClient = WebViewClient()
        mapa.webChromeClient = WebChromeClient()
        mapa.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            useWideViewPort = true
            loadWithOverviewMode = true
            mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
        }

        val html = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="utf-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">

                <link rel="stylesheet"
                href="https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/leaflet.min.css"/>

                <script src="https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/leaflet.min.js"></script>

                <style>
                    html, body {
                        margin: 0;
                        padding: 0;
                        height: 100%;
                        width: 100%;
                    }

                    #mapa {
                        height: 100%;
                        width: 100%;
                    }
                </style>
            </head>

            <body>

            <div id="mapa"></div>

            <script>

                var latitude = -7.15915;
                var longitude = -34.85442;

                var mapa = L.map('mapa').setView(
                    [latitude, longitude], 15
                );

                // Novo servidor de mapas
                L.tileLayer(
                    'https://server.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer/tile/{z}/{y}/{x}',
                    {
                        attribution: 'Tiles &copy; Esri',
                        maxZoom: 19
                    }
                ).addTo(mapa);

                // Marcador da faculdade
                L.marker([latitude, longitude])
                    .addTo(mapa)
                    .bindPopup('<b>UNIPÊ</b><br>Faculdade');

                // Busca academias próximas
                var consulta =
                    '[out:json][timeout:25];' +
                    '(' +
                    'node["leisure"="fitness_centre"](around:3000,-7.15915,-34.85442);' +
                    'way["leisure"="fitness_centre"](around:3000,-7.15915,-34.85442);' +
                    'node["amenity"="gym"](around:3000,-7.15915,-34.85442);' +
                    'way["amenity"="gym"](around:3000,-7.15915,-34.85442);' +
                    ');' +
                    'out center tags;';

                var xhr = new XMLHttpRequest();
                xhr.open(
                    'GET',
                    'https://overpass-api.de/api/interpreter?data=' + encodeURIComponent(consulta),
                    true
                );
                xhr.onreadystatechange = function() {
                    if (xhr.readyState === 4 && xhr.status === 200) {
                        try {
                            var dados = JSON.parse(xhr.responseText);
                            if (dados && dados.elements) {
                                for (var i = 0; i < dados.elements.length; i++) {
                                    var local = dados.elements[i];
                                    var lat = local.lat || (local.center ? local.center.lat : null);
                                    var lon = local.lon || (local.center ? local.center.lon : null);

                                    if (lat && lon) {
                                        var nome = (local.tags && local.tags.name) ? local.tags.name : 'Academia';
                                        L.marker([lat, lon])
                                            .addTo(mapa)
                                            .bindPopup('<b>' + nome + '</b>');
                                    }
                                }
                            }
                        } catch (erro) {
                            console.log("Erro ao buscar academias:", erro);
                        }
                    }
                };
                xhr.send();

            </script>

            </body>
            </html>
        """.trimIndent()

        mapa.loadDataWithBaseURL(
            "https://www.openstreetmap.org/",
            html,
            "text/html",
            "UTF-8",
            null
        )
    }
}
