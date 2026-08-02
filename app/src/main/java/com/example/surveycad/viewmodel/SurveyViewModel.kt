package com.example.surveycad.viewmodel

import androidx.lifecycle.ViewModel
import com.example.surveycad.model.ActiveTool
import com.example.surveycad.model.CadCommand
import com.example.surveycad.model.CadEntity
import com.example.surveycad.model.CadLayer
import com.example.surveycad.model.CadViewOptions
import com.example.surveycad.model.CrsConfig
import com.example.surveycad.model.CrsType
import com.example.surveycad.model.Point3D
import com.example.surveycad.model.SurveyPoint
import com.example.surveycad.model.ViewMode
import com.example.surveycad.util.PenzParser
import com.example.surveycad.util.SurveyCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class SurveyViewModel : ViewModel() {

    private val _points = MutableStateFlow<List<SurveyPoint>>(emptyList())
    val points: StateFlow<List<SurveyPoint>> = _points.asStateFlow()

    private val _entities = MutableStateFlow<List<CadEntity>>(emptyList())
    val entities: StateFlow<List<CadEntity>> = _entities.asStateFlow()

    private val defaultLayers = mapOf(
        "POINT" to CadLayer("POINT", "#00D9C0"),
        "LINE" to CadLayer("LINE", "#5BE08A"),
        "POLYLINE" to CadLayer("POLYLINE", "#FF9D3D"),
        "DIMENSION" to CadLayer("DIMENSION", "#3D8FCE"),
        "CONTOUR" to CadLayer("CONTOUR", "#C0455A")
    )

    private val _layers = MutableStateFlow<Map<String, CadLayer>>(defaultLayers)
    val layers: StateFlow<Map<String, CadLayer>> = _layers.asStateFlow()

    private val _activeLayer = MutableStateFlow("LINE")
    val activeLayer: StateFlow<String> = _activeLayer.asStateFlow()

    private val _crs = MutableStateFlow(CrsConfig(type = CrsType.LOCAL, zone = 49, hemisphere = "S"))
    val crs: StateFlow<CrsConfig> = _crs.asStateFlow()

    private val _selectedPointId = MutableStateFlow<String?>(null)
    val selectedPointId: StateFlow<String?> = _selectedPointId.asStateFlow()

    private val _selectedEntityId = MutableStateFlow<String?>(null)
    val selectedEntityId: StateFlow<String?> = _selectedEntityId.asStateFlow()

    private val _viewOptions = MutableStateFlow(CadViewOptions())
    val viewOptions: StateFlow<CadViewOptions> = _viewOptions.asStateFlow()

    private val _viewMode = MutableStateFlow(ViewMode.CAD)
    val viewMode: StateFlow<ViewMode> = _viewMode.asStateFlow()

    private val _activeTool = MutableStateFlow<ActiveTool?>(null)
    val activeTool: StateFlow<ActiveTool?> = _activeTool.asStateFlow()

    private val _toolPoints = MutableStateFlow<List<Point3D>>(emptyList())
    val toolPoints: StateFlow<List<Point3D>> = _toolPoints.asStateFlow()

    private val _activeCommand = MutableStateFlow<CadCommand?>(null)
    val activeCommand: StateFlow<CadCommand?> = _activeCommand.asStateFlow()

    private val _commandStep = MutableStateFlow(0)
    val commandStep: StateFlow<Int> = _commandStep.asStateFlow()

    private val _commandPoints = MutableStateFlow<List<Point3D>>(emptyList())
    val commandPoints: StateFlow<List<Point3D>> = _commandPoints.asStateFlow()

    private val _commandHint = MutableStateFlow("No active command")
    val commandHint: StateFlow<String> = _commandHint.asStateFlow()

    private val _projectName = MutableStateFlow("survey_project")
    val projectName: StateFlow<String> = _projectName.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    init {
        loadSampleProject("utm49s")
    }

    fun showToast(msg: String) {
        _toastMessage.value = msg
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    fun setViewMode(mode: ViewMode) {
        _viewMode.value = mode
    }

    fun setCrs(crs: CrsConfig) {
        _crs.value = crs
    }

    fun setViewOptions(options: CadViewOptions) {
        _viewOptions.value = options
    }

    fun updateViewOptions(transform: (CadViewOptions) -> CadViewOptions) {
        _viewOptions.value = transform(_viewOptions.value)
    }

    fun selectPoint(id: String?) {
        _selectedPointId.value = id
        if (id != null) {
            _selectedEntityId.value = null
        }
    }

    fun selectEntity(id: String?) {
        _selectedEntityId.value = id
        if (id != null) {
            _selectedPointId.value = null
        }
    }

    fun setActiveLayer(name: String) {
        if (_layers.value.containsKey(name)) {
            _activeLayer.value = name
        }
    }

    fun addLayer(name: String, colorHex: String) {
        val upperName = name.trim().uppercase()
        if (upperName.isNotEmpty() && !_layers.value.containsKey(upperName)) {
            val updated = _layers.value.toMutableMap()
            updated[upperName] = CadLayer(upperName, colorHex)
            _layers.value = updated
            _activeLayer.value = upperName
            showToast("Layer $upperName created")
        }
    }

    fun toggleLayerVisibility(name: String) {
        val layer = _layers.value[name] ?: return
        val updated = _layers.value.toMutableMap()
        updated[name] = layer.copy(isVisible = !layer.isVisible)
        _layers.value = updated
    }

    fun toggleLayerLock(name: String) {
        val layer = _layers.value[name] ?: return
        val updated = _layers.value.toMutableMap()
        updated[name] = layer.copy(isLocked = !layer.isLocked)
        _layers.value = updated
    }

    fun deleteLayer(name: String) {
        if (_layers.value.size <= 1) {
            showToast("At least one layer is required")
            return
        }
        val pointsInLayer = _points.value.any { it.layer == name }
        val entitiesInLayer = _entities.value.any { it.layer == name }
        if (pointsInLayer || entitiesInLayer) {
            showToast("Layer $name is in use by objects")
            return
        }
        val updated = _layers.value.toMutableMap()
        updated.remove(name)
        _layers.value = updated
        if (_activeLayer.value == name) {
            _activeLayer.value = updated.keys.first()
        }
        showToast("Layer $name deleted")
    }

    fun updatePoint(point: SurveyPoint) {
        _points.value = _points.value.map { if (it.id == point.id) point else it }
        showToast("Point ${point.name} saved")
    }

    fun deletePoint(id: String) {
        _points.value = _points.value.filterNot { it.id == id }
        if (_selectedPointId.value == id) _selectedPointId.value = null
        showToast("Point deleted")
    }

    fun addEntity(entity: CadEntity) {
        _entities.value = _entities.value + entity
        showToast("Created ${entity::class.simpleName}")
    }

    fun deleteSelectedEntity() {
        val selId = _selectedEntityId.value ?: return
        _entities.value = _entities.value.filterNot { it.id == selId }
        _selectedEntityId.value = null
        showToast("Entity deleted")
    }

    fun setActiveTool(tool: ActiveTool?) {
        _activeTool.value = if (_activeTool.value == tool) null else tool
        _toolPoints.value = emptyList()
    }

    fun addToolPoint(point: Point3D) {
        _toolPoints.value = _toolPoints.value + point
    }

    fun clearToolPoints() {
        _toolPoints.value = emptyList()
        _activeTool.value = null
    }

    fun activateCommand(command: CadCommand) {
        _activeCommand.value = command
        _commandStep.value = 0
        _commandPoints.value = emptyList()
        _commandHint.value = "${command.name} - ${command.prompt}"
        _activeTool.value = null
        showToast("Command: ${command.name}")
    }

    fun cancelCommand() {
        _activeCommand.value = null
        _commandStep.value = 0
        _commandPoints.value = emptyList()
        _commandHint.value = "No active command"
    }

    fun handleCommandClickPoint(clickPt: Point3D) {
        val cmd = _activeCommand.value ?: return
        val currentPts = _commandPoints.value + clickPt
        _commandPoints.value = currentPts

        when (cmd) {
            CadCommand.LINE -> {
                if (currentPts.size >= 2) {
                    val p1 = currentPts[currentPts.size - 2]
                    val p2 = currentPts[currentPts.size - 1]
                    addEntity(
                        CadEntity.Line(
                            id = "e_" + UUID.randomUUID().toString().take(8),
                            layer = _activeLayer.value,
                            p1 = p1,
                            p2 = p2
                        )
                    )
                    _commandHint.value = "LINE - click next point or press Enter/Cancel"
                }
            }
            CadCommand.POLYLINE -> {
                _commandHint.value = "POLYLINE - point #${currentPts.size}, press Enter when finished"
            }
            CadCommand.CIRCLE -> {
                if (currentPts.size == 2) {
                    val c = currentPts[0]
                    val edge = currentPts[1]
                    val radius = SurveyCalculator.calcBetween(c, edge).distance
                    addEntity(
                        CadEntity.Circle(
                            id = "e_" + UUID.randomUUID().toString().take(8),
                            layer = _activeLayer.value,
                            center = c,
                            radius = radius
                        )
                    )
                    showToast("Circle created, r = ${"%.2f".format(radius)}m")
                    cancelCommand()
                } else {
                    _commandHint.value = "CIRCLE - click radius edge point"
                }
            }
            CadCommand.RECTANGLE -> {
                if (currentPts.size == 2) {
                    val a = currentPts[0]
                    val b = currentPts[1]
                    val pts = listOf(
                        Point3D(a.easting, a.northing, 0.0),
                        Point3D(b.easting, a.northing, 0.0),
                        Point3D(b.easting, b.northing, 0.0),
                        Point3D(a.easting, b.northing, 0.0)
                    )
                    addEntity(
                        CadEntity.Rectangle(
                            id = "e_" + UUID.randomUUID().toString().take(8),
                            layer = _activeLayer.value,
                            points = pts
                        )
                    )
                    showToast("Rectangle created")
                    cancelCommand()
                } else {
                    _commandHint.value = "RECTANGLE - click opposite corner"
                }
            }
            CadCommand.DIMLINEAR, CadCommand.DIMALIGNED -> {
                if (currentPts.size == 2) {
                    val a = currentPts[0]
                    val b = currentPts[1]
                    val dist = SurveyCalculator.calcBetween(a, b).distance
                    val label = "${"%.3f".format(dist)} m"
                    val dimEntity = if (cmd == CadCommand.DIMLINEAR) {
                        CadEntity.DimensionLinear(
                            id = "e_" + UUID.randomUUID().toString().take(8),
                            layer = "DIMENSION",
                            p1 = a,
                            p2 = b,
                            placement = Point3D((a.easting + b.easting) / 2, (a.northing + b.northing) / 2 + 1.0),
                            label = label
                        )
                    } else {
                        CadEntity.DimensionAligned(
                            id = "e_" + UUID.randomUUID().toString().take(8),
                            layer = "DIMENSION",
                            p1 = a,
                            p2 = b,
                            label = label
                        )
                    }
                    addEntity(dimEntity)
                    showToast("Dimension: $label")
                    cancelCommand()
                } else {
                    _commandHint.value = "${cmd.name} - click second point"
                }
            }
            CadCommand.DIMANGULAR -> {
                if (currentPts.size == 3) {
                    val v = currentPts[0]
                    val p1 = currentPts[1]
                    val p2 = currentPts[2]
                    val a1 = Math.toDegrees(Math.atan2(p1.easting - v.easting, p1.northing - v.northing))
                    val a2 = Math.toDegrees(Math.atan2(p2.easting - v.easting, p2.northing - v.northing))
                    var ang = Math.abs(a1 - a2)
                    if (ang > 180) ang = 360.0 - ang
                    val label = "${"%.2f".format(ang)}°"
                    addEntity(
                        CadEntity.DimensionAngular(
                            id = "e_" + UUID.randomUUID().toString().take(8),
                            layer = "DIMENSION",
                            vertex = v,
                            p1 = p1,
                            p2 = p2,
                            label = label
                        )
                    )
                    showToast("Angle: $label")
                    cancelCommand()
                } else {
                    _commandHint.value = "DIMANGULAR - click point #${currentPts.size + 1}"
                }
            }
            else -> {
                // Other commands
            }
        }
    }

    fun finishPolylineCommand() {
        val pts = _commandPoints.value
        if (pts.size >= 2) {
            addEntity(
                CadEntity.Polyline(
                    id = "e_" + UUID.randomUUID().toString().take(8),
                    layer = _activeLayer.value,
                    points = pts,
                    isClosed = false
                )
            )
            showToast("Polyline created (${pts.size} points)")
        }
        cancelCommand()
    }

    fun loadPenzData(rawText: String) {
        val result = PenzParser.parsePENZ(rawText)
        if (result.points.isNotEmpty()) {
            _points.value = result.points
            _selectedPointId.value = null
            showToast("Loaded ${result.points.size} points" + if (result.skippedLinesCount > 0) " (${result.skippedLinesCount} lines skipped)" else "")
        } else {
            showToast("No valid PENZ data found")
        }
    }

    fun loadSampleProject(presetKey: String) {
        val sampleText: String
        val newCrs: CrsConfig
        when (presetKey) {
            "utm49s" -> {
                newCrs = CrsConfig(type = CrsType.UTM, zone = 49, hemisphere = "S")
                sampleText = """
                    BM1 410250.500 9245100.200 45.250
                    P2 410280.750 9245095.400 44.980
                    P3 410310.200 9245110.600 46.120
                    P4 410305.900 9245150.300 47.500
                    P5 410265.400 9245160.800 46.850
                    P6 410235.100 9245135.500 45.600
                    CP1 410268.300 9245122.100 45.900
                """.trimIndent()
            }
            "utm50s" -> {
                newCrs = CrsConfig(type = CrsType.UTM, zone = 50, hemisphere = "S")
                sampleText = """
                    TS01 512400.000 9672500.000 12.400
                    TS02 512430.500 9672510.200 12.800
                    TS03 512461.200 9672519.900 13.150
                    TS04 512492.800 9672528.400 13.600
                    TS05 512524.100 9672535.700 14.050
                    TS06 512556.400 9672541.300 14.500
                    TS07 512589.000 9672545.800 15.100
                """.trimIndent()
            }
            else -> { // wgs84
                newCrs = CrsConfig(type = CrsType.WGS84_GEO)
                sampleText = """
                    BM-A 106.827200 -6.175400 32.500
                    BM-B 106.828100 -6.174900 33.100
                    CP-01 106.827650 -6.175650 32.800
                    CP-02 106.827900 -6.175100 33.400
                """.trimIndent()
            }
        }
        _crs.value = newCrs
        loadPenzData(sampleText)
    }

    fun newProject() {
        _points.value = emptyList()
        _entities.value = emptyList()
        _layers.value = defaultLayers
        _activeLayer.value = "LINE"
        _crs.value = CrsConfig(type = CrsType.LOCAL)
        _projectName.value = "new_project"
        _selectedPointId.value = null
        _selectedEntityId.value = null
        showToast("New project initialized")
    }

    fun exportContent(format: String): String {
        return when (format.lowercase()) {
            "csv" -> PenzParser.exportCsv(_points.value, _entities.value)
            "txt" -> PenzParser.exportTxt(_points.value)
            "geojson" -> PenzParser.exportGeoJson(_points.value)
            "dxf" -> PenzParser.exportDxf(_projectName.value, _points.value, _entities.value, _layers.value)
            else -> PenzParser.exportCsv(_points.value, _entities.value)
        }
    }
}
