/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.wfs.bindings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.util.Map;
import net.opengis.wfs.FeatureCollectionType;
import net.opengis.wfs.WfsFactory;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.test.TestData;
import org.geotools.wfs.WFS;
import org.geotools.wfs.WFSTestSupport;
import org.geotools.xsd.Binding;
import org.junit.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.opengis.feature.simple.SimpleFeature;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class FeatureCollectionTypeBindingTest extends WFSTestSupport {

    public FeatureCollectionTypeBindingTest() {
        super(WFS.FeatureCollectionType, FeatureCollectionType.class, Binding.OVERRIDE);
    }

    @Override
    protected Map<String, String> getNamespaces() {
        return namespaces(Namespace("geotools", "http://geotools.org"));
    }

    @Override
    @SuppressWarnings("unchecked")
    @Test
    public void testEncode() throws Exception {

        DefaultFeatureCollection features = new DefaultFeatureCollection(null, null);

        SimpleFeatureTypeBuilder tb = new SimpleFeatureTypeBuilder();
        tb.setName("feature");
        tb.setNamespaceURI("http://geotools.org");
        tb.add("geometry", Point.class);
        tb.add("integer", Integer.class);

        SimpleFeatureBuilder b = new SimpleFeatureBuilder(tb.buildFeatureType());
        b.add(new GeometryFactory().createPoint(new Coordinate(0, 0)));
        b.add(0);
        features.add(b.buildFeature("zero"));

        b.add(new GeometryFactory().createPoint(new Coordinate(1, 1)));
        b.add(1);
        features.add(b.buildFeature("one"));

        FeatureCollectionType fc = WfsFactory.eINSTANCE.createFeatureCollectionType();
        fc.getFeature().add(features);

        Document dom = encode(fc, WFS.FeatureCollection);
        // print( dom );

        NodeList featureNodes = dom.getElementsByTagNameNS("http://geotools.org", "feature");

        assertEquals(2, featureNodes.getLength());
        for (int i = 0; i < featureNodes.getLength(); i++) {
            Element featureNode = (Element) featureNodes.item(i);
            assertNotNull(featureNode.getElementsByTagNameNS("http://geotools.org", "geometry"));
            assertNotNull(featureNode.getElementsByTagNameNS("http://geotools.org", "integer"));
        }
    }

    @Override
    @Test
    public void testParse() throws Exception {
        final URL resource = TestData.getResource(this, "FeatureCollectionTypeBindingTest.xml");
        buildDocument(resource);

        FeatureCollectionType fc = (FeatureCollectionType) parse();
        assertEquals(1, fc.getFeature().size());

        FeatureCollection features = (FeatureCollection) fc.getFeature().get(0);
        assertEquals(2, features.size());

        try (FeatureIterator fi = features.features()) {
            assertTrue(fi.hasNext());
            SimpleFeature f = (SimpleFeature) fi.next();

            assertEquals("feature", f.getType().getTypeName());
            assertTrue(f.getDefaultGeometry() instanceof LineString);
            assertEquals("1", f.getAttribute("integer").toString());
        }
    }
}
