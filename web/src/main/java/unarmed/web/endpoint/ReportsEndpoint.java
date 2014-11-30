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

import unarmed.domain.Attachment;
import unarmed.domain.Reports;
import unarmed.domain.auth.User;
import unarmed.infrastructure.date.DateTimeParser;
import unarmed.infrastructure.ui.function.TemplateFunctions;
import unarmed.web.endpoint.api.GeoEndpoint;
import unarmed.web.middleware.AuthorizationRequired;
import holon.api.http.Content;
import holon.api.http.FormParam;
import holon.api.http.GET;
import holon.api.http.POST;
import holon.api.http.PathParam;
import holon.api.http.QueryParam;
import holon.api.http.Request;
import holon.api.http.UploadedFile;
import holon.contrib.caching.HttpCache;
import holon.contrib.http.FileContent;
import holon.contrib.template.Templates;

import java.sql.Connection;
import java.sql.SQLException;

import static holon.api.http.Status.Code.OK;
import static holon.contrib.http.Redirect.redirect;
import static holon.util.collection.Maps.map;
import static java.util.Arrays.asList;

public class ReportsEndpoint
{
    private final Content uploadSucceeded;
    private final Content unverifiedReportList;
    private final Content verifiedReportList;
    private final Content createUnverified;
    private final Content unverifiedReport;
    private final Content createVerified;
    private final Content verifiedReport;

    private final HttpCache cache;
    private final Reports reports;
    private final DateTimeParser dateParser;
    private final FileContent fileContent;

    public ReportsEndpoint( HttpCache cache, Templates templates, Reports reports )
    {
        this.cache = cache;
        this.reports = reports;
        this.dateParser = new DateTimeParser( "dd/MM/yyyy" );
        this.uploadSucceeded = templates.load( "unarmed/reports/upload_succeeded.mustache" );
        this.unverifiedReportList = templates.load( "unarmed/reports/reports.mustache" );
        this.verifiedReportList = templates.load( "unarmed/reports/verified_reports.mustache" );
        this.createUnverified = templates.load( "unarmed/reports/create_report.mustache" );
        this.unverifiedReport = templates.load( "unarmed/reports/unverified_report.mustache" );
        this.createVerified = templates.load( "unarmed/reports/create_verified_report.mustache" );
        this.verifiedReport = templates.load( "unarmed/reports/verified_report.mustache" );

        this.fileContent = new FileContent();
    }

    @GET("/reports/create")
    public void reportForm(Request request) throws SQLException
    {
        request.respond( OK, createUnverified );
    }

    @POST("/reports")
    public void createUnverified( Request request, Connection conn,
            @FormParam( value = "name", defaultVal = "N/A" ) String victimName,
            @FormParam( value = "age", defaultVal = "0" ) String victimAge,
            @FormParam( "zip" ) String zipCode,
            @FormParam( "date" ) String date,
            @FormParam( "description" ) String desc,
            @FormParam( "links" ) String links,
            @FormParam( "email" ) String email,
            @FormParam( "attachment" ) UploadedFile uploaded ) throws Exception
    {
        long timestamp = dateParser.parse( date );
        zipCode = zipCode.replaceAll("\\s","");

        reports.newUnverifiedReport( conn, victimName,
                victimAge.trim().length() > 0 ? Integer.valueOf(victimAge) : -1,
                zipCode,
                timestamp,
                desc, email,
                links,
                asList( uploaded ) );
        redirect(request, "/reports/uploadsuccess" );
    }

    @GET("/reports/uploadsuccess")
    public void uploadSucceeded( Request request )
    {
        request.respond( OK, uploadSucceeded );
    }

    // Verified report management

    @GET("/reports/unverified")
    @AuthorizationRequired
    public void listUnverified(Request request, Connection conn) throws SQLException
    {
        request.respond( OK, unverifiedReportList, map(
                "reports", reports.unverifiedReports( conn ),
                "dateFormat", TemplateFunctions.dateFormat ) );
    }

    @GET("/reports/unverified/{id}")
    @AuthorizationRequired
    public void showUnverified(Request request, Connection conn, @PathParam("id") String id) throws SQLException
    {
        request.respond( OK, unverifiedReport, map(
                "report", reports.unverifiedReport( conn, id ),
                "dateFormat", TemplateFunctions.dateFormat ) );
    }

    @POST("/reports/unverified/{id}/archive")
    @AuthorizationRequired
    public void archiveUnverified(Request request, Connection conn, @PathParam("id") String id) throws SQLException
    {
        reports.archiveUnverified( conn, id );
        redirect( request, "/reports/unverified" );
    }

    @GET("/reports/{id}/attachment/{attachmentid}")
    @AuthorizationRequired
    public void getAttachment(Request request, Connection conn, @PathParam("attachmentid") String id) throws SQLException
    {
        Attachment context = reports.attachmentFile( conn, id );
        request.addHeader( "Content-type", context.contentType() )
               .respond( OK, fileContent, context.file() );
    }

    @GET("/reports/verified")
    @AuthorizationRequired
    public void verifiedReports(Request request, Connection conn ) throws SQLException
    {
        request.respond( OK, verifiedReportList, map(
                "reports", reports.verifiedReports( conn ),
                "dateFormat", TemplateFunctions.dateFormat ) );
    }

    @GET("/reports/verified/{id}")
    @AuthorizationRequired
    public void verifiedReport(Request request, Connection conn, @PathParam("id") String id ) throws SQLException
    {
        request.respond( OK, verifiedReport, map(
                "report", reports.verifiedReport( conn, id ),
                "dateFormat", TemplateFunctions.dateFormat ) );
    }

    @GET("/reports/verified/create")
    @AuthorizationRequired
    public void verifiedReportForm(Request request, Connection conn,
            @QueryParam(value="template", defaultVal="N/A") String templateId) throws SQLException
    {
        request.respond( OK, createVerified, map(
                "template", templateId.equals("N/A") ? null : reports.unverifiedReport( conn, templateId ),
                "dateFormat", TemplateFunctions.dateFormat ));
    }

    @POST("/reports/verified")
    @AuthorizationRequired
    public void createVerified(Request request, Connection conn, User user,
                               @FormParam(value="name", defaultVal = "N/A") String victimName,
                               @FormParam(value="age", defaultVal = "0") String victimAge,
                               @FormParam("zip") String zipCode,
                               @FormParam("date") String date,
                               @FormParam("description") String desc,
                               @FormParam(value="basedOn", defaultVal="") String basedOn ) throws SQLException
    {
        long timestamp = dateParser.parse( date );
        zipCode = zipCode.replaceAll("\\s","");
        reports.newVerifiedReport( conn, victimName, Integer.valueOf( victimAge ), zipCode, timestamp, desc,
                basedOn.length() > 0 ? basedOn : null, user );
        cache.evict( GeoEndpoint.GEOMAP_CACHE_KEY );
        redirect( request, "/reports/verified" );
    }
}
