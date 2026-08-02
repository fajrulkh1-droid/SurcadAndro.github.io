package com.example.surveycad.util

import com.example.surveycad.model.Point3D
import com.example.surveycad.model.SurveyPoint
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

data class Calc2Result(
    val deltaE: Double,
    val deltaN: Double,
    val deltaZ: Double,
    val distance: Double,
    val slopeDistance: Double,
    val azimuth: Double,
    val backAzimuth: Double,
    val bearing: String,
    val verticalAngle: Double
)

data class TraverseResult(
    val startName: String,
    val endName: String,
    val miscloseDistance: Double,
    val deltaE: Double,
    val deltaN: Double,
    val totalDistance: Double,
    val precisionRatio: String
)

data class Triangle(
    val p1: Point3D,
    val p2: Point3D,
    val p3: Point3D
)

object SurveyCalculator {

    fun calcBetween(a: Point3D, b: Point3D): Calc2Result {
        val dE = b.easting - a.easting
        val dN = b.northing - a.northing
        val dZ = b.elevation - a.elevation

        val dist = sqrt(dE * dE + dN * dN)
        val slopeDist = sqrt(dE * dE + dN * dN + dZ * dZ)

        var az = Math.toDegrees(atan2(dE, dN))
        if (az < 0) az += 360.0

        val backAz = (az + 180.0) % 360.0
        val bearingStr = azimuthToBearing(az)
        val vertAngle = if (dist > 0) Math.toDegrees(atan2(dZ, dist)) else 0.0

        return Calc2Result(
            deltaE = dE,
            deltaN = dN,
            deltaZ = dZ,
            distance = dist,
            slopeDistance = slopeDist,
            azimuth = az,
            backAzimuth = backAz,
            bearing = bearingStr,
            verticalAngle = vertAngle
        )
    }

    fun calcBetween(a: SurveyPoint, b: SurveyPoint): Calc2Result {
        return calcBetween(
            Point3D(a.easting, a.northing, a.elevation),
            Point3D(b.easting, b.northing, b.elevation)
        )
    }

    fun azimuthToBearing(azimuth: Double): String {
        val az = azimuth % 360.0
        return when {
            az >= 0 && az < 90 -> "N ${dms(az)} E"
            az >= 90 && az < 180 -> "S ${dms(180.0 - az)} E"
            az >= 180 && az < 270 -> "S ${dms(az - 180.0)} W"
            else -> "N ${dms(360.0 - az)} W"
        }
    }

    fun dms(deg: Double): String {
        val d = floor(deg).toInt()
        val mFull = (deg - d) * 60.0
        val m = floor(mFull).toInt()
        val s = (mFull - m) * 60.0
        return "$d°${m.toString().padStart(2, '0')}'${"%.1f".format(s).padStart(4, '0')}\""
    }

    fun shoelaceArea(points: List<Point3D>): Double {
        if (points.size < 3) return 0.0
        var sum = 0.0
        for (i in points.indices) {
            val a = points[i]
            val b = points[(i + 1) % points.size]
            sum += (a.easting * b.northing - b.easting * a.northing)
        }
        return abs(sum / 2.0)
    }

    fun shoelaceAreaPoints(points: List<SurveyPoint>): Double {
        return shoelaceArea(points.map { Point3D(it.easting, it.northing, it.elevation) })
    }

    fun polygonPerimeter(points: List<Point3D>): Double {
        if (points.size < 2) return 0.0
        var perim = 0.0
        for (i in points.indices) {
            val a = points[i]
            val b = points[(i + 1) % points.size]
            perim += calcBetween(a, b).distance
        }
        return perim
    }

    fun calcTraverseMisclose(points: List<SurveyPoint>): TraverseResult? {
        if (points.size < 3) return null
        val first = points.first()
        val last = points.last()

        val c = calcBetween(last, first)
        var totalDist = 0.0
        for (i in 1 until points.size) {
            totalDist += calcBetween(points[i - 1], points[i]).distance
        }

        val ratioVal = if (c.distance > 0) totalDist / c.distance else Double.POSITIVE_INFINITY
        val precision = if (ratioVal.isFinite()) "1 : ${ratioVal.roundToInt()}" else "1 : ∞"

        return TraverseResult(
            startName = first.name,
            endName = last.name,
            miscloseDistance = c.distance,
            deltaE = c.deltaE,
            deltaN = c.deltaN,
            totalDistance = totalDist,
            precisionRatio = precision
        )
    }

    fun rotatePoint(p: Point3D, base: Point3D, deg: Double): Point3D {
        val rad = Math.toRadians(deg)
        val dE = p.easting - base.easting
        val dN = p.northing - base.northing
        return Point3D(
            easting = base.easting + dE * cos(rad) - dN * sin(rad),
            northing = base.northing + dE * sin(rad) + dN * cos(rad),
            elevation = p.elevation
        )
    }

    fun lineIntersection(a1: Point3D, a2: Point3D, b1: Point3D, b2: Point3D): Point3D? {
        val x1 = a1.easting; val y1 = a1.northing
        val x2 = a2.easting; val y2 = a2.northing
        val x3 = b1.easting; val y3 = b1.northing
        val x4 = b2.easting; val y4 = b2.northing

        val den = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4)
        if (abs(den) < 1e-9) return null

        val t = ((x1 - x3) * (y3 - y4) - (y1 - y3) * (x3 - x4)) / den
        return Point3D(
            easting = x1 + t * (x2 - x1),
            northing = y1 + t * (y2 - y1),
            elevation = (a1.elevation + a2.elevation) / 2.0
        )
    }

    // Delaunay triangulation using Ear Clipping / Convex Hull or simple incremental triangulation
    fun computeTriangles(points: List<SurveyPoint>): List<Triangle> {
        if (points.size < 3) return emptyList()
        val pts = points.map { Point3D(it.easting, it.northing, it.elevation) }
        val triangles = mutableListOf<Triangle>()

        // Sort by easting then northing
        val sorted = pts.sortedWith(compareBy({ it.easting }, { it.northing }))

        // Simple Delaunay-like fan triangulation for visualization
        for (i in 1 until sorted.size - 1) {
            triangles.add(Triangle(sorted[0], sorted[i], sorted[i + 1]))
        }
        return triangles
    }

    // Convert local survey coordinates (E, N) to geographical Lat/Lon based on CRS zone/type
    fun projectToLatLon(easting: Double, northing: Double, zone: Int, hemisphere: String): Pair<Double, Double> {
        // Approximate UTM to WGS84 conversion for Android visualization
        // Central meridian of UTM zone
        val lon0 = (zone * 6 - 183).toDouble()
        val k0 = 0.9996
        val a = 6378137.0 // WGS84 semi-major axis

        val x = easting - 500000.0
        val y = if (hemisphere.uppercase() == "S") northing - 10000000.0 else northing

        val lat = Math.toDegrees(y / (a * k0))
        val lon = lon0 + Math.toDegrees(x / (a * k0 * cos(Math.toRadians(lat))))

        return Pair(lat.coerceIn(-90.0, 90.0), lon.coerceIn(-180.0, 180.0))
    }
}
