package com.example.surveycad.model

data class Point3D(
    val easting: Double,
    val northing: Double,
    val elevation: Double = 0.0
)

data class SurveyPoint(
    val id: String,
    val name: String,
    val easting: Double,
    val northing: Double,
    val elevation: Double,
    val layer: String = "POINT"
)

sealed class CadEntity {
    abstract val id: String
    abstract val layer: String

    data class Line(
        override val id: String,
        override val layer: String,
        val p1: Point3D,
        val p2: Point3D
    ) : CadEntity()

    data class Polyline(
        override val id: String,
        override val layer: String,
        val points: List<Point3D>,
        val isClosed: Boolean = false
    ) : CadEntity()

    data class Circle(
        override val id: String,
        override val layer: String,
        val center: Point3D,
        val radius: Double
    ) : CadEntity()

    data class Rectangle(
        override val id: String,
        override val layer: String,
        val points: List<Point3D>,
        val isClosed: Boolean = true
    ) : CadEntity()

    data class DimensionLinear(
        override val id: String,
        override val layer: String,
        val p1: Point3D,
        val p2: Point3D,
        val placement: Point3D,
        val label: String
    ) : CadEntity()

    data class DimensionAligned(
        override val id: String,
        override val layer: String,
        val p1: Point3D,
        val p2: Point3D,
        val label: String
    ) : CadEntity()

    data class DimensionAngular(
        override val id: String,
        override val layer: String,
        val vertex: Point3D,
        val p1: Point3D,
        val p2: Point3D,
        val label: String
    ) : CadEntity()
}

data class CadLayer(
    val name: String,
    val colorHex: String,
    val isVisible: Boolean = true,
    val isLocked: Boolean = false
)

enum class CrsType {
    LOCAL,
    WGS84_GEO,
    UTM,
    EPSG
}

data class CrsConfig(
    val type: CrsType = CrsType.LOCAL,
    val zone: Int = 49,
    val hemisphere: String = "S", // "N" or "S"
    val epsgCode: String = ""
)

data class CadViewOptions(
    val showLabels: Boolean = true,
    val showElevation: Boolean = true,
    val showSeqNumber: Boolean = false,
    val connectPoints: Boolean = false,
    val closedPolygon: Boolean = false,
    val colorByElevation: Boolean = false,
    val showTin: Boolean = false,
    val showContours: Boolean = false,
    val contourInterval: Double = 1.0,
    val contourResolution: Int = 70,
    val showMapPoints: Boolean = true,
    val showMapLabels: Boolean = true
)

enum class ViewMode {
    CAD,
    MAP,
    SPLIT
}

enum class ActiveTool {
    MEASURE_DISTANCE,
    MEASURE_AREA,
    MEASURE_AZIMUTH,
    STAKEOUT
}

enum class CadCommand(val alias: String, val prompt: String) {
    LINE("L", "Click start point of line"),
    POLYLINE("PL", "Click polyline points, press Enter to finish"),
    CIRCLE("C", "Click center point of circle"),
    RECTANGLE("REC", "Click first corner of rectangle"),
    MOVE("M", "Select entity/point to move"),
    COPY("CO", "Select entity/point to copy"),
    ROTATE("RO", "Select entity to rotate"),
    TRIM("TR", "Select line to trim"),
    EXTEND("EX", "Select line to extend"),
    OFFSET("O", "Select source entity for offset"),
    DELETE("DEL", "Click entity or point to delete"),
    DIMLINEAR("DIMLIN", "Click point 1, point 2 for linear dimension"),
    DIMALIGNED("DIMALN", "Click point 1, point 2 for aligned dimension"),
    DIMANGULAR("DIMANG", "Click vertex, point 1, point 2 for angle dimension")
}
