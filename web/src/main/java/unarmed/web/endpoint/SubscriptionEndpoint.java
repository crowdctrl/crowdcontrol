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

import holon.api.http.Content;
import holon.api.http.FormParam;
import holon.api.http.GET;
import holon.api.http.POST;
import holon.api.http.Request;
import holon.api.http.Status;
import holon.contrib.template.Templates;
import unarmed.domain.subscriptions.Subscriptions;

import java.sql.Connection;
import java.sql.SQLException;

import static holon.contrib.http.Redirect.redirect;

public class SubscriptionEndpoint
{
    private final Content subscribeForm;
    private final Content subscribed;

    private final Subscriptions subscriptions;

    public SubscriptionEndpoint( Templates templates, Subscriptions reports )
    {
        this.subscriptions = reports;
        this.subscribeForm = templates.load( "unarmed/subscriptions/create.mustache" );
        this.subscribed = templates.load( "unarmed/subscriptions/subscribed.mustache" );
    }

    @GET("/subscription/create")
    public void subscriptionCreateForm( Request request ) throws SQLException
    {
        request.respond( Status.Code.OK, subscribeForm );
    }

    @GET("/subscription/created")
    public void subscriptionCreated( Request request ) throws SQLException
    {
        request.respond( Status.Code.OK, subscribed );
    }

    @POST("/subscription/create")
    public void createSubscription( Request request, Connection conn,
            @FormParam(value="reports", defaultVal = "no") String subscribeToReports,
            @FormParam(value="zips", defaultVal = "all") String zipFilter,
            @FormParam(value="states", defaultVal = "all") String stateFilter,
            @FormParam(value="newsletter", defaultVal = "no") String subscribeToNewsletter,
            @FormParam(value="email") String email ) throws SQLException
    {
        subscriptions.createTentative(conn, !subscribeToReports.equals( "no" ), !subscribeToNewsletter.equals( "no" ),
                zipFilter, stateFilter, email);
        redirect(request, "/subscription/created");
    }
}
