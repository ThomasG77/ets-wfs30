package org.opengis.cite.wfs30.util;

import static io.restassured.RestAssured.given;
import static io.restassured.http.Method.GET;
import static org.opengis.cite.wfs30.WFS3.GEOJSON_MIME_TYPE;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class JsonUtils {

    private JsonUtils() {
    }

    /**
     * Parses the id of the first feature from the passed json.
     * 
     * @param collectionItemJson
     *            the json document containing the features, never <code>null</code>
     * @return the parsed id, may be <code>null</code> if no feature could be found
     */
    public static String parseFeatureId( JsonPath collectionItemJson ) {
        List<Map<String, Object>> features = collectionItemJson.get( "features" );
        if ( features == null )
            return null;
        for ( Map<String, Object> feature : features ) {
            Object featureId = feature.get( "id" );
            if ( featureId != null && featureId instanceof String )
                return (String) featureId;
        }
        return null;
    }

    /**
     * Parses the temporal extent from the passed collection.
     *
     * @param collection
     *            the collection containing the extent to parse, never <code>null</code>
     * @return the parsed temporal extent, <code>null</code> if no extent exists
     * @throws IllegalArgumentException
     *             if the number of items in the extent invalid
     *
     */
    public static TemporalExtent parseTemporalExtent( Map<String, Object> collection ) {
        Object extent = collection.get( "extent" );
        if ( extent == null || !( extent instanceof Map ) )
            return null;
        Object spatial = ( (Map<String, Object>) extent ).get( "temporal" );
        if ( spatial == null || !( spatial instanceof List ) )
            return null;
        List<Object> coords = (List<Object>) spatial;
        if ( coords.size() == 2 ) {
            ZonedDateTime begin = parseAsDate( (String) coords.get( 0 ) );
            ZonedDateTime end = parseAsDate( (String) coords.get( 1 ) );
            return new TemporalExtent( begin, end );
        }
        throw new IllegalArgumentException( "Temporal extent with " + coords.size() + " items is invalid" );
    }

    /**
     * Parses the passed string as ISO 8601 date.
     * 
     * @param dateTime
     *            the dateTime to parse, never <code>null</code>
     * @return the parsed date, never <code>null</code>
     */
    public static ZonedDateTime parseAsDate( String dateTime ) {
        return ZonedDateTime.parse( dateTime );
    }

    /**
     * Formats the passed string as ISO 8601 date. Example: "2018-02-12T23:20:50Z"
     *
     * @param dateTime
     *            the dateTime to format, never <code>null</code>
     * @return the formatted date, never <code>null</code>
     */
    public static String formatDate( ZonedDateTime dateTime ) {
        return DateTimeFormatter.ISO_INSTANT.format( dateTime );
    }

    /**
     * Formats the passed string as ISO 8601 date. Example: "2018-02-12"
     *
     * @param date
     *            the dateTime to format, never <code>null</code>
     * @return the formatted date, never <code>null</code>
     */
    public static String formatDate( LocalDate date ) {
        return DateTimeFormatter.ISO_DATE.format( date );
    }

    /**
     * Formats the passed string as a period using a start and end time. Example:
     * "2018-02-12T00:00:00Z/2018-03-18T12:31:12Z"
     *
     * @param beginDateTime
     *            the begin dateTime to format, never <code>null</code>
     * @param endDateTime
     *            the end dateTime to format, never <code>null</code>
     * @return the formatted date, never <code>null</code>
     */
    public static String formatDateRange( ZonedDateTime beginDateTime, ZonedDateTime endDateTime ) {
        return formatDate( beginDateTime ) + "/" + formatDate( endDateTime );
    }

    /**
     * Formats the passed string as a period using start time and a duration. Example:
     * "2018-02-12T00:00:00Z/P1M6DT12H31M12S"
     *
     * @param beginDate
     *            the begin date to format, never <code>null</code>
     * @param endDate
     *            the end date to format, never <code>null</code>
     * @return the formatted date, never <code>null</code>
     */
    public static String formatDateRangeWithDuration( LocalDate beginDate, LocalDate endDate ) {
        Period betweenDate = Period.between( beginDate, endDate );
        return formatDate( beginDate ) + "/" + betweenDate;
    }

    /**
     * Parses the spatial extent from the passed collection.
     *
     * @param collection
     *            the collection containing the extent to parse, never <code>null</code>
     * @return the parsed bbox, <code>null</code> if no extent exists
     * @throws IllegalArgumentException
     *             if the number of items in the extent invalid
     *
     */
    public static BBox parseSpatialExtent( Map<String, Object> collection ) {
        Object extent = collection.get( "extent" );
        if ( extent == null || !( extent instanceof Map ) )
            return null;
        Object spatial = ( (Map<String, Object>) extent ).get( "spatial" );
        if ( spatial == null || !( spatial instanceof List ) )
            return null;
        List<Object> coords = (List<Object>) spatial;
        if ( coords.size() == 4 ) {
            double minX = parseValueAsDouble( coords.get( 0 ) );
            double minY = parseValueAsDouble( coords.get( 1 ) );
            double maxX = parseValueAsDouble( coords.get( 2 ) );
            double maxY = parseValueAsDouble( coords.get( 3 ) );
            return new BBox( minX, minY, maxX, maxY );
        } else if ( coords.size() == 6 ) {
            throw new IllegalArgumentException( "BBox with " + coords.size()
                                                + " coordinates is currently not supported" );
        }
        throw new IllegalArgumentException( "BBox with " + coords.size() + " coordinates is invalid" );
    }

    /**
     * Parses all links with 'type' of one of the passed mediaTypes and the 'rel' property with the passed value.
     *
     * @param links
     *            list of all links, never <code>null</code>
     * @param mediaTypesToSupport
     *            a list of media types the links searched for should support, may be empty but never <code>null</code>
     * @param expectedRel
     *            the expected value of the property 'rel', never <code>null</code>
     * @return a list of links supporting one of the media types and with the expected 'rel' property, may be empty but
     *         never <code>null</code>
     */
    public static List<Map<String, Object>> findLinksWithSupportedMediaTypeByRel( List<Map<String, Object>> links,
                                                                                  List<String> mediaTypesToSupport,
                                                                                  String expectedRel ) {
        List<Map<String, Object>> alternateLinks = new ArrayList<>();
        for ( Map<String, Object> link : links ) {
            Object type = link.get( "type" );
            Object rel = link.get( "rel" );
            if ( expectedRel.equals( rel ) && isSupportedMediaType( type, mediaTypesToSupport ) )
                alternateLinks.add( link );
        }
        return alternateLinks;
    }

    /**
     * Parsing the media types which does not have a link woth property 'type' for.
     *
     * @param links
     *            list of links to search in, never <code>null</code>
     * @param mediaTypesToSuppport
     *            a list of media types which should be supported, never <code>null</code>
     * @return the media types which does not have a link for.
     */
    public static List<String> findUnsupportedTypes( List<Map<String, Object>> links, List<String> mediaTypesToSuppport ) {
        List<String> unsupportedType = new ArrayList<>();
        for ( String contentMediaType : mediaTypesToSuppport ) {
            boolean hasLinkForContentType = hasLinkForContentType( links, contentMediaType );
            if ( !hasLinkForContentType )
                unsupportedType.add( contentMediaType );
        }
        return unsupportedType;
    }

    /**
     * Parses the links without 'rel' or 'type' property.
     * 
     * @param links
     *            list of links to search in, never <code>null</code>
     * @return the links without 'rel' or 'type' property
     */
    public static List<String> findLinksWithoutRelOrType( List<Map<String, Object>> links ) {
        List<String> linksWithoutRelOrType = new ArrayList<>();
        for ( Map<String, Object> alternateLink : links ) {
            if ( !linkIncludesRelAndType( alternateLink ) )
                linksWithoutRelOrType.add( (String) alternateLink.get( "href" ) );
        }
        return linksWithoutRelOrType;
    }

    /**
     * Parses the link with 'rel=self'.
     *
     * @param links
     *            list of links to search in, never <code>null</code>
     * @param expectedRel
     *            the expected value of the property 'rel', never <code>null</code>
     * @return the link to itself or <code>null</code> if no such link exists
     */
    public static Map<String, Object> findLinkByRel( List<Map<String, Object>> links, String expectedRel ) {
        for ( Map<String, Object> link : links ) {
            Object rel = link.get( "rel" );
            if ( expectedRel.equals( rel ) )
                return link;
        }
        return null;
    }

    /**
     * Checks if the passed link contains 'rel' and 'type' properties.
     * 
     * @param link
     *            to check, never <code>null</code>
     * @return <code>true</code> if the link contains 'rel' and 'type' properties, <code>false</code> otherwise
     */
    public static boolean linkIncludesRelAndType( Map<String, Object> link ) {
        Object rel = link.get( "rel" );
        Object type = link.get( "type" );
        if ( rel != null && type != null )
            return true;
        return false;
    }

    /**
     * Checks if a property with the passed name exists in the jsonPath.
     * 
     * @param propertyName
     *            name of the property to check, never <code>null</code>
     * @param jsonPath
     *            to check, never <code>null</code>
     * @return <code>true</code> if the property exists, <code>false</code> otherwise
     */
    public static boolean hasProperty( String propertyName, JsonPath jsonPath ) {
        return jsonPath.get( propertyName ) != null;
    }

    /**
     * Collects the number of all returned features by iterating over all 'next' links and summarizing the size of
     * features in 'features' array property.
     * 
     * @param jsonPath
     *            the initial collection, never <code>null</code>
     * @param maximumLimit
     *            the limit parameter value to use, if &lt;= 0 the parameter is omitted
     * @return the number of all returned features
     * @throws URISyntaxException
     *             if the creation of a uri fails
     */
    public static int collectNumberOfAllReturnedFeatures( JsonPath jsonPath, int maximumLimit )
                            throws URISyntaxException {
        int numberOfAllReturnedFeatures = jsonPath.getList( "features" ).size();
        Map<String, Object> nextLink = findLinkByRel( jsonPath.getList( "links" ), "next" );
        while ( nextLink != null ) {
            String nextUrl = (String) nextLink.get( "href" );
            URI uri = new URI( nextUrl );

            RequestSpecification accept = given().log().all().baseUri( nextUrl ).accept( GEOJSON_MIME_TYPE );
            String[] pairs = uri.getQuery().split( "&" );
            for ( String pair : pairs ) {
                int idx = pair.indexOf( "=" );
                String key = pair.substring( 0, idx );
                String value = pair.substring( idx + 1 );
                accept.param( key, value );
            }
            if ( maximumLimit > 0 ) {
                accept.param( "limit", maximumLimit );
            }

            Response response = accept.when().request( GET );
            response.then().statusCode( 200 );

            JsonPath nextJsonPath = response.jsonPath();
            int features = nextJsonPath.getList( "features" ).size();
            if ( features > 0 ) {
                numberOfAllReturnedFeatures += features;
                nextLink = findLinkByRel( nextJsonPath.getList( "links" ), "next" );
            } else {
                nextLink = null;
            }
        }
        return numberOfAllReturnedFeatures;
    }

    private static boolean hasLinkForContentType( List<Map<String, Object>> alternateLinks, String mediaType ) {
        for ( Map<String, Object> alternateLink : alternateLinks ) {
            Object type = alternateLink.get( "type" );
            if ( mediaType.equals( type ) )
                return true;
        }
        return false;
    }

    private static boolean isSupportedMediaType( Object type, List<String> contentMediaTypes ) {
        for ( String contentMediaType : contentMediaTypes ) {
            if ( contentMediaType.equals( type ) )
                return true;
        }
        return false;
    }

    private static double parseValueAsDouble( Object cords ) {
        if ( cords instanceof Integer ) {
            return ( (Integer) cords ).doubleValue();
        } else if ( cords instanceof Float ) {
            return ( (Float) cords ).doubleValue();
        } else if ( cords instanceof Double ) {
            return (Double) cords;
        } else {
            return Double.parseDouble( cords.toString() );
        }
    }

}
