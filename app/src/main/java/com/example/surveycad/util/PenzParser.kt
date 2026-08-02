package com.example.surveycad.util

import com.example.surveycad.model.CadEntity
import com.example.surveycad.model.CadLayer
import com.example.surveycad.model.SurveyPoint
import java.util.UUID

data class ParseResult(
    val points: List<SurveyPoint>,
    val skippedLinesCount: Int
)

object PenzParser {

    fun parsePENZ(text: String): ParseResult {
        val lines = text.split("\n", "\r\n")
        val points = mutableListOf<SurveyPoint>()
        var skipped = 0

        for (rawLine in lines) {
            val line = rawLine.trim()
            if (line.isEmpty() || line.startsWith("#") || line.startsWith(";")) {
                continue
            }

            // Split by comma, semicolon, tab, or whitespace
            val parts = line.split(Regex("[,;\\t]+|\\s+")).filter { it.isNotEmpty() }
            if (parts.size < 4) {
                skipped++
                continue
            }

            val name = parts[0]
            val easting = parts[1].toDoubleOrNull()
            val northing = parts[2].toDoubleOrNull()
            val elevation = parts[3].toDoubleOrNull()

            if (easting == null || northing == null || elevation == null) {
                skipped++
                continue
            }

            points.add(
                SurveyPoint(
                    id = "p_" + UUID.randomUUID().toString().take(8),
                    name = name,
                    easting = easting,
                    northing = northing,
                    elevation = elevation,
                    layer = "POINT"
                )
            )
        }

        return ParseResult(points, skipped)
    }

    fun exportCsv(
        points: List<SurveyPoint>,
        entities: List<CadEntity> = emptyList()
    ): String {
        val sb = StringBuilder()
        sb.append("Point,Easting,Northing,Elevation,Layer\n")
        for (p in points) {
            sb.append("${p.name},${"%.3f".format(p.easting)},${"%.3f".format(p.northing)},${"%.3f".format(p.elevation)},${p.layer}\n")
        }

        if (entities.isNotEmpty()) {
            sb.append("\n# CAD DRAWING ENTITIES\n")
            sb.append("Type,Layer,Details,Coordinates\n")
            for (ent in entities) {
                val typeName = ent::class.simpleName ?: "Entity"
                when (ent) {
                    is CadEntity.Line -> {
                        sb.append("$typeName,${ent.layer},Length=${"%.3f".format(SurveyCalculator.calcBetween(ent.p1, ent.p2).distance)}m,\"P1(${ent.p1.easting}, ${ent.p1.northing}, ${ent.p1.elevation}) -> P2(${ent.p2.easting}, ${ent.p2.northing}, ${ent.p2.elevation})\"\n")
                    }
                    is CadEntity.Polyline -> {
                        sb.append("$typeName,${ent.layer},Vertices=${ent.points.size},Closed=${ent.isClosed}\n")
                    }
                    is CadEntity.Rectangle -> {
                        sb.append("$typeName,${ent.layer},Corners=${ent.points.size},Closed=true\n")
                    }
                    is CadEntity.Circle -> {
                        sb.append("$typeName,${ent.layer},Radius=${"%.3f".format(ent.radius)}m,\"Center(${ent.center.easting}, ${ent.center.northing}, ${ent.center.elevation})\"\n")
                    }
                    is CadEntity.DimensionLinear -> {
                        sb.append("$typeName,${ent.layer},Label=${ent.label},\"P1(${ent.p1.easting}, ${ent.p1.northing}) -> P2(${ent.p2.easting}, ${ent.p2.northing})\"\n")
                    }
                    is CadEntity.DimensionAligned -> {
                        sb.append("$typeName,${ent.layer},Label=${ent.label},\"P1(${ent.p1.easting}, ${ent.p1.northing}) -> P2(${ent.p2.easting}, ${ent.p2.northing})\"\n")
                    }
                    is CadEntity.DimensionAngular -> {
                        sb.append("$typeName,${ent.layer},Label=${ent.label},\"Vertex(${ent.vertex.easting}, ${ent.vertex.northing})\"\n")
                    }
                }
            }
        }
        return sb.toString()
    }

    fun exportTxt(points: List<SurveyPoint>): String {
        val sb = StringBuilder()
        for (p in points) {
            sb.append("${p.name} ${"%.3f".format(p.easting)} ${"%.3f".format(p.northing)} ${"%.3f".format(p.elevation)}\n")
        }
        return sb.toString()
    }

    fun exportGeoJson(points: List<SurveyPoint>): String {
        val sb = StringBuilder()
        sb.append("{\n  \"type\": \"FeatureCollection\",\n  \"features\": [\n")
        points.forEachIndexed { index, p ->
            sb.append("    {\n")
            sb.append("      \"type\": \"Feature\",\n")
            sb.append("      \"properties\": {\n")
            sb.append("        \"name\": \"${p.name}\",\n")
            sb.append("        \"elevation\": ${p.elevation},\n")
            sb.append("        \"easting\": ${p.easting},\n")
            sb.append("        \"northing\": ${p.northing},\n")
            sb.append("        \"layer\": \"${p.layer}\"\n")
            sb.append("      },\n")
            sb.append("      \"geometry\": {\n")
            sb.append("        \"type\": \"Point\",\n")
            sb.append("        \"coordinates\": [${p.easting}, ${p.northing}, ${p.elevation}]\n")
            sb.append("      }\n")
            sb.append("    }${if (index < points.size - 1) "," else ""}\n")
        }
        sb.append("  ]\n}")
        return sb.toString()
    }

