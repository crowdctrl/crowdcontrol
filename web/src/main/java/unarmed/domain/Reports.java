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
package unarmed.domain;

import holon.api.http.UploadedFile;
import unarmed.domain.auth.User;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static holon.util.collection.Maps.map;

public class Reports
{
    private final Path uploadDir;

    public Reports(Path uploadDir) throws IOException
    {
        this.uploadDir = uploadDir;
        if(!Files.exists( uploadDir ))
        {
            Files.createDirectories( uploadDir );
        }
    }

    public void newUnverifiedReport( Connection conn, String victimName, int victimAge,
            String zipCode, long timestamp, String desc, String email, String links,
            List<UploadedFile> policeReports ) throws SQLException, IOException
    {
        PreparedStatement statement = conn.prepareStatement(
                "MERGE (zip:ZipCode {code:{0}}) \n" +
                "MERGE (submitter:Submitter {email:{1}}) \n" +
                "CREATE (report:UnverifiedReport {\n" +
                "   time: {2},\n" +
                "   submitTime: {3},\n" +
                "   description: {4},\n" +
                "   id: {5},\n" +
                "   victimName: {6},\n" +
                "   victimAge: {7},\n" +
                "   links:{8} " +
                "} ) " +
                "CREATE (report)-[:IN_ZIPCODE]->(zip) \n" +
                "FOREACH ( a in {9} | \n" +
                "    CREATE (report)-[:ATTACHED]->(:Attachment { path : a.path, id:a.id, name:a.name, type:a.type }))" );
        statement.setString( 0, zipCode );
        statement.setString( 1, email );
        statement.setLong(   2, timestamp );
        statement.setLong(   3, System.currentTimeMillis() );
        statement.setString( 4, desc );
        statement.setObject( 5, UUID.randomUUID().toString() );
        statement.setObject( 6, victimName );
        statement.setObject( 7, victimAge );
        statement.setObject( 8, links );
        statement.setObject( 9, moveAttachmentsToStorage( policeReports ) );
        statement.execute();
    }

    public List<UnverifiedReport> unverifiedReports( Connection conn ) throws SQLException
    {
        List<UnverifiedReport> unverifiedReports = new ArrayList<>();

        try(Statement st = conn.createStatement())
        {
            ResultSet res = st.executeQuery(
                    "MATCH (report:UnverifiedReport)-[:IN_ZIPCODE]->(zip) " +
                    "WHERE NOT (report:Archived) " +
                    "OPTIONAL MATCH (report)-[:ATTACHED]->(attachment) " +
                    "RETURN " +
                    "  report.submitTime as submitTime, "+
                    "  report.id as id, " +
                    "  report.victimName as victimName, " +
                    "  report.victimAge as victimAge, " +
                    "  report.description as desc, " +
                    "  report.time as time, " +
                    "  report.links as links, " +
                    "  zip.code as zipCode, COLLECT(attachment) as attachments " +
                    "ORDER BY submitTime DESC"
            );
            while(res.next())
            {
                unverifiedReports.add( new UnverifiedReport(
                        res.getString( "id" ),
                        res.getString( "victimName" ),
                        res.getInt( "victimAge" ),
                        res.getString("zipCode"),
                        res.getLong( "time" ),
                        res.getLong( "submitTime" ),
                        res.getString( "desc" ),
                        "N/A",
                        res.getString( "links" ),
                        null,
                        (List<Map<String, Object>>)res.getObject( "attachments" ) ) );
            }
        }
        return unverifiedReports;
    }

    public UnverifiedReport unverifiedReport( Connection conn, String id ) throws SQLException
    {
        PreparedStatement statement = conn.prepareStatement(
                "MATCH (report:UnverifiedReport)-[:IN_ZIPCODE]->(zip) " +
                "WHERE report.id = {0} " +
                "OPTIONAL MATCH (report)-[:ATTACHED]->(attachment) " +
                "OPTIONAL MATCH (zip)-[:IN_COUNTY]->(county) " +
                "OPTIONAL MATCH (verifiedReport)<-[:BASED_ON]->(report) " +
                "RETURN " +
                "  report.id as id, " +
                "  report.description as desc, " +
                "  report.victimName as victimName, " +
                "  report.victimAge as victimAge, " +
                "  report.time as time, " +
                "  report.submitTime as submitTime, " +
                "  report.links as links, " +
                "  COALESCE(county.name, \"N/A\") as countyName, " +
                "  zip.code as zipCode, COLLECT(attachment) as attachments, " +
                "  verifiedReport.id as verifiedId " +
                "ORDER BY time DESC");
        statement.setString( 0, id );

        try(ResultSet res = statement.executeQuery())
        {
            if ( res.next() )
            {
                return new UnverifiedReport( res.getString( "id" ),
                        res.getString( "victimName" ),
                        res.getInt( "victimAge" ),
                        res.getString( "zipCode" ),
                        res.getLong( "time" ),
                        res.getLong( "submitTime" ),
                        res.getString( "desc" ),
                        res.getString( "countyName" ),
                        res.getString( "links" ),
                        res.getString( "verifiedId" ),
                        (List<Map<String, Object>>) res.getObject( "attachments" ) );
            }
            else
            {
                // TODO, throw
                return null;
            }
        }
    }

