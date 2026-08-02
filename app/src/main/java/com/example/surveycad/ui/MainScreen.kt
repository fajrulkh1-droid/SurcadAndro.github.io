package com.example.surveycad.ui

import android.widget.Toast
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Architecture
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MenuOpen
import androidx.compose.material.icons.filled.Publish
import androidx.compose.material.icons.filled.SquareFoot
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.surveycad.model.ActiveTool
import com.example.surveycad.model.CadCommand
import com.example.surveycad.model.ViewMode
import com.example.surveycad.ui.cad.CadCanvas
import com.example.surveycad.ui.dialogs.ExportDialog
import com.example.surveycad.ui.dialogs.ImportPenzDialog
import com.example.surveycad.ui.dialogs.SampleDataDialog
import com.example.surveycad.ui.map.MapView
import com.example.surveycad.ui.sidebar.SidebarPanel
import com.example.surveycad.ui.theme.AccentCyan
import com.example.surveycad.ui.theme.AccentCyanDim
import com.example.surveycad.ui.theme.AmberWarning
import com.example.surveycad.ui.theme.BgApp
import com.example.surveycad.ui.theme.BgCanvas
import com.example.surveycad.ui.theme.BgPanel
import com.example.surveycad.ui.theme.BgPanel2
import com.example.surveycad.ui.theme.LineBorder
import com.example.surveycad.ui.theme.TextDim
import com.example.surveycad.ui.theme.TextFaint
import com.example.surveycad.ui.theme.TextPrimary
import com.example.surveycad.viewmodel.SurveyViewModel

