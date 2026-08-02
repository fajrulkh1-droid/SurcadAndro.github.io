package com.example.surveycad.ui.cad

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.surveycad.model.ActiveTool
import com.example.surveycad.model.CadEntity
import com.example.surveycad.model.CadLayer
import com.example.surveycad.model.CadViewOptions
import com.example.surveycad.model.Point3D
import com.example.surveycad.model.SurveyPoint
import com.example.surveycad.ui.theme.AccentCyan
import com.example.surveycad.ui.theme.AccentCyanDim
import com.example.surveycad.ui.theme.AmberWarning
import com.example.surveycad.ui.theme.AxisXRed
import com.example.surveycad.ui.theme.AxisYBlue
import com.example.surveycad.ui.theme.BgCanvas
import com.example.surveycad.ui.theme.BgPanel
import com.example.surveycad.ui.theme.GridMinor
import com.example.surveycad.ui.theme.LineBorder
import com.example.surveycad.ui.theme.TextDim
import com.example.surveycad.ui.theme.TextFaint
import com.example.surveycad.ui.theme.TextPrimary
import com.example.surveycad.util.SurveyCalculator
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.log10
import kotlin.math.pow

@Composable
fun CadCanvas(
    points: List<SurveyPoint>,
    entities: List<CadEntity>,
    layers: Map<String, CadLayer>,
    selectedPointId: String?,
    selectedEntityId: String?,
    viewOptions: CadViewOptions,
    activeTool: ActiveTool?,
    toolPoints: List<Point3D>,
    onPointSelect: (String) -> Unit,
    onEntitySelect: (String) -> Unit,
    onClickWorldPoint: (Point3D) -> Unit,
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableDoubleStateOf(1.0) } // screen pixels per world meter
    var offsetX by remember { mutableDoubleStateOf(0.0) } // world Easting at center
    var offsetY by remember { mutableDoubleStateOf(0.0) } // world Northing at center

    var crosshairWorldPos by remember { mutableStateOf(Point3D(0.0, 0.0, 0.0)) }
    var hoveredSnapPoint by remember { mutableStateOf<SurveyPoint?>(null) }

    fun zoomExtents(widthPx: Float, heightPx: Float) {
        if (points.isEmpty()) {
            scale = 1.0; offsetX = 0.0; offsetY = 0.0
            return
        }
        val minE = points.minOf { it.easting }
        val maxE = points.maxOf { it.easting }
        val minN = points.minOf { it.northing }
        val maxN = points.maxOf { it.northing }

        val spanE = (maxE - minE).coerceAtLeast(1.0)
        val spanN = (maxN - minN).coerceAtLeast(1.0)

        val sX = (widthPx * 0.7) / spanE
        val sY = (heightPx * 0.7) / spanN
        scale = minOf(sX, sY).coerceIn(0.001, 500.0)
        offsetX = (minE + maxE) / 2.0
        offsetY = (minN + maxN) / 2.0
    }

    Box(modifier = modifier.background(BgCanvas)) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(points, entities) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(0.0001, 2000.0)
                        offsetX -= pan.x / scale
                        offsetY += pan.y / scale
                    }
                }
                .pointerInput(points, entities, activeTool) {
                    detectTapGestures(
                        onTap = { tapOffset ->
                            val canvasW = size.width.toFloat()
                            val canvasH = size.height.toFloat()

                            // Convert tap pixel to World coordinates
                            val worldE = (tapOffset.x - canvasW / 2f) / scale + offsetX
                            val worldN = (canvasH / 2f - tapOffset.y) / scale + offsetY

                            // Find nearest survey point within 28dp snap radius
                            val snapPx = 28f
                            var nearestPoint: SurveyPoint? = null
                            var minSnapDist = Float.MAX_VALUE

                            for (p in points) {
                                val sX = (p.easting - offsetX) * scale + canvasW / 2f
                                val sY = canvasH / 2f - (p.northing - offsetY) * scale
                                val dist = hypot(tapOffset.x - sX, tapOffset.y - sY).toFloat()
                                if (dist < snapPx && dist < minSnapDist) {
                                    minSnapDist = dist
                                    nearestPoint = p
                                }
                            }

                            if (nearestPoint != null) {
                                onPointSelect(nearestPoint.id)
                                onClickWorldPoint(Point3D(nearestPoint.easting, nearestPoint.northing, nearestPoint.elevation))
                            } else {
                                val worldPt = Point3D(worldE, worldN, 0.0)
                                onClickWorldPoint(worldPt)
                            }
                        }
                    )
                }
        ) {
            val canvasW = size.width
            val canvasH = size.height

            // Auto-initialize zoom on first load if default
            if (scale == 1.0 && offsetX == 0.0 && offsetY == 0.0 && points.isNotEmpty()) {
                zoomExtents(canvasW, canvasH)
            }

            fun worldToScreen(easting: Double, northing: Double): Offset {
                val sX = (easting - offsetX) * scale + canvasW / 2f
                val sY = canvasH / 2f - (northing - offsetY) * scale
                return Offset(sX.toFloat(), sY.toFloat())
            }

            // 1. Adaptive Grid Lines
            val worldPerPx = 1.0 / scale
            val targetGridPx = 80f * worldPerPx
            val mag = 10.0.pow(kotlin.math.floor(log10(targetGridPx)))
            val norm = targetGridPx / mag
            val step = when {
                norm < 2 -> 1.0 * mag
                norm < 5 -> 2.0 * mag
                else -> 5.0 * mag
            }

            val topLeftWorldE = (0f - canvasW / 2f) / scale + offsetX
            val botRightWorldE = (canvasW - canvasW / 2f) / scale + offsetX
            val topLeftWorldN = (canvasH / 2f - 0f) / scale + offsetY
            val botRightWorldN = (canvasH / 2f - canvasH) / scale + offsetY

            var startE = kotlin.math.floor(topLeftWorldE / step) * step
            while (startE <= botRightWorldE) {
                val sX = worldToScreen(startE, 0.0).x
                drawLine(GridMinor, Offset(sX, 0f), Offset(sX, canvasH), strokeWidth = 1f)
                startE += step
            }

            var startN = kotlin.math.floor(botRightWorldN / step) * step
            while (startN <= topLeftWorldN) {
                val sY = worldToScreen(0.0, startN).y
                drawLine(GridMinor, Offset(0f, sY), Offset(canvasW, sY), strokeWidth = 1f)
                startN += step
            }

            // 2. Axes Lines
            val zeroScreen = worldToScreen(0.0, 0.0)
            if (zeroScreen.x in 0f..canvasW) {
                drawLine(AxisYBlue, Offset(zeroScreen.x, 0f), Offset(zeroScreen.x, canvasH), strokeWidth = 1.8f)
            }
            if (zeroScreen.y in 0f..canvasH) {
                drawLine(AxisXRed, Offset(0f, zeroScreen.y), Offset(canvasW, zeroScreen.y), strokeWidth = 1.8f)
            }

            // 3. Surface TIN mesh rendering
            if (viewOptions.showTin && points.size >= 3) {
                val triangles = SurveyCalculator.computeTriangles(points)
                for (tri in triangles) {
                    val s1 = worldToScreen(tri.p1.easting, tri.p1.northing)
                    val s2 = worldToScreen(tri.p2.easting, tri.p2.northing)
                    val s3 = worldToScreen(tri.p3.easting, tri.p3.northing)

                    val path = Path().apply {
                        moveTo(s1.x, s1.y)
                        lineTo(s2.x, s2.y)
                        lineTo(s3.x, s3.y)
                        close()
                    }
                    drawPath(path, Color(0x3B00D9C0), style = Stroke(width = 1f))
                }
            }

            // 4. Connect survey points in sequence / polygon
            if (viewOptions.connectPoints && points.size >= 2) {
                val path = Path()
                points.forEachIndexed { i, p ->
                    val s = worldToScreen(p.easting, p.northing)
                    if (i == 0) path.moveTo(s.x, s.y) else path.lineTo(s.x, s.y)
                }
                if (viewOptions.closedPolygon && points.size >= 3) {
                    val firstS = worldToScreen(points.first().easting, points.first().northing)
                    path.lineTo(firstS.x, firstS.y)
                }
                drawPath(path, TextDim, style = Stroke(width = 2f))
            }

            // 5. CAD Entities
            for (ent in entities) {
                val layerConfig = layers[ent.layer]
                if (layerConfig?.isVisible == false) continue

                val isSelected = ent.id == selectedEntityId
                val entityColor = if (isSelected) AmberWarning else parseHexColor(layerConfig?.colorHex ?: "#00D9C0")
                val strokeW = if (isSelected) 3.5f else 2f

                when (ent) {
                    is CadEntity.Line -> {
                        val s1 = worldToScreen(ent.p1.easting, ent.p1.northing)
                        val s2 = worldToScreen(ent.p2.easting, ent.p2.northing)
                        drawLine(entityColor, s1, s2, strokeWidth = strokeW)
                    }
                    is CadEntity.Polyline -> {
                        if (ent.points.size >= 2) {
                            val path = Path()
                            ent.points.forEachIndexed { i, p ->
                                val s = worldToScreen(p.easting, p.northing)
                                if (i == 0) path.moveTo(s.x, s.y) else path.lineTo(s.x, s.y)
                            }
                            if (ent.isClosed) {
                                val s0 = worldToScreen(ent.points.first().easting, ent.points.first().northing)
                                path.lineTo(s0.x, s0.y)
                            }
                            drawPath(path, entityColor, style = Stroke(width = strokeW))
                        }
                    }
                    is CadEntity.Rectangle -> {
                        if (ent.points.size >= 4) {
                            val path = Path()
                            ent.points.forEachIndexed { i, p ->
                                val s = worldToScreen(p.easting, p.northing)
                                if (i == 0) path.moveTo(s.x, s.y) else path.lineTo(s.x, s.y)
                            }
                            path.close()
                            drawPath(path, entityColor, style = Stroke(width = strokeW))
                        }
                    }
                    is CadEntity.Circle -> {
                        val centerS = worldToScreen(ent.center.easting, ent.center.northing)
                        val radiusPx = (ent.radius * scale).toFloat()
                        drawCircle(entityColor, radius = radiusPx, center = centerS, style = Stroke(width = strokeW))
                    }
                    is CadEntity.DimensionLinear, is CadEntity.DimensionAligned -> {
                        val p1 = if (ent is CadEntity.DimensionLinear) ent.p1 else (ent as CadEntity.DimensionAligned).p1
                        val p2 = if (ent is CadEntity.DimensionLinear) ent.p2 else (ent as CadEntity.DimensionAligned).p2
                        val labelText = if (ent is CadEntity.DimensionLinear) ent.label else (ent as CadEntity.DimensionAligned).label

                        val s1 = worldToScreen(p1.easting, p1.northing)
                        val s2 = worldToScreen(p2.easting, p2.northing)

                        drawLine(entityColor, s1, s2, strokeWidth = 1.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f)))
                        drawCircle(entityColor, radius = 3f, center = s1)
                        drawCircle(entityColor, radius = 3f, center = s2)

                        val midS = Offset((s1.x + s2.x) / 2f, (s1.y + s2.y) / 2f)
                        drawContext.canvas.nativeCanvas.drawText(
                            labelText,
                            midS.x + 8f,
                            midS.y - 8f,
                            android.graphics.Paint().apply {
                                color = entityColor.toArgb()
                                textSize = 28f
                                isFakeBoldText = true
                            }
                        )
                    }
                    is CadEntity.DimensionAngular -> {
                        val vS = worldToScreen(ent.vertex.easting, ent.vertex.northing)
                        val s1 = worldToScreen(ent.p1.easting, ent.p1.northing)
                        val s2 = worldToScreen(ent.p2.easting, ent.p2.northing)

                        drawLine(entityColor, vS, s1, strokeWidth = 1.5f)
                        drawLine(entityColor, vS, s2, strokeWidth = 1.5f)

                        drawContext.canvas.nativeCanvas.drawText(
                            ent.label,
                            vS.x + 12f,
                            vS.y + 24f,
                            android.graphics.Paint().apply {
                                color = entityColor.toArgb()
                                textSize = 28f
                                isFakeBoldText = true
                            }
                        )
                    }
                }
            }

            // 6. Survey Points
            var zMin = Double.MAX_VALUE
            var zMax = Double.MIN_VALUE
            if (viewOptions.colorByElevation && points.isNotEmpty()) {
                points.forEach {
                    if (it.elevation < zMin) zMin = it.elevation
                    if (it.elevation > zMax) zMax = it.elevation
                }
            }

            points.forEachIndexed { idx, p ->
                val layerConfig = layers[p.layer]
                if (layerConfig?.isVisible != false) {
                    val s = worldToScreen(p.easting, p.northing)
                    if (s.x in -50f..(canvasW + 50f) && s.y in -50f..(canvasH + 50f)) {
                        val isSelected = p.id == selectedPointId
                        val pointColor = if (viewOptions.colorByElevation && zMax > zMin) {
                            elevationColor((p.elevation - zMin) / (zMax - zMin))
                        } else {
                            parseHexColor(layerConfig?.colorHex ?: "#00D9C0")
                        }

                        val radius = if (isSelected) 9f else 5.5f
                        drawCircle(if (isSelected) Color.White else pointColor, radius = radius, center = s)

                        if (isSelected) {
                            drawCircle(AmberWarning, radius = 14f, center = s, style = Stroke(width = 3f))
                        }

                        // Labels / Elevation text / Sequence
                        val labelParts = mutableListOf<String>()
                        if (viewOptions.showSeqNumber) labelParts.add("#${idx + 1}")
                        if (viewOptions.showLabels) labelParts.add(p.name)
                        if (viewOptions.showElevation) labelParts.add("Z${"%.2f".format(p.elevation)}")

                        if (labelParts.isNotEmpty()) {
                            drawContext.canvas.nativeCanvas.drawText(
                                labelParts.joinToString("  "),
                                s.x + 12f,
                                s.y - 8f,
                                android.graphics.Paint().apply {
                                    color = TextPrimary.toArgb()
                                    textSize = 26f
                                }
                            )
                        }
                    }
                }
            }

            // 7. Active Tool Measurement Preview
            if (toolPoints.size >= 2) {
                val toolColor = AmberWarning
                val path = Path()
                toolPoints.forEachIndexed { i, p ->
                    val s = worldToScreen(p.easting, p.northing)
                    if (i == 0) path.moveTo(s.x, s.y) else path.lineTo(s.x, s.y)
                }
                if (activeTool == ActiveTool.MEASURE_AREA && toolPoints.size >= 3) {
                    val firstS = worldToScreen(toolPoints.first().easting, toolPoints.first().northing)
                    path.lineTo(firstS.x, firstS.y)
                }
                drawPath(path, toolColor, style = Stroke(width = 2.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f))))
            }
        }

        // CAD Overlay HUD: Coordinate Readout & Zoom Controls
        Box(modifier = Modifier.fillMaxSize().padding(10.dp)) {
            // Top Left Coordinate Readout
            Surface(
                color = BgPanel.copy(alpha = 0.85f),
                shape = RoundedCornerShape(6.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, LineBorder)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("E: ", color = TextFaint, fontSize = 11.sp)
                    Text("%.3f".format(crosshairWorldPos.easting), color = AccentCyan, fontSize = 11.sp, modifier = Modifier.padding(end = 8.dp))

                    Text("N: ", color = TextFaint, fontSize = 11.sp)
                    Text("%.3f".format(crosshairWorldPos.northing), color = AccentCyan, fontSize = 11.sp, modifier = Modifier.padding(end = 8.dp))

                    Text("Z: ", color = TextFaint, fontSize = 11.sp)
                    Text(if (hoveredSnapPoint != null) "%.3f".format(hoveredSnapPoint!!.elevation) else "—", color = AccentCyan, fontSize = 11.sp)
                }
            }

            // Bottom Left Scale Indicator
            Surface(
                modifier = Modifier.align(Alignment.BottomStart),
                color = BgPanel.copy(alpha = 0.7f),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = "Scale 1 : ${if (scale > 0) kotlin.math.round((1.0 / scale) * 100) / 100.0 else "—"}",
                    color = TextDim,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            // Bottom Right Zoom Controls
            Column(
                modifier = Modifier.align(Alignment.BottomEnd)
            ) {
                IconButton(
                    onClick = { scale *= 1.25 },
                    modifier = Modifier
                        .size(36.dp)
                        .background(BgPanel.copy(alpha = 0.85f), RoundedCornerShape(6.dp))
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Zoom In", tint = AccentCyan)
                }

                IconButton(
                    onClick = { scale *= 0.8 },
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .size(36.dp)
                        .background(BgPanel.copy(alpha = 0.85f), RoundedCornerShape(6.dp))
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Zoom Out", tint = AccentCyan)
                }
            }
        }
    }
}

private fun parseHexColor(hex: String): Color {
    return try {
        val clean = hex.removePrefix("#")
        val colorInt = clean.toLong(16)
        if (clean.length == 6) Color(0xFF000000 or colorInt) else Color(colorInt)
    } catch (e: Exception) {
        AccentCyan
    }
}

private fun elevationColor(t: Double): Color {
    val clamped = t.coerceIn(0.0, 1.0)
    val r = (0.2 + 0.8 * clamped).toFloat()
    val g = (0.8 - 0.4 * clamped).toFloat()
    val b = (0.3 + 0.5 * (1.0 - clamped)).toFloat()
    return Color(r, g, b, 1f)
}
