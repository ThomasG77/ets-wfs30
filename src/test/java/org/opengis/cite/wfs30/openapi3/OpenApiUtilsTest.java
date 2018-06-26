package org.opengis.cite.wfs30.openapi3;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.opengis.cite.wfs30.WFS3.PATH.API;
import static org.opengis.cite.wfs30.WFS3.PATH.COLLECTIONS;
import static org.opengis.cite.wfs30.openapi3.OpenApiUtils.retrieveTestPoints;

import java.net.URL;
import java.util.List;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;

import com.reprezen.kaizen.oasparser.OpenApi3Parser;
import com.reprezen.kaizen.oasparser.model3.MediaType;
import com.reprezen.kaizen.oasparser.model3.OpenApi3;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class OpenApiUtilsTest {

    @Test
    public void testRetrieveTestPoints() {
        OpenApi3Parser parser = new OpenApi3Parser();

        URL openAppiDocument = OpenApiUtilsTest.class.getResource( "openapi.json" );
        OpenApi3 apiModel = parser.parse( openAppiDocument, true );
        List<TestPoint> testPoints = retrieveTestPoints( apiModel );

        assertThat( testPoints.size(), is( 3 ) );

    }

    @Ignore
    @Test
    public void testRetrieveTestPoints_moreComplex() {
        OpenApi3Parser parser = new OpenApi3Parser();

        URL openAppiDocument = OpenApiUtilsTest.class.getResource( "openapi_moreComplex.json" );
        OpenApi3 apiModel = parser.parse( openAppiDocument, true );
        List<TestPoint> testPoints = retrieveTestPoints( apiModel );

        assertThat( testPoints.size(), is( 4 ) );

        TestPoint testPointWIthIndex = testPoints.get( 0 );
        assertThat( testPointWIthIndex.getTemplateReplacement().size(), is( 1 ) );
        assertThat( testPointWIthIndex.getTemplateReplacement().get( "index" ), is( "10" ) );

        TestPoint testPointWIthIndexAndEnum1 = testPoints.get( 1 );
        assertThat( testPointWIthIndexAndEnum1.getTemplateReplacement().size(), is( 2 ) );
        assertThat( testPointWIthIndexAndEnum1.getTemplateReplacement().get( "index" ), is( "10" ) );
        assertThat( testPointWIthIndexAndEnum1.getTemplateReplacement().get( "enum" ), is( "eins" ) );

        TestPoint testPointWIthIndexAndEnum2 = testPoints.get( 2 );
        assertThat( testPointWIthIndexAndEnum2.getTemplateReplacement().size(), is( 2 ) );
        assertThat( testPointWIthIndexAndEnum2.getTemplateReplacement().get( "index" ), is( "10" ) );
        assertThat( testPointWIthIndexAndEnum2.getTemplateReplacement().get( "enum" ), is( "zwei" ) );

        TestPoint testPointWIthIndexAndEnum3 = testPoints.get( 3 );
        assertThat( testPointWIthIndexAndEnum3.getTemplateReplacement().size(), is( 2 ) );
        assertThat( testPointWIthIndexAndEnum3.getTemplateReplacement().get( "index" ), is( "10" ) );
        assertThat( testPointWIthIndexAndEnum3.getTemplateReplacement().get( "enum" ), is( "drei" ) );
    }

    @Test
    public void testRetrieveTestPoints_API() {
        OpenApi3Parser parser = new OpenApi3Parser();

        URL openAppiDocument = OpenApiUtilsTest.class.getResource( "openapi.json" );
        OpenApi3 apiModel = parser.parse( openAppiDocument, true );
        List<TestPoint> testPoints = retrieveTestPoints( apiModel, API );

        assertThat( testPoints.size(), is( 1 ) );
    }

    @Test
    public void testRetrieveTestPoints_COLLECTIONS() {
        OpenApi3Parser parser = new OpenApi3Parser();

        URL openAppiDocument = OpenApiUtilsTest.class.getResource( "openapi.json" );
        OpenApi3 apiModel = parser.parse( openAppiDocument, true );
        List<TestPoint> testPoints = retrieveTestPoints( apiModel, COLLECTIONS );

        assertThat( testPoints.size(), is( 1 ) );
        Map<String, MediaType> contentMediaTypes = testPoints.get( 0 ).getContentMediaTypes();
        assertThat( contentMediaTypes.size(), is( 2 ) );
    }

}