    public Attachment attachmentFile( Connection conn, String id ) throws SQLException
    {
        PreparedStatement statement = conn.prepareStatement(
                "MATCH (attach:Attachment) " +
                "WHERE attach.id = {0} " +
                "RETURN attach.path as path, attach.type as type, attach.id as id");
        statement.setString( 0, id );
        try(ResultSet res = statement.executeQuery())
        {
            if ( res.next() )
            {
                return new Attachment(res.getString( "type" ), res.getString( "id" ), new File(res.getString( "path" )) );
            }
            else
            {
                // TODO, throw
                return null;
            }
        }
    }

    public void newVerifiedReport( Connection conn, String victimName, int victimAge, String zipCode, long timestamp,
            String desc, String basedOnUnverified, User verifiedBy ) throws SQLException
    {
        PreparedStatement statement = conn.prepareStatement(
                "MATCH (u:User {email:{0}}), (zip:ZipCode {code:{1}}) \n" +
                "CREATE (report:VerifiedReport {\n" +
                "   time: {2},\n" +
                "   description: {3},\n" +
                "   victimName: {4},\n" +
                "   victimAge: {5},\n" +
                "   id: {6}\n" +
                "} ) " +
                "CREATE (report)-[:IN_ZIPCODE]->(zip)");

        String id = UUID.randomUUID().toString();

        statement.setString( 0, verifiedBy.name() );
        statement.setString( 1, zipCode );
        statement.setLong( 2, timestamp );
        statement.setString( 3, desc );
        statement.setObject( 4, victimName );
        statement.setInt( 5, victimAge );
        statement.setObject( 6, id );
        statement.execute();

        if(basedOnUnverified != null)
        {
            PreparedStatement stmt = conn.prepareStatement(
                "MATCH (report:VerifiedReport {id: {0}}), (unverified:UnverifiedReport {id:{1}}) \n" +
                "CREATE (report)-[:BASED_ON]->(unverified)");

            stmt.setObject( 0, id );
            stmt.setObject( 1, basedOnUnverified );
            stmt.execute();
        }
    }

    public List<VerifiedReport> verifiedReports( Connection conn ) throws SQLException
    {
        List<VerifiedReport> reports = new ArrayList<>();

        try(Statement st = conn.createStatement())
        {
            ResultSet res = st.executeQuery(
                    "MATCH (report:VerifiedReport)-[:IN_ZIPCODE]->(zip) " +
                    "RETURN " +
                    "  report.id as id, " +
                    "  report.victimName as victimName, " +
                    "  report.victimAge as victimAge, " +
                    "  report.description as desc, " +
                    "  report.time as time, " +
                    "  zip.code as zipCode " +
                    "ORDER BY time DESC"
            );
            while(res.next())
            {
                reports.add( new VerifiedReport(
                        res.getString( "id" ),
                        res.getString( "victimName" ),
                        res.getInt( "victimAge" ),
                        res.getString( "zipCode" ),
                        res.getLong( "time" ),
                        res.getString( "desc" ) ) );
            }
        }
        return reports;
    }

    private List<Object> moveAttachmentsToStorage( List<UploadedFile> policeReports ) throws IOException
    {
        List<Object> attachments = new ArrayList<>();
        for ( UploadedFile file : policeReports )
        {
            String uuid = UUID.randomUUID().toString();

            Path targetFile = uploadDir.resolve( uuid );
            Files.move( file.file().toPath(), targetFile, StandardCopyOption.REPLACE_EXISTING );
            attachments.add( map(
                    "path", targetFile.toString(),
                    "id", uuid,
                    "name", file.fileName(),
                    "type", file.contentType()) );
        }

        return attachments;
    }

    public VerifiedReport verifiedReport( Connection conn, String id ) throws SQLException
    {
        PreparedStatement stmt = conn.prepareStatement(
                "MATCH (report:VerifiedReport)-[:IN_ZIPCODE]->(zip) " +
                "WHERE report.id = {0} " +
                "OPTIONAL MATCH (report)-[:BASED_ON]->(unverified) " +
                "RETURN " +
                "  report.id as id, " +
                "  report.description as desc, " +
                "  report.victimName as victimName, " +
                "  report.victimAge as victimAge, " +
                "  report.time as time, " +
                "  zip.code as zipCode, " +
                "  unverified.id as basedOnId " +
                "ORDER BY time DESC"
        );
        stmt.setString( 0, id );

        try( ResultSet res = stmt.executeQuery();)
        {
            if(res.next())
            {
                return new VerifiedReport(
                        res.getString( "id" ),
                        res.getString( "victimName" ),
                        res.getInt( "victimAge" ),
                        res.getString( "zipCode" ),
                        res.getLong( "time" ),
                        res.getString( "desc" ),
                        res.getString( "basedOnId" ));
            }
        }

        throw new RuntimeException( "No verified report with id " + id );
    }

    public void archiveUnverified( Connection conn, String id ) throws SQLException
    {
        PreparedStatement stmt = conn.prepareStatement(
                "MATCH (report:UnverifiedReport {id : {0}}) " +
                "SET report:Archived"
        );
        stmt.setString( 0, id );
        stmt.execute();
    }
}
