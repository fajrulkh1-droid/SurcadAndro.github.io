package com.example.surveycad.ui.sidebar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.surveycad.model.ActiveTool
import com.example.surveycad.model.CadCommand
import com.example.surveycad.model.CadEntity
import com.example.surveycad.model.CrsConfig
import com.example.surveycad.model.CrsType
import com.example.surveycad.model.Point3D
import com.example.surveycad.model.SurveyPoint
import com.example.surveycad.ui.theme.AccentCyan
import com.example.surveycad.ui.theme.AccentCyanDim
import com.example.surveycad.ui.theme.AmberWarning
import com.example.surveycad.ui.theme.BgApp
import com.example.surveycad.ui.theme.BgCanvas
import com.example.surveycad.ui.theme.BgPanel
import com.example.surveycad.ui.theme.BgPanel2
import com.example.surveycad.ui.theme.DangerRed
import com.example.surveycad.ui.theme.LineBorder
import com.example.surveycad.ui.theme.LineBorderSoft
import com.example.surveycad.ui.theme.OkGreen
import com.example.surveycad.ui.theme.TextDim
import com.example.surveycad.ui.theme.TextFaint
import com.example.surveycad.ui.theme.TextPrimary
import com.example.surveycad.util.Calc2Result
import com.example.surveycad.util.SurveyCalculator
import com.example.surveycad.util.TraverseResult
import com.example.surveycad.viewmodel.SurveyViewModel

@Composable
fun SidebarPanel(
    viewModel: SurveyViewModel,
    onOpenImport: () -> Unit = {},
    onOpenSample: () -> Unit = {},
    onOpenExport: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabTitles = listOf("Data", "Point Info", "Analysis", "Tools", "Layers")

    Surface(
        modifier = modifier,
        color = BgPanel,
        border = androidx.compose.foundation.BorderStroke(1.dp, LineBorder)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Tabs Bar
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = BgPanel,
                contentColor = AccentCyan,
                edgePadding = 0.dp,
                indicator = { tabPositions ->
                    if (selectedTab < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = AccentCyan
                        )
                    }
                }
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                title,
                                fontSize = 11.sp,
                                color = if (selectedTab == index) AccentCyan else TextDim,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            Box(modifier = Modifier.weight(1f).padding(10.dp)) {
                when (selectedTab) {
                    0 -> DataTab(viewModel, onOpenImport, onOpenSample, onOpenExport)
                    1 -> PointInfoTab(viewModel)
                    2 -> AnalysisTab(viewModel)
                    3 -> ToolsTab(viewModel)
                    4 -> LayersTab(viewModel)
                }
            }

            // Quick Export Footer
            Surface(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                color = AccentCyanDim,
                shape = RoundedCornerShape(6.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, AccentCyan)
            ) {
                Button(
                    onClick = onOpenExport,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = AccentCyan),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Calculate, contentDescription = "Export", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("EXPORT DATA (DXF / CSV)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        color = TextFaint,
        letterSpacing = 0.8.sp,
        modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
    )
}

@Composable
private fun KeyValueRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 11.sp, color = TextDim)
        Text(value, fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = TextPrimary)
    }
}

