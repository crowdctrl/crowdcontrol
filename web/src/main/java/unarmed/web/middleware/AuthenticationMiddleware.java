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
package unarmed.web.middleware;

import unarmed.domain.auth.User;
import unarmed.domain.auth.UnauthenticatedUser;
import holon.api.middleware.MiddlewareHandler;
import holon.api.middleware.Pipeline;
import holon.contrib.session.Session;

public class AuthenticationMiddleware
{
    public static final String AUTH_SESSION_KEY = "__auth";

    @MiddlewareHandler
    public void handle( Pipeline pipeline, Session session )
    {
        User user = session.get( AUTH_SESSION_KEY );
        if(user == null)
        {
            user = new UnauthenticatedUser();
            session.set( AUTH_SESSION_KEY, user );
        }

        pipeline.satisfyDependency( User.class, user );
        pipeline.call();
    }
}