    private fun hexToAciColor(hex: String): Int {
        val clean = hex.trim().removePrefix("#").lowercase()
        return when (clean) {
            "00d9c0" -> 4 // Cyan
            "5be08a" -> 3 // Green
            "ff9d3d" -> 2 // Yellow
            "3d8fce" -> 5 // Blue
            "c0455a" -> 1 // Red
            else -> 7 // White / Standard
        }
    }

    fun exportDxf(
        projectName: String,
        points: List<SurveyPoint>,
        entities: List<CadEntity>,
        layers: Map<String, CadLayer>
    ): String {
        val sb = StringBuilder()
        sb.append("0\nSECTION\n2\nTABLES\n0\nTABLE\n2\nLAYER\n70\n${layers.size}\n")
        for ((name, layer) in layers) {
            val lockedFlag = if (layer.isLocked) 4 else 0
            val aciColor = hexToAciColor(layer.colorHex)
            sb.append("0\nLAYER\n2\n$name\n70\n$lockedFlag\n62\n$aciColor\n6\nCONTINUOUS\n")
        }
        sb.append("0\nENDTAB\n0\nENDSEC\n")

        sb.append("0\nSECTION\n2\nENTITIES\n")

        // Points & Text Labels
        for (p in points) {
            val lyr = p.layer.ifEmpty { "POINT" }
            sb.append("0\nPOINT\n8\n$lyr\n10\n${p.easting}\n20\n${p.northing}\n30\n${p.elevation}\n")
            sb.append("0\nTEXT\n8\n$lyr\n10\n${p.easting + 0.4}\n20\n${p.northing + 0.4}\n30\n${p.elevation}\n40\n0.5\n1\n${p.name} (Z=${"%.2f".format(p.elevation)})\n")
        }

        // CAD Drawing Entities
        for (ent in entities) {
            val lyr = ent.layer.ifEmpty { "LINE" }
            when (ent) {
                is CadEntity.Line -> {
                    sb.append("0\nLINE\n8\n$lyr\n10\n${ent.p1.easting}\n20\n${ent.p1.northing}\n30\n${ent.p1.elevation}\n11\n${ent.p2.easting}\n21\n${ent.p2.northing}\n31\n${ent.p2.elevation}\n")
                }
                is CadEntity.Polyline -> {
                    sb.append("0\nPOLYLINE\n8\n$lyr\n66\n1\n70\n${if (ent.isClosed) 1 else 0}\n")
                    for (pt in ent.points) {
                        sb.append("0\nVERTEX\n8\n$lyr\n10\n${pt.easting}\n20\n${pt.northing}\n30\n${pt.elevation}\n")
                    }
                    if (ent.isClosed && ent.points.isNotEmpty()) {
                        val p0 = ent.points.first()
                        sb.append("0\nVERTEX\n8\n$lyr\n10\n${p0.easting}\n20\n${p0.northing}\n30\n${p0.elevation}\n")
                    }
                    sb.append("0\nSEQEND\n")
                }
                is CadEntity.Rectangle -> {
                    sb.append("0\nPOLYLINE\n8\n$lyr\n66\n1\n70\n1\n")
                    for (pt in ent.points) {
                        sb.append("0\nVERTEX\n8\n$lyr\n10\n${pt.easting}\n20\n${pt.northing}\n30\n${pt.elevation}\n")
                    }
                    if (ent.points.isNotEmpty()) {
                        val p0 = ent.points.first()
                        sb.append("0\nVERTEX\n8\n$lyr\n10\n${p0.easting}\n20\n${p0.northing}\n30\n${p0.elevation}\n")
                    }
                    sb.append("0\nSEQEND\n")
                }
                is CadEntity.Circle -> {
                    sb.append("0\nCIRCLE\n8\n$lyr\n10\n${ent.center.easting}\n20\n${ent.center.northing}\n30\n${ent.center.elevation}\n40\n${ent.radius}\n")
                }
                is CadEntity.DimensionLinear -> {
                    sb.append("0\nLINE\n8\n$lyr\n10\n${ent.p1.easting}\n20\n${ent.p1.northing}\n30\n${ent.p1.elevation}\n11\n${ent.p2.easting}\n21\n${ent.p2.northing}\n31\n${ent.p2.elevation}\n")
                    val mx = (ent.p1.easting + ent.p2.easting) / 2
                    val my = (ent.p1.northing + ent.p2.northing) / 2
                    sb.append("0\nTEXT\n8\n$lyr\n10\n$mx\n20\n$my\n30\n0.0\n40\n0.4\n1\n${ent.label}\n")
                }
                is CadEntity.DimensionAligned -> {
                    sb.append("0\nLINE\n8\n$lyr\n10\n${ent.p1.easting}\n20\n${ent.p1.northing}\n30\n${ent.p1.elevation}\n11\n${ent.p2.easting}\n21\n${ent.p2.northing}\n31\n${ent.p2.elevation}\n")
                    val mx = (ent.p1.easting + ent.p2.easting) / 2
                    val my = (ent.p1.northing + ent.p2.northing) / 2
                    sb.append("0\nTEXT\n8\n$lyr\n10\n$mx\n20\n$my\n30\n0.0\n40\n0.4\n1\n${ent.label}\n")
                }
                is CadEntity.DimensionAngular -> {
                    val mx = (ent.p1.easting + ent.p2.easting) / 2
                    val my = (ent.p1.northing + ent.p2.northing) / 2
                    sb.append("0\nTEXT\n8\n$lyr\n10\n$mx\n20\n$my\n30\n0.0\n40\n0.4\n1\n${ent.label}\n")
                }
            }
        }

        sb.append("0\nENDSEC\n0\nEOF\n")
        return sb.toString()
    }
}