// ---------------------------------------------------------------------------
// TAB 1: DATA TAB
// ---------------------------------------------------------------------------
@Composable
private fun DataTab(
    viewModel: SurveyViewModel,
    onOpenImport: () -> Unit = {},
    onOpenSample: () -> Unit = {},
    onOpenExport: () -> Unit = {}
) {
    val crs by viewModel.crs.collectAsState()
    val points by viewModel.points.collectAsState()
    val viewOptions by viewModel.viewOptions.collectAsState()
    val selectedPointId by viewModel.selectedPointId.collectAsState()

    val scrollState = rememberScrollState()

    Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState)) {
        SectionHeader("FILE & EXPORT ACTIONS")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Button(
                onClick = onOpenExport,
                colors = ButtonDefaults.buttonColors(containerColor = AccentCyanDim, contentColor = AccentCyan),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text("EXPORT DXF", fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = onOpenImport,
                colors = ButtonDefaults.buttonColors(containerColor = BgPanel2, contentColor = TextPrimary),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text("IMPORT PENZ", fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = onOpenSample,
                colors = ButtonDefaults.buttonColors(containerColor = BgPanel2, contentColor = TextDim),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text("SAMPLES", fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        SectionHeader("PROJECT COORDINATE SYSTEM")

        var crsMenuExpanded by remember { mutableStateOf(false) }
        Box(modifier = Modifier.fillMaxWidth()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { crsMenuExpanded = true },
                color = BgCanvas,
                shape = RoundedCornerShape(5.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, LineBorder)
            ) {
                Text(
                    text = when (crs.type) {
                        CrsType.LOCAL -> "Local Cartesian System"
                        CrsType.WGS84_GEO -> "WGS84 Geographic (EPSG:4326)"
                        CrsType.UTM -> "UTM Zone ${crs.zone}${crs.hemisphere} (WGS84)"
                        CrsType.EPSG -> "Custom EPSG ${crs.epsgCode}"
                    },
                    color = TextPrimary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(10.dp)
                )
            }

            DropdownMenu(
                expanded = crsMenuExpanded,
                onDismissRequest = { crsMenuExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Local Cartesian System") },
                    onClick = {
                        viewModel.setCrs(CrsConfig(CrsType.LOCAL))
                        crsMenuExpanded = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("WGS84 Geographic (EPSG:4326)") },
                    onClick = {
                        viewModel.setCrs(CrsConfig(CrsType.WGS84_GEO))
                        crsMenuExpanded = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("UTM (Select Zone)") },
                    onClick = {
                        viewModel.setCrs(CrsConfig(CrsType.UTM, zone = 49, hemisphere = "S"))
                        crsMenuExpanded = false
                    }
                )
            }
        }

        if (crs.type == CrsType.UTM) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                var zoneText by remember { mutableStateOf(crs.zone.toString()) }
                OutlinedTextField(
                    value = zoneText,
                    onValueChange = {
                        zoneText = it
                        it.toIntOrNull()?.let { z ->
                            viewModel.setCrs(crs.copy(zone = z.coerceIn(1, 60)))
                        }
                    },
                    label = { Text("UTM Zone (1-60)", fontSize = 10.sp) },
                    modifier = Modifier.weight(1f),
                    colors = textInputColors(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                var hemiMenuExpanded by remember { mutableStateOf(false) }
                Box(modifier = Modifier.weight(1f).padding(top = 8.dp)) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { hemiMenuExpanded = true },
                        color = BgCanvas,
                        shape = RoundedCornerShape(5.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, LineBorder)
                    ) {
                        Text(
                            text = if (crs.hemisphere == "S") "South (S)" else "North (N)",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = hemiMenuExpanded,
                        onDismissRequest = { hemiMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("South (S)") },
                            onClick = {
                                viewModel.setCrs(crs.copy(hemisphere = "S"))
                                hemiMenuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("North (N)") },
                            onClick = {
                                viewModel.setCrs(crs.copy(hemisphere = "N"))
                                hemiMenuExpanded = false
                            }
                        )
                    }
                }
            }
        }

        SectionHeader("SUMMARY & PLOT OPTIONS")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            StatCard(title = "Total Points", value = "${points.size}", modifier = Modifier.weight(1f))
            StatCard(title = "CRS Status", value = if (crs.type == CrsType.LOCAL) "Local" else "Valid", modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(8.dp))
        CheckboxRow(label = "Show Point Labels", checked = viewOptions.showLabels) {
            viewModel.updateViewOptions { opt -> opt.copy(showLabels = it) }
        }
        CheckboxRow(label = "Show Point Elevation", checked = viewOptions.showElevation) {
            viewModel.updateViewOptions { opt -> opt.copy(showElevation = it) }
        }
        CheckboxRow(label = "Show Sequence Numbers", checked = viewOptions.showSeqNumber) {
            viewModel.updateViewOptions { opt -> opt.copy(showSeqNumber = it) }
        }
        CheckboxRow(label = "Connect Points (Survey Order)", checked = viewOptions.connectPoints) {
            viewModel.updateViewOptions { opt -> opt.copy(connectPoints = it) }
        }
        CheckboxRow(label = "Treat as Closed Polygon", checked = viewOptions.closedPolygon) {
            viewModel.updateViewOptions { opt -> opt.copy(closedPolygon = it) }
        }
        CheckboxRow(label = "Color Points by Elevation", checked = viewOptions.colorByElevation) {
            viewModel.updateViewOptions { opt -> opt.copy(colorByElevation = it) }
        }

        SectionHeader("COORDINATE TABLE")
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            color = BgCanvas,
            shape = RoundedCornerShape(6.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, LineBorder)
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BgPanel2)
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Text("Point", color = TextDim, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Text("Easting", color = TextDim, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.2f))
                    Text("Northing", color = TextDim, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.2f))
                    Text("Elev", color = TextDim, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                }

                LazyColumn {
                    items(points) { p ->
                        val isSel = p.id == selectedPointId
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (isSel) AccentCyanDim else Color.Transparent)
                                .clickable { viewModel.selectPoint(p.id) }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(p.name, color = if (isSel) AccentCyan else TextPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.weight(1f))
                            Text("%.3f".format(p.easting), color = if (isSel) AccentCyan else TextPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.weight(1.2f))
                            Text("%.3f".format(p.northing), color = if (isSel) AccentCyan else TextPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.weight(1.2f))
                            Text("%.3f".format(p.elevation), color = if (isSel) AccentCyan else TextPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// TAB 2: POINT INFO & CALC TAB
// ---------------------------------------------------------------------------
@Composable
private fun PointInfoTab(viewModel: SurveyViewModel) {
    val points by viewModel.points.collectAsState()
    val selectedPointId by viewModel.selectedPointId.collectAsState()
    val selectedEntityId by viewModel.selectedEntityId.collectAsState()
    val entities by viewModel.entities.collectAsState()
    val layers by viewModel.layers.collectAsState()

    val selectedPoint = points.find { it.id == selectedPointId }
    val selectedEntity = entities.find { it.id == selectedEntityId }

    val scrollState = rememberScrollState()

    Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState)) {
        SectionHeader("SELECTED OBJECT")

        if (selectedPoint != null) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = BgCanvas,
                shape = RoundedCornerShape(6.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, LineBorderSoft)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(selectedPoint.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AccentCyan)

                    var ptName by remember(selectedPoint.id) { mutableStateOf(selectedPoint.name) }
                    var ptE by remember(selectedPoint.id) { mutableStateOf(selectedPoint.easting.toString()) }
                    var ptN by remember(selectedPoint.id) { mutableStateOf(selectedPoint.northing.toString()) }
                    var ptZ by remember(selectedPoint.id) { mutableStateOf(selectedPoint.elevation.toString()) }

                    OutlinedTextField(
                        value = ptName,
                        onValueChange = { ptName = it },
                        label = { Text("Point Name", fontSize = 10.sp) },
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        colors = textInputColors()
                    )
                    OutlinedTextField(
                        value = ptE,
                        onValueChange = { ptE = it },
                        label = { Text("Easting (m)", fontSize = 10.sp) },
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        colors = textInputColors()
                    )
                    OutlinedTextField(
                        value = ptN,
                        onValueChange = { ptN = it },
                        label = { Text("Northing (m)", fontSize = 10.sp) },
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        colors = textInputColors()
                    )
                    OutlinedTextField(
                        value = ptZ,
                        onValueChange = { ptZ = it },
                        label = { Text("Elevation (m)", fontSize = 10.sp) },
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        colors = textInputColors()
                    )

                    Button(
                        onClick = {
                            val newE = ptE.toDoubleOrNull() ?: selectedPoint.easting
                            val newN = ptN.toDoubleOrNull() ?: selectedPoint.northing
                            val newZ = ptZ.toDoubleOrNull() ?: selectedPoint.elevation
                            viewModel.updatePoint(selectedPoint.copy(name = ptName, easting = newE, northing = newN, elevation = newZ))
                        },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentCyanDim, contentColor = AccentCyan)
                    ) {
                        Text("Save Changes", fontSize = 11.sp)
                    }
                }
            }
        } else if (selectedEntity != null) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = BgCanvas,
                shape = RoundedCornerShape(6.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, LineBorderSoft)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(selectedEntity::class.simpleName?.uppercase() ?: "ENTITY", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AmberWarning)
                    Spacer(modifier = Modifier.height(4.dp))
                    KeyValueRow("Layer", selectedEntity.layer)

                    Button(
                        onClick = { viewModel.deleteSelectedEntity() },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DangerRed.copy(alpha = 0.2f), contentColor = DangerRed)
                    ) {
                        Text("Delete Entity", fontSize = 11.sp)
                    }
                }
            }
        } else {
            Text("Click a point or line on the CAD canvas or table to inspect properties.", color = TextFaint, fontSize = 11.sp)
        }

        SectionHeader("CALCULATE BETWEEN TWO POINTS")
        var fromPointId by remember { mutableStateOf(points.firstOrNull()?.id ?: "") }
        var toPointId by remember { mutableStateOf(points.getOrNull(1)?.id ?: "") }
        var calcResult by remember { mutableStateOf<Calc2Result?>(null) }

        if (points.isNotEmpty()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                // From Point Dropdown
                var fromExpanded by remember { mutableStateOf(false) }
                val fromPoint = points.find { it.id == fromPointId } ?: points.first()

                Box(modifier = Modifier.weight(1f)) {
                    Surface(
                        modifier = Modifier.fillMaxWidth().clickable { fromExpanded = true },
                        color = BgCanvas,
                        shape = RoundedCornerShape(5.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, LineBorder)
                    ) {
                        Text("From: ${fromPoint.name}", color = TextPrimary, fontSize = 11.sp, modifier = Modifier.padding(8.dp))
                    }
                    DropdownMenu(expanded = fromExpanded, onDismissRequest = { fromExpanded = false }) {
                        points.forEach { p ->
                            DropdownMenuItem(text = { Text(p.name) }, onClick = { fromPointId = p.id; fromExpanded = false })
                        }
                    }
                }

                // To Point Dropdown
                var toExpanded by remember { mutableStateOf(false) }
                val toPoint = points.find { it.id == toPointId } ?: points.getOrNull(1) ?: points.first()

                Box(modifier = Modifier.weight(1f)) {
                    Surface(
                        modifier = Modifier.fillMaxWidth().clickable { toExpanded = true },
                        color = BgCanvas,
                        shape = RoundedCornerShape(5.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, LineBorder)
                    ) {
                        Text("To: ${toPoint.name}", color = TextPrimary, fontSize = 11.sp, modifier = Modifier.padding(8.dp))
                    }
                    DropdownMenu(expanded = toExpanded, onDismissRequest = { toExpanded = false }) {
                        points.forEach { p ->
                            DropdownMenuItem(text = { Text(p.name) }, onClick = { toPointId = p.id; toExpanded = false })
                        }
                    }
                }
            }

            Button(
                onClick = {
                    val pA = points.find { it.id == fromPointId }
                    val pB = points.find { it.id == toPointId }
                    if (pA != null && pB != null) {
                        calcResult = SurveyCalculator.calcBetween(pA, pB)
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentCyanDim, contentColor = AccentCyan)
            ) {
                Text("Calculate Distance & Azimuth", fontSize = 11.sp)
            }

            calcResult?.let { res ->
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    color = BgCanvas,
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, LineBorderSoft)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        KeyValueRow("Horizontal Dist", "%.3f m".format(res.distance))
                        KeyValueRow("Slope Dist (3D)", "%.3f m".format(res.slopeDistance))
                        KeyValueRow("Azimuth", "%.4f°".format(res.azimuth))
                        KeyValueRow("Back Azimuth", "%.4f°".format(res.backAzimuth))
                        KeyValueRow("Bearing", res.bearing)
                        KeyValueRow("Elevation Diff (ΔZ)", "%.3f m".format(res.deltaZ))
                        KeyValueRow("ΔE / ΔN", "%.3f / %.3f".format(res.deltaE, res.deltaN))
                        KeyValueRow("Vertical Angle", "%.3f°".format(res.verticalAngle))
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// TAB 3: ANALYSIS TAB
// ---------------------------------------------------------------------------
@Composable
private fun AnalysisTab(viewModel: SurveyViewModel) {
    val points by viewModel.points.collectAsState()
    val viewOptions by viewModel.viewOptions.collectAsState()

    val scrollState = rememberScrollState()

    Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState)) {
        SectionHeader("ELEVATION & PATH STATS")

        if (points.isNotEmpty()) {
            val highest = points.maxByOrNull { it.elevation }
            val lowest = points.minByOrNull { it.elevation }
            val avgZ = points.map { it.elevation }.average()

            var totalDist = 0.0
            for (i in 1 until points.size) {
                totalDist += SurveyCalculator.calcBetween(points[i - 1], points[i]).distance
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                StatCard(title = "Highest (${highest?.name})", value = "%.2f m".format(highest?.elevation ?: 0.0), modifier = Modifier.weight(1f))
                StatCard(title = "Lowest (${lowest?.name})", value = "%.2f m".format(lowest?.elevation ?: 0.0), modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                StatCard(title = "Avg Elevation", value = "%.3f m".format(avgZ), modifier = Modifier.weight(1f))
                StatCard(title = "Total Path Length", value = "%.2f m".format(totalDist), modifier = Modifier.weight(1f))
            }

            SectionHeader("POLYGON ANALYSIS")
            if (viewOptions.connectPoints && viewOptions.closedPolygon && points.size >= 3) {
                val area = SurveyCalculator.shoelaceAreaPoints(points)
                val perim = totalDist + SurveyCalculator.calcBetween(points.last(), points.first()).distance

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    StatCard(title = "Polygon Area", value = "%.2f m²".format(area), modifier = Modifier.weight(1f))
                    StatCard(title = "Perimeter", value = "%.2f m".format(perim), modifier = Modifier.weight(1f))
                }
            } else {
                Text("Enable 'Connect points' & 'Closed polygon' in Data tab to calculate area/perimeter.", fontSize = 11.sp, color = TextFaint)
            }

            SectionHeader("SURFACE & CONTOUR OPTIONS")
            CheckboxRow(label = "Show TIN Triangulation Surface", checked = viewOptions.showTin) {
                viewModel.updateViewOptions { opt -> opt.copy(showTin = it) }
            }
            CheckboxRow(label = "Show Contour Lines", checked = viewOptions.showContours) {
                viewModel.updateViewOptions { opt -> opt.copy(showContours = it) }
            }
        } else {
            Text("No survey points loaded.", color = TextFaint, fontSize = 11.sp)
        }
    }
}

