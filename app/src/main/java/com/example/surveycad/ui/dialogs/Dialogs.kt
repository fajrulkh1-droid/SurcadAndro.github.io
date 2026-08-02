package com.example.surveycad.ui.dialogs

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.surveycad.ui.theme.AccentCyan
import com.example.surveycad.ui.theme.AccentCyanDim
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
fun ImportPenzDialog(
    viewModel: SurveyViewModel,
    onDismiss: () -> Unit
) {
    var rawText by remember {
        mutableStateOf(
            """
            P1 410250.500 9245100.200 45.250
            P2 410280.750 9245095.400 44.980
            P3 410310.200 9245110.600 46.120
            P4 410305.900 9245150.300 47.500
            P5 410265.400 9245160.800 46.850
            """.trimIndent()
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            color = BgPanel,
            shape = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, LineBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("IMPORT PENZ SURVEY DATA", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AccentCyan)
                Text("Format: Point, Easting, Northing, Elevation (comma, space, or tab delimited)", fontSize = 11.sp, color = TextFaint, modifier = Modifier.padding(top = 2.dp))

                OutlinedTextField(
                    value = rawText,
                    onValueChange = { rawText = it },
                    modifier = Modifier.fillMaxWidth().height(180.dp).padding(top = 10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentCyan,
                        unfocusedBorderColor = LineBorder,
                        focusedContainerColor = BgCanvas,
                        unfocusedContainerColor = BgCanvas,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                )

                Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.End) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = BgPanel2, contentColor = TextDim)
                    ) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            viewModel.loadPenzData(rawText)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentCyanDim, contentColor = AccentCyan)
                    ) {
                        Text("Import Points")
                    }
                }
            }
        }
    }
}

@Composable
fun SampleDataDialog(
    viewModel: SurveyViewModel,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            color = BgPanel,
            shape = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, LineBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("LOAD SAMPLE DATASETS", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AccentCyan)
                Spacer(modifier = Modifier.height(10.dp))

                SampleOptionCard("UTM Zone 49S (Jawa/Bali)", "7 points, UTM meters, Z = 44-47m") {
                    viewModel.loadSampleProject("utm49s")
                    onDismiss()
                }
                SampleOptionCard("UTM Zone 50S (Kalimantan)", "7 traverse points, UTM meters, Z = 12-15m") {
                    viewModel.loadSampleProject("utm50s")
                    onDismiss()
                }
                SampleOptionCard("WGS84 Geographic", "4 points, Lat/Lon decimal degrees") {
                    viewModel.loadSampleProject("wgs84")
                    onDismiss()
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BgPanel2, contentColor = TextDim)
                ) {
                    Text("Close")
                }
            }
        }
    }
}

@Composable
private fun SampleOptionCard(title: String, desc: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        color = BgCanvas,
        shape = RoundedCornerShape(6.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, LineBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(desc, fontSize = 10.5.sp, color = TextFaint, modifier = Modifier.padding(top = 2.dp))
        }
    }
}

@Composable
fun ExportDialog(
    viewModel: SurveyViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val points by viewModel.points.collectAsState()
    val entities by viewModel.entities.collectAsState()
    val projectName by viewModel.projectName.collectAsState()

    var selectedFormat by remember { mutableStateOf("dxf") }
    val formats = listOf("dxf", "csv", "txt", "geojson")

    val exportText = remember(selectedFormat, points, entities) {
        viewModel.exportContent(selectedFormat)
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            color = BgPanel,
            shape = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, LineBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("EXPORT SURVEY & CAD DATA", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AccentCyan)
                Text("Export current drawing & survey points to CAD/GIS exchange formats", fontSize = 11.sp, color = TextFaint, modifier = Modifier.padding(top = 2.dp))

                // Stats summary badges
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = BgCanvas,
                        shape = RoundedCornerShape(4.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, LineBorder),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("POINTS", fontSize = 9.sp, color = TextFaint, fontWeight = FontWeight.Bold)
                            Text("${points.size}", fontSize = 13.sp, color = AccentCyan, fontWeight = FontWeight.Bold)
                        }
                    }
                    Surface(
                        color = BgCanvas,
                        shape = RoundedCornerShape(4.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, LineBorder),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("CAD ENTITIES", fontSize = 9.sp, color = TextFaint, fontWeight = FontWeight.Bold)
                            Text("${entities.size}", fontSize = 13.sp, color = AccentCyan, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Format tabs
                Row(modifier = Modifier.fillMaxWidth().padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    formats.forEach { fmt ->
                        val isSel = selectedFormat == fmt
                        Button(
                            onClick = { selectedFormat = fmt },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSel) AccentCyanDim else BgPanel2,
                                contentColor = if (isSel) AccentCyan else TextPrimary
                            ),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(fmt.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Scrollable Preview Box
                Surface(
                    modifier = Modifier.fillMaxWidth().height(160.dp).padding(top = 10.dp),
                    color = BgCanvas,
                    shape = RoundedCornerShape(4.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, LineBorder)
                ) {
                    val scrollState = androidx.compose.foundation.rememberScrollState()
                    Text(
                        text = exportText,
                        color = TextPrimary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier
                            .padding(8.dp)
                            .verticalScroll(scrollState)
                    )
                }

                val fileName = "$projectName.$selectedFormat"

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = BgPanel2, contentColor = TextDim)
                    ) {
                        Text("Close")
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("SurveyCAD Export ($fileName)", exportText)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Copied $fileName to clipboard", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BgPanel2, contentColor = TextPrimary)
                        ) {
                            Text("Copy", fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                try {
                                    val sendIntent = android.content.Intent().apply {
                                        action = android.content.Intent.ACTION_SEND
                                        putExtra(android.content.Intent.EXTRA_TITLE, fileName)
                                        putExtra(android.content.Intent.EXTRA_SUBJECT, "SurveyCAD Export - $fileName")
                                        putExtra(android.content.Intent.EXTRA_TEXT, exportText)
                                        type = "text/plain"
                                    }
                                    val shareIntent = android.content.Intent.createChooser(sendIntent, "Share $fileName")
                                    context.startActivity(shareIntent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Could not launch share sheet: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentCyanDim, contentColor = AccentCyan)
                        ) {
                            Text("Share $selectedFormat", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
