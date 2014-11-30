package unarmed.web.endpoint;


import com.fasterxml.jackson.databind.ObjectMapper;
import holon.api.http.Content;
import holon.api.http.GET;
import holon.api.http.Request;
import holon.contrib.template.Templates;
import unarmed.domain.Counties;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Calendar;

import static holon.api.http.Status.Code.OK;
import static holon.util.collection.Maps.map;

public class FrontpageEndpoint
{
    private final Content frontpage;

    public FrontpageEndpoint( Templates templates )
    {
        frontpage = templates.load( "unarmed/frontpage.mustache" );
    }

    @GET("/")
//    @Cached(cacheKey= GeoEndpoint.GEOMAP_CACHE_KEY)
    public void frontpage(Request request, Connection conn, Counties counties) throws SQLException, IOException
    {
        String json = new ObjectMapper().writer().writeValueAsString( counties.allCountiesGeodata( conn, Calendar.getInstance().get(Calendar.YEAR) ) );
        request.respond( OK, frontpage, map( "geodata", json));
    }
}
