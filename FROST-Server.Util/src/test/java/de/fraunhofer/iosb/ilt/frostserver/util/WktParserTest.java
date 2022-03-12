/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package de.fraunhofer.iosb.ilt.frostserver.util;

import org.geojson.GeoJsonObject;
import org.geojson.LineString;
import org.geojson.MultiPoint;
import org.geojson.Polygon;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

/**
 *
 * @author hylke
 */
public class WktParserTest {

    @Test
    void testParsePointSpaces() {
        String text = "POINT                     (      30                                              10    )";
        GeoJsonObject result = WktParser.parseWkt(text);
        assertEquals(TestHelper.getPoint(30, 10), result);
    }

    @Test
    void testParsePoint2D() {
        String text = "POINT (30 10)";
        GeoJsonObject result = WktParser.parseWkt(text);
        assertEquals(TestHelper.getPoint(30, 10), result);
    }

    @Test
    void testParsePoint3D() {
        String text = "POINTZ(30 10 10)";
        GeoJsonObject result = WktParser.parseWkt(text);
        assertEquals(TestHelper.getPoint(30, 10, 10), result);
    }

    @Test
    void testParsePointWithWrongDimension1D() {
        String text = "POINT (10)";
        assertThrows(IllegalArgumentException.class, () -> WktParser.parseWkt(text));
    }

    @Test
    void testParsePointWithWrongDimension4D() {
        String text = "POINT ZM (10 10 10 10)";
        assertThrows(IllegalArgumentException.class, () -> WktParser.parseWkt(text));
    }

    @Test
    void testParseMultiPoint2D() {
        MultiPoint expected = TestHelper.buildMutliPoint().a(30, 10).a(40, 20).a(50, 10).b();
        String text = "MULTIPOINT ((30 10),(40 20),(50 10))";
        GeoJsonObject result = WktParser.parseWkt(text);
        assertEquals(expected, result);
    }

    @Test
    void testParseMultiPoint3D() {
        MultiPoint expected = TestHelper.buildMutliPoint().a(30, 10, 1).a(40, 20, 2).a(50, 10, 1).b();
        String text = "MULTIPOINT Z ((30 10 1),(40 20 2),(50 10 1))";
        GeoJsonObject result = WktParser.parseWkt(text);
        assertEquals(expected, result);

        text = "MULTIPOINTM ( ( 30  10  1 ) , ( 40 20 2 ) , ( 50 10 1 ) ) ";
        result = WktParser.parseWkt(text);
        assertEquals(expected, result);
    }

    @Test
    void testParseLineString2D() {
        final LineString expected = TestHelper.getLine(new Integer[]{30, 10}, new Integer[]{10, 30}, new Integer[]{40, 40});
        String text = "LINESTRING (30 10, 10 30, 40 40)";
        GeoJsonObject result = WktParser.parseWkt(text);
        assertEquals(expected, result);

        text = "LINESTRING(30 10,10 30,40 40)";
        result = WktParser.parseWkt(text);
        assertEquals(expected, result);

        text = "LINESTRING  (30 10 , 10 30 , 40 40)";
        result = WktParser.parseWkt(text);
        assertEquals(expected, result);

        text = "LINESTRING      (30             10                 ,                    10                 30                ,           40             40        )         ";
        result = WktParser.parseWkt(text);
        assertEquals(expected, result);
    }

    @Test
    void testParseLineStringDecimal2D() {
        final LineString expected = TestHelper.getLine(new Double[]{30.1, 10.2}, new Double[]{0.1, .1}, new Double[]{40.0, 40.0});
        String text = "LINESTRING (30.1 10.2, 0.1 .1, 40.0 40.0)";
        GeoJsonObject result = WktParser.parseWkt(text);
        assertEquals(expected, result);
    }

    @Test
    void testParseLineString3D() {
        final LineString expected = TestHelper.getLine(new Integer[]{30, 10, 10}, new Integer[]{10, 30, 10}, new Integer[]{40, 40, 40});
        String text = "LINESTRING Z(30 10 10, 10 30 10, 40 40 40)";
        GeoJsonObject result = WktParser.parseWkt(text);
        assertEquals(expected, result);

        text = "LINESTRINGZ(30 10 10,10 30 10,40 40 40)";
        result = WktParser.parseWkt(text);
        assertEquals(expected, result);

        text = "LINESTRING Z (30 10 10 , 10 30 10 , 40 40 40)";
        result = WktParser.parseWkt(text);
        assertEquals(expected, result);
    }

