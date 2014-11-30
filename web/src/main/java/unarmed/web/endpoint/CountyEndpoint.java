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
package unarmed.web.endpoint;

import unarmed.domain.Counties;
import unarmed.domain.CountyOverview;
import unarmed.domain.Reports;
import unarmed.infrastructure.ui.function.TemplateFunctions;
import holon.api.http.Content;
import holon.api.http.GET;
import holon.api.http.PathParam;
import holon.api.http.Request;
import holon.api.http.Status;
import holon.contrib.template.Templates;

import java.sql.Connection;
import java.sql.SQLException;

import static holon.api.http.Status.Code.OK;
import static holon.util.collection.Maps.map;

public class CountyEndpoint
{
    private final Content overview;

    private final Templates templates;
    private final Reports reports;
    private final Counties counties;

    public CountyEndpoint( Templates templates, Reports reports, Counties counties )
    {
        this.templates = templates;
        this.reports = reports;
        this.counties = counties;
        this.overview = templates.load( "unarmed/counties/county.mustache" );
    }

    @GET("/county/{id}")
    public void countyOverview(Request request, Connection conn, @PathParam("id") String id) throws SQLException
    {
        CountyOverview countyOverview = counties.countyDetails( conn, id );
        if(countyOverview == null )
        {
            request.respond( Status.Code.NOT_FOUND );
        }
        else
        {
            request.respond( OK, overview, map(
                    "county", countyOverview,
                    "dateFormat", TemplateFunctions.dateFormat ) );
        }
    }
}
