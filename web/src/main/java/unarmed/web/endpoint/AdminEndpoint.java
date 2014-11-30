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

import unarmed.web.middleware.AuthorizationRequired;
import holon.api.http.Content;
import holon.api.http.GET;
import holon.api.http.Request;
import holon.contrib.template.Templates;

import java.io.IOException;
import java.sql.SQLException;

import static holon.api.http.Status.Code.OK;

public class AdminEndpoint
{
    private final Content index;

    public AdminEndpoint( Templates templates )
    {
        index = templates.load( "unarmed/admin/index.mustache" );
    }

    @GET("/admin")
    @AuthorizationRequired
    public void index(Request request) throws SQLException, IOException
    {
        request.respond( OK, index );
    }
}