    @Test
    void testParseLineStringWithMixedDimensions() {
        String text = "LINESTRING (30 10, 10 30 40)";
        assertThrows(IllegalArgumentException.class, () -> WktParser.parseWkt(text));
    }

    @Test
    void testParsePolygon2DOnlyExterior() {
        final Polygon expected = TestHelper.getPolygon(
                2,
                30, 10,
                10, 30,
                40, 40);
        String text = "POLYGON ((30 10, 10 30, 40 40))";
        GeoJsonObject result = WktParser.parseWkt(text);
        assertEquals(expected, result);
    }

    @Test
    void testParsePolygon2DWithInteriorRing() {
        Polygon expected = TestHelper.getPolygon(
                2,
                30, 10,
                10, 30,
                40, 40);
        expected.addInteriorRing(TestHelper.getPointList(
                2,
                29, 29,
                29, 30,
                30, 29));
        String text = "POLYGON ((30 10, 10 30, 40 40), (29 29, 29 30, 30 29))";
        GeoJsonObject result = WktParser.parseWkt(text);
        assertEquals(expected, result);
    }

    @Test
    void testParsePolygon2DWithMultpleInteriorRings() {
        String text = "POLYGON ((30 10, 10 30, 40 40), (29 29, 29 30, 30 29), (21 21, 21 22, 22 21))";
        Polygon polygon = TestHelper.getPolygon(
                2,
                30, 10,
                10, 30,
                40, 40);
        polygon.addInteriorRing(TestHelper.getPointList(
                2,
                29, 29,
                29, 30,
                30, 29));
        polygon.addInteriorRing(TestHelper.getPointList(
                2,
                21, 21,
                21, 22,
                22, 21));
        GeoJsonObject result = WktParser.parseWkt(text);
        assertEquals(polygon, result);
    }

    @Test
    void testParsePolygon3DOnlyExterior() {
        String text = "POLYGONZ ((30 10 1, 10 30 1, 40 40 1))";
        final Polygon expected = TestHelper.getPolygon(
                3,
                30, 10, 1,
                10, 30, 1,
                40, 40, 1);
        GeoJsonObject result = WktParser.parseWkt(text);
        assertEquals(expected, result);
    }

    @Test
    void testParsePolygon3DWithInteriorRing() {
        String text = "POLYGON z ((30 10 1, 10 30 1, 40 40 1), (29 29 1, 29 30 1, 30 29 1))";
        Polygon expected = TestHelper.getPolygon(
                3,
                30, 10, 1,
                10, 30, 1,
                40, 40, 1);
        expected.addInteriorRing(TestHelper.getPointList(
                3,
                29, 29, 1,
                29, 30, 1,
                30, 29, 1));
        GeoJsonObject result = WktParser.parseWkt(text);
        assertEquals(expected, result);
    }

    @Test
    void testParsePolygon3DWithMultpleInteriorRings() {
        String text = "POLYGON z ((30 10 1, 10 30 1, 40 40 1), (29 29 1, 29 30 1, 30 29 1), (21 21 1, 21 22 1, 22 21 1))";
        Polygon expected = TestHelper.getPolygon(
                3,
                30, 10, 1,
                10, 30, 1,
                40, 40, 1);
        expected.addInteriorRing(TestHelper.getPointList(
                3,
                29, 29, 1,
                29, 30, 1,
                30, 29, 1));
        expected.addInteriorRing(TestHelper.getPointList(
                3,
                21, 21, 1,
                21, 22, 1,
                22, 21, 1));
        GeoJsonObject result = WktParser.parseWkt(text);
        assertEquals(expected, result);
    }

    @Test
    void testParsePolygonWithMixedDimensions() {
        String text = "POLYGON ((30 10, 10 30 40))";
        assertThrows(IllegalArgumentException.class, () -> WktParser.parseWkt(text));
    }
}