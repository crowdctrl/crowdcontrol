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

import java.sql.Connection;
import java.sql.SQLException;

import unarmed.domain.auth.Users;
import unarmed.domain.auth.User;
import unarmed.web.middleware.AuthenticationMiddleware;
import holon.api.http.Content;
import holon.api.http.FormParam;
import holon.api.http.GET;
import holon.api.http.POST;
import holon.api.http.QueryParam;
import holon.api.http.Request;
import holon.contrib.session.Session;
import holon.contrib.template.Templates;

import static holon.api.http.Status.Code.OK;
import static holon.contrib.http.Redirect.redirect;
import static holon.util.collection.Maps.map;

public class AuthEndpoint
{
    private final Users users;
    private final Content loginForm;

    public AuthEndpoint( Users users, Templates templates )
    {
        this.users = users;
        this.loginForm = templates.load( "unarmed/auth/login.mustache" );
    }

    @POST("/auth/login")
    public void login(Request request, Session session, Connection conn,
                      @FormParam("email") String email, @FormParam("password") String password,
                      @QueryParam(value="redirect", defaultVal="/") String redirectPath) throws SQLException
    {
        if(users.validateLogin( conn, email, password ))
        {
            session.set( AuthenticationMiddleware.AUTH_SESSION_KEY, new User(email) );
        }
        redirect( request, redirectPath );
    }

    @GET("/auth/login")
    public void loginForm(Request request, Session session, Connection conn,
                          @QueryParam(value = "redirect", defaultVal = "/") String redirectPath) throws SQLException
    {
        request.respond( OK, loginForm, map("redirect", redirectPath) );
    }
}
