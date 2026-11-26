
package com.example.fitmanager

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity

class MapasAcademias : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mapa_academias)

        val mapa = findViewById<WebView>(R.id.mapa)

        mapa.webViewClient = WebViewClient()
        mapa.settings.javaScriptEnabled = true
        mapa.settings.domStorageEnabled = true

        val html = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta name="viewport" content="width=device-width, initial-scale=1.0">

                <link rel="stylesheet"
                href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css"/>

                <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>

                <style>
                    body {
                        margin: 0;
                        padding: 0;
                    }

                    #mapa {
                        height: 100vh;
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
                var consulta = `
                    [out:json][timeout:25];
                    (
                        node["leisure"="fitness_centre"](around:3000,-7.15915,-34.85442);
                        way["leisure"="fitness_centre"](around:3000,-7.15915,-34.85442);
                        node["amenity"="gym"](around:3000,-7.15915,-34.85442);
                        way["amenity"="gym"](around:3000,-7.15915,-34.85442);
                    );
                    out center tags;
                `;

                fetch(
                    'https://overpass-api.de/api/interpreter?data='
                    + encodeURIComponent(consulta)
                )
                .then(resposta => resposta.json())
                .then(dados => {

                    dados.elements.forEach(local => {

                        var lat = local.lat || local.center?.lat;
                        var lon = local.lon || local.center?.lon;

                        if (lat && lon) {

                            var nome = local.tags.name || 'Academia';

                            L.marker([lat, lon])
                                .addTo(mapa)
                                .bindPopup('<b>' + nome + '</b>');
                        }

                    });

                })
                .catch(erro => {
                    console.log("Erro ao buscar academias:", erro);
                });

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