// ---------------------------------------------------------------------------
// TAB 4: TOOLS & CAD COMMANDS TAB
// ---------------------------------------------------------------------------
@Composable
private fun ToolsTab(viewModel: SurveyViewModel) {
    val activeTool by viewModel.activeTool.collectAsState()
    val activeCommand by viewModel.activeCommand.collectAsState()
    val commandHint by viewModel.commandHint.collectAsState()
    val points by viewModel.points.collectAsState()

    val scrollState = rememberScrollState()

    Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState)) {
        SectionHeader("CAD COMMANDS")
        Text("Click a command button, then tap points on the canvas.", fontSize = 10.5.sp, color = TextFaint)

        val commands = listOf(
            CadCommand.LINE, CadCommand.POLYLINE, CadCommand.CIRCLE, CadCommand.RECTANGLE,
            CadCommand.MOVE, CadCommand.COPY, CadCommand.ROTATE, CadCommand.TRIM,
            CadCommand.EXTEND, CadCommand.OFFSET, CadCommand.DELETE
        )

        Column(modifier = Modifier.fillMaxWidth().padding(top = 6.dp)) {
            commands.chunked(3).forEach { rowCmds ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    rowCmds.forEach { cmd ->
                        val isActive = activeCommand == cmd
                        Button(
                            onClick = { viewModel.activateCommand(cmd) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isActive) AccentCyanDim else BgPanel2,
                                contentColor = if (isActive) AccentCyan else TextPrimary
                            ),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(cmd.alias, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        if (activeCommand != null) {
            Surface(
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                color = BgCanvas,
                shape = RoundedCornerShape(4.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, AccentCyanDim)
            ) {
                Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(commandHint, color = AccentCyan, fontSize = 11.sp, modifier = Modifier.weight(1f))
                    Button(
                        onClick = { viewModel.cancelCommand() },
                        colors = ButtonDefaults.buttonColors(containerColor = DangerRed.copy(alpha = 0.2f), contentColor = DangerRed)
                    ) {
                        Text("Cancel", fontSize = 10.sp)
                    }
                }
            }
        }

        SectionHeader("ACTIVE MEASUREMENT TOOLS")
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Button(
                onClick = { viewModel.setActiveTool(ActiveTool.MEASURE_DISTANCE) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (activeTool == ActiveTool.MEASURE_DISTANCE) AccentCyanDim else BgPanel2,
                    contentColor = if (activeTool == ActiveTool.MEASURE_DISTANCE) AccentCyan else TextPrimary
                )
            ) {
                Text("Distance", fontSize = 10.sp)
            }
            Button(
                onClick = { viewModel.setActiveTool(ActiveTool.MEASURE_AREA) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (activeTool == ActiveTool.MEASURE_AREA) AccentCyanDim else BgPanel2,
                    contentColor = if (activeTool == ActiveTool.MEASURE_AREA) AccentCyan else TextPrimary
                )
            ) {
                Text("Area", fontSize = 10.sp)
            }
            Button(
                onClick = { viewModel.setActiveTool(ActiveTool.MEASURE_AZIMUTH) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (activeTool == ActiveTool.MEASURE_AZIMUTH) AccentCyanDim else BgPanel2,
                    contentColor = if (activeTool == ActiveTool.MEASURE_AZIMUTH) AccentCyan else TextPrimary
                )
            ) {
                Text("Azimuth", fontSize = 10.sp)
            }
        }

        SectionHeader("TRAVERSE MISCLOSE TOOL")
        var miscloseResult by remember { mutableStateOf<TraverseResult?>(null) }
        Button(
            onClick = { miscloseResult = SurveyCalculator.calcTraverseMisclose(points) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = BgPanel2, contentColor = TextPrimary)
        ) {
            Text("Calculate Traverse Misclose", fontSize = 11.sp)
        }

        miscloseResult?.let { res ->
            Surface(
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                color = BgCanvas,
                shape = RoundedCornerShape(6.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, LineBorderSoft)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    KeyValueRow("Start → End", "${res.startName} → ${res.endName}")
                    KeyValueRow("Misclose Error", "%.4f m".format(res.miscloseDistance))
                    KeyValueRow("ΔE / ΔN Error", "%.4f / %.4f".format(res.deltaE, res.deltaN))
                    KeyValueRow("Total Path Dist", "%.3f m".format(res.totalDistance))
                    KeyValueRow("Precision Ratio", res.precisionRatio)
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// TAB 5: LAYERS TAB
// ---------------------------------------------------------------------------
@Composable
private fun LayersTab(viewModel: SurveyViewModel) {
    val layers by viewModel.layers.collectAsState()
    val activeLayer by viewModel.activeLayer.collectAsState()

    var newLayerName by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState)) {
        SectionHeader("LAYER MANAGER")

        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = newLayerName,
                onValueChange = { newLayerName = it },
                label = { Text("New Layer Name", fontSize = 10.sp) },
                modifier = Modifier.weight(1f),
                colors = textInputColors()
            )
            Spacer(modifier = Modifier.width(6.dp))
            Button(
                onClick = {
                    if (newLayerName.isNotEmpty()) {
                        viewModel.addLayer(newLayerName, "#C9D3DE")
                        newLayerName = ""
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentCyanDim, contentColor = AccentCyan)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Layer")
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = BgCanvas,
            shape = RoundedCornerShape(6.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, LineBorder)
        ) {
            Column {
                layers.forEach { (name, layer) ->
                    val isActive = name == activeLayer
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (isActive) AccentCyanDim.copy(alpha = 0.3f) else Color.Transparent)
                            .clickable { viewModel.setActiveLayer(name) }
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Swatch indicator
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .background(parseHexColor(layer.colorHex), RoundedCornerShape(3.dp))
                                .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(3.dp))
                        )

                        Text(
                            text = name,
                            color = if (isActive) AccentCyan else TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.weight(1f).padding(start = 10.dp)
                        )

                        IconButton(
                            onClick = { viewModel.toggleLayerVisibility(name) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                if (layer.isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Visibility",
                                tint = if (layer.isVisible) TextDim else TextFaint
                            )
                        }

                        IconButton(
                            onClick = { viewModel.toggleLayerLock(name) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                if (layer.isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                contentDescription = "Lock",
                                tint = if (layer.isLocked) AmberWarning else TextDim
                            )
                        }

                        IconButton(
                            onClick = { viewModel.deleteLayer(name) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Layer", tint = DangerRed)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = BgCanvas,
        shape = RoundedCornerShape(6.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, LineBorderSoft)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AccentCyan, fontFamily = FontFamily.Monospace)
            Text(title, fontSize = 9.5.sp, color = TextFaint, modifier = Modifier.padding(top = 2.dp))
        }
    }
}

@Composable
private fun CheckboxRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(checkedColor = AccentCyan, checkmarkColor = BgCanvas)
        )
        Text(label, fontSize = 11.5.sp, color = TextPrimary, modifier = Modifier.padding(start = 4.dp))
    }
}

@Composable
private fun textInputColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = AccentCyan,
    unfocusedBorderColor = LineBorder,
    focusedLabelColor = AccentCyan,
    unfocusedLabelColor = TextDim,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary
)

private fun parseHexColor(hex: String): Color {
    return try {
        val clean = hex.removePrefix("#")
        val colorInt = clean.toLong(16)
        if (clean.length == 6) Color(0xFF000000 or colorInt) else Color(colorInt)
    } catch (e: Exception) {
        AccentCyan
    }
}
