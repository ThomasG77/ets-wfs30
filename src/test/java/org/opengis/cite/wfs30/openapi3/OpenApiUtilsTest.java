package org.opengis.cite.wfs30.openapi3;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.net.URL;
import java.util.List;

import org.junit.Test;

import com.reprezen.kaizen.oasparser.OpenApi3Parser;
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
        List<TestPoint> testPoints = OpenApiUtils.retrieveTestPoints( apiModel );

        assertThat( testPoints.size(), is( 12 ) );
    }

    @Test
    public void testRetrieveTestPoints_morComplex() {
        OpenApi3Parser parser = new OpenApi3Parser();

        URL openAppiDocument = OpenApiUtilsTest.class.getResource( "openapi_moreComplex.json" );
        OpenApi3 apiModel = parser.parse( openAppiDocument, true );
        List<TestPoint> testPoints = OpenApiUtils.retrieveTestPoints( apiModel );

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

}
