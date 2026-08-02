package com.example.surveycad.ui.map

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.surveycad.model.CrsConfig
import com.example.surveycad.model.SurveyPoint
import com.example.surveycad.ui.theme.AccentCyan
import com.example.surveycad.ui.theme.AmberWarning
import com.example.surveycad.ui.theme.BgCanvas
import com.example.surveycad.ui.theme.BgPanel
import com.example.surveycad.ui.theme.GridMinor
import com.example.surveycad.ui.theme.LineBorder
import com.example.surveycad.ui.theme.TextDim
import com.example.surveycad.ui.theme.TextFaint
import com.example.surveycad.ui.theme.TextPrimary
import com.example.surveycad.util.SurveyCalculator

@Composable
fun MapView(
    points: List<SurveyPoint>,
    crs: CrsConfig,
    selectedPointId: String?,
    onPointSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var mapZoom by remember { mutableDoubleStateOf(1000.0) }
    var centerLat by remember { mutableDoubleStateOf(-2.5) }
    var centerLon by remember { mutableDoubleStateOf(118.0) }

    // Auto center map on survey data centroid
    if (points.isNotEmpty() && centerLat == -2.5) {
        val latLons = points.map {
            SurveyCalculator.projectToLatLon(it.easting, it.northing, crs.zone, crs.hemisphere)
        }
        centerLat = latLons.map { it.first }.average()
        centerLon = latLons.map { it.second }.average()
    }

    Box(modifier = modifier.background(BgCanvas)) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(points, crs) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        mapZoom = (mapZoom * zoom).coerceIn(10.0, 50000.0)
                        centerLon -= pan.x / mapZoom
                        centerLat += pan.y / mapZoom
                    }
                }
        ) {
            val canvasW = size.width
            val canvasH = size.height

            fun latLonToScreen(lat: Double, lon: Double): Offset {
                val sX = (lon - centerLon) * mapZoom + canvasW / 2f
                val sY = canvasH / 2f - (lat - centerLat) * mapZoom
                return Offset(sX.toFloat(), sY.toFloat())
            }

            // Grid lines
            for (step in -10..10) {
                val gridLon = centerLon + step * (100.0 / mapZoom)
                val sX = ((gridLon - centerLon) * mapZoom + canvasW / 2f).toFloat()
                drawLine(GridMinor, Offset(sX, 0f), Offset(sX, canvasH), strokeWidth = 1f)
            }

            // Points
            for (p in points) {
                val latLon = SurveyCalculator.projectToLatLon(p.easting, p.northing, crs.zone, crs.hemisphere)
                val s = latLonToScreen(latLon.first, latLon.second)

                val isSel = p.id == selectedPointId
                val pointColor = if (isSel) AmberWarning else AccentCyan

                drawCircle(pointColor, radius = if (isSel) 8f else 5f, center = s)
                if (isSel) {
                    drawCircle(Color.White, radius = 12f, center = s, style = Stroke(width = 2.5f))
                }

                drawContext.canvas.nativeCanvas.drawText(
                    p.name,
                    s.x + 10f,
                    s.y - 6f,
                    android.graphics.Paint().apply {
                        color = TextPrimary.toArgb()
                        textSize = 24f
                    }
                )
            }
        }

        // Overlay Map Info HUD
        Surface(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(10.dp),
            color = BgPanel.copy(alpha = 0.85f),
            shape = RoundedCornerShape(6.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, LineBorder)
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Text("MAP VIEW (UTM Projection)", color = AccentCyan, fontSize = 11.sp)
                Text(
                    "Center Lat/Lon: ${"%.5f".format(centerLat)}, ${"%.5f".format(centerLon)}",
                    color = TextDim,
                    fontSize = 10.sp
                )
            }
        }
    }
}