@Composable
fun MainScreen(
    viewModel: SurveyViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val toastMsg by viewModel.toastMessage.collectAsState()

    LaunchedEffect(toastMsg) {
        toastMsg?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    val viewMode by viewModel.viewMode.collectAsState()
    val points by viewModel.points.collectAsState()
    val entities by viewModel.entities.collectAsState()
    val layers by viewModel.layers.collectAsState()
    val crs by viewModel.crs.collectAsState()
    val selectedPointId by viewModel.selectedPointId.collectAsState()
    val selectedEntityId by viewModel.selectedEntityId.collectAsState()
    val viewOptions by viewModel.viewOptions.collectAsState()
    val activeTool by viewModel.activeTool.collectAsState()
    val toolPoints by viewModel.toolPoints.collectAsState()
    val activeLayer by viewModel.activeLayer.collectAsState()
    val activeCommand by viewModel.activeCommand.collectAsState()

    var showImportDialog by remember { mutableStateOf(false) }
    var showSampleDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var isSidebarVisible by remember { mutableStateOf(true) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = BgApp,
        topBar = {
            TopAppBar(
                viewMode = viewMode,
                onViewModeChange = { viewModel.setViewMode(it) },
                onOpenImport = { showImportDialog = true },
                onOpenSample = { showSampleDialog = true },
                onOpenExport = { showExportDialog = true },
                isSidebarVisible = isSidebarVisible,
                onToggleSidebar = { isSidebarVisible = !isSidebarVisible }
            )
        },
        bottomBar = {
            BottomStatusBar(
                activeLayer = activeLayer,
                pointCount = points.size,
                activeTool = activeTool,
                activeCommand = activeCommand
            )
        }
    ) { padding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Main Viewport (CAD Canvas / Map View)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                when (viewMode) {
                    ViewMode.CAD -> {
                        CadCanvas(
                            points = points,
                            entities = entities,
                            layers = layers,
                            selectedPointId = selectedPointId,
                            selectedEntityId = selectedEntityId,
                            viewOptions = viewOptions,
                            activeTool = activeTool,
                            toolPoints = toolPoints,
                            onPointSelect = { viewModel.selectPoint(it) },
                            onEntitySelect = { viewModel.selectEntity(it) },
                            onClickWorldPoint = { worldPt ->
                                if (activeTool != null) {
                                    viewModel.addToolPoint(worldPt)
                                } else if (activeCommand != null) {
                                    viewModel.handleCommandClickPoint(worldPt)
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    ViewMode.MAP -> {
                        MapView(
                            points = points,
                            crs = crs,
                            selectedPointId = selectedPointId,
                            onPointSelect = { viewModel.selectPoint(it) },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    ViewMode.SPLIT -> {
                        Row(modifier = Modifier.fillMaxSize()) {
                            CadCanvas(
                                points = points,
                                entities = entities,
                                layers = layers,
                                selectedPointId = selectedPointId,
                                selectedEntityId = selectedEntityId,
                                viewOptions = viewOptions,
                                activeTool = activeTool,
                                toolPoints = toolPoints,
                                onPointSelect = { viewModel.selectPoint(it) },
                                onEntitySelect = { viewModel.selectEntity(it) },
                                onClickWorldPoint = { worldPt ->
                                    if (activeTool != null) viewModel.addToolPoint(worldPt)
                                    else if (activeCommand != null) viewModel.handleCommandClickPoint(worldPt)
                                },
                                modifier = Modifier.weight(1f).fillMaxHeight()
                            )
                            MapView(
                                points = points,
                                crs = crs,
                                selectedPointId = selectedPointId,
                                onPointSelect = { viewModel.selectPoint(it) },
                                modifier = Modifier.weight(1f).fillMaxHeight()
                            )
                        }
                    }
                }

                // Floating Export Action Button on Canvas
                Surface(
                    onClick = { showExportDialog = true },
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp),
                    color = AccentCyan,
                    shape = RoundedCornerShape(20.dp),
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Download, contentDescription = "Export", tint = BgApp, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("EXPORT DXF / CSV", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BgApp)
                    }
                }
            }

            // Right Sidebar Drawer
            if (isSidebarVisible) {
                SidebarPanel(
                    viewModel = viewModel,
                    onOpenImport = { showImportDialog = true },
                    onOpenSample = { showSampleDialog = true },
                    onOpenExport = { showExportDialog = true },
                    modifier = Modifier
                        .width(320.dp)
                        .fillMaxHeight()
                )
            }
        }
    }

    if (showImportDialog) {
        ImportPenzDialog(viewModel = viewModel, onDismiss = { showImportDialog = false })
    }
    if (showSampleDialog) {
        SampleDataDialog(viewModel = viewModel, onDismiss = { showSampleDialog = false })
    }
    if (showExportDialog) {
        ExportDialog(viewModel = viewModel, onDismiss = { showExportDialog = false })
    }
}

@Composable
private fun TopAppBar(
    viewMode: ViewMode,
    onViewModeChange: (ViewMode) -> Unit,
    onOpenImport: () -> Unit,
    onOpenSample: () -> Unit,
    onOpenExport: () -> Unit,
    isSidebarVisible: Boolean,
    onToggleSidebar: () -> Unit
) {
    val topScrollState = rememberScrollState()
    Surface(
        color = BgPanel,
        border = androidx.compose.foundation.BorderStroke(1.dp, LineBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(topScrollState)
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App Logo Title
            Text("SurveyCAD", color = AccentCyan, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)

            Spacer(modifier = Modifier.width(12.dp))

            // Quick Actions with clear labels
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    onClick = onOpenImport,
                    color = BgPanel2,
                    shape = RoundedCornerShape(4.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, LineBorder)
                ) {
                    Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Publish, contentDescription = "Import", tint = TextPrimary, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("IMPORT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                }

                Surface(
                    onClick = onOpenSample,
                    color = BgPanel2,
                    shape = RoundedCornerShape(4.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, LineBorder)
                ) {
                    Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Folder, contentDescription = "Samples", tint = TextPrimary, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("SAMPLES", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                }

                Surface(
                    onClick = onOpenExport,
                    color = AccentCyanDim,
                    shape = RoundedCornerShape(4.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AccentCyan)
                ) {
                    Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Download, contentDescription = "Export", tint = AccentCyan, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("EXPORT DXF/CSV", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = AccentCyan)
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // View Mode Selectors
            Row(
                modifier = Modifier
                    .background(BgPanel2, RoundedCornerShape(4.dp))
                    .padding(2.dp)
            ) {
                ViewModeButton("CAD", isActive = viewMode == ViewMode.CAD) { onViewModeChange(ViewMode.CAD) }
                ViewModeButton("MAP", isActive = viewMode == ViewMode.MAP) { onViewModeChange(ViewMode.MAP) }
                ViewModeButton("SPLIT", isActive = viewMode == ViewMode.SPLIT) { onViewModeChange(ViewMode.SPLIT) }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Sidebar Toggle Button
            IconButton(onClick = onToggleSidebar, modifier = Modifier.size(32.dp)) {
                Icon(
                    if (isSidebarVisible) Icons.Default.MenuOpen else Icons.Default.Menu,
                    contentDescription = "Toggle Sidebar",
                    tint = AccentCyan
                )
            }
        }
    }
}

@Composable
private fun ViewModeButton(label: String, isActive: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .background(if (isActive) AccentCyanDim else Color.Transparent, RoundedCornerShape(3.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = if (isActive) AccentCyan else TextDim
        )
    }
}

@Composable
private fun BottomStatusBar(
    activeLayer: String,
    pointCount: Int,
    activeTool: ActiveTool?,
    activeCommand: CadCommand?
) {
    Surface(
        color = BgPanel,
        border = androidx.compose.foundation.BorderStroke(1.dp, LineBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Layer: $activeLayer", fontSize = 10.5.sp, color = AccentCyan, fontFamily = FontFamily.Monospace)
            Text("Points: $pointCount", fontSize = 10.5.sp, color = TextDim, fontFamily = FontFamily.Monospace)
            Text(
                text = when {
                    activeCommand != null -> "CMD: ${activeCommand.alias}"
                    activeTool != null -> "TOOL: $activeTool"
                    else -> "READY"
                },
                fontSize = 10.5.sp,
                color = if (activeCommand != null || activeTool != null) AmberWarning else TextFaint,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
