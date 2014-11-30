/**
 * Copyright (c) 2002-2014 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package unarmed.web.endpoint.api;

import unarmed.domain.Counties;
import holon.api.http.GET;
import holon.api.http.QueryParam;
import holon.api.http.Request;
import holon.api.http.Status;
import holon.contrib.caching.Cached;
import holon.contrib.http.JsonContent;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public class GeoEndpoint
{
    public static final String GEOMAP_CACHE_KEY = "unarmed.countymap";
    private final JsonContent json = new JsonContent();

    @GET("/api/v1/geo/counties.topo.json")
    @Cached(cacheKey= GEOMAP_CACHE_KEY)
    public void getAllCounties(Request req, Connection conn, Counties counties,
            @QueryParam("year") String year) throws IOException,
            SQLException
    {
        req.respond( Status.Code.OK, json, counties.allCountiesGeodata( conn, Integer.valueOf( year ) ) );
    }
}
