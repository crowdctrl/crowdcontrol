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

import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static holon.util.collection.Maps.map;
import static java.util.Arrays.asList;

public class Counties
{
    /**
     * This loads a GEOJSON-formatted Map data structure that contains all counties in the US and their respective
     * number of reports.
     */
    public Map<String, Object> allCountiesGeodata( Connection conn, int year ) throws IOException, SQLException
    {
        Calendar cal = Calendar.getInstance();
        cal.set( year, Calendar.JANUARY, 1 );
        long startMillis = cal.getTimeInMillis();
        cal.set( year + 1, Calendar.JANUARY, 1 );
        long endMillis = cal.getTimeInMillis();

        Map<String, Object> topologicalData = new HashMap<>();
        topologicalData.put( "type", "Topology" );
        topologicalData.put( "transform", map(
                "scale",     asList( 0.035896170617061705, 0.005347309530953095 ),
                "translate", asList( -179.14734, 17.884813 ) ) );
        topologicalData.put( "objects", map( "counties", map(
                "type", "GeometryCollection",
                "geometries", loadCounties( conn, startMillis, endMillis ) ) ) );
        topologicalData.put( "arcs", loadUSGeoData( conn ) );

        return topologicalData;
    }

    private List<CountyGeoData> loadCounties( Connection conn, long startMillis, long endMillis ) throws SQLException,
            IOException
    {
        List<CountyGeoData> out = new ArrayList<>();
        PreparedStatement stmt = conn.prepareStatement(
                "MATCH (c:County) " +
                "OPTIONAL MATCH (c)<-[:IN_COUNTY]-(:ZipCode)<-[:IN_ZIPCODE]-(report:VerifiedReport) " +
                "WHERE report.time > {0} AND report.time < {1} " +
                "RETURN c, count(report) as numReports" );
        stmt.setLong( 0, startMillis );
        stmt.setLong( 1, endMillis );
        ResultSet resultSet = stmt.executeQuery();
        while(resultSet.next())
        {
            Map<String, Object> county = (Map<String, Object>) resultSet.getObject( "c" );
            out.add( new CountyGeoData( county, resultSet.getInt( "numReports" ) ) );
        }
        return out;
    }

    private Object loadUSGeoData( Connection conn ) throws SQLException, IOException
    {
        try(Statement stmt = conn.createStatement())
        {
            ResultSet resultSet = stmt.executeQuery( "MATCH (us:Geodata {key:'US'}) RETURN us.arcs as arcs" );
            if(resultSet.next())
            {
                return new ObjectMapper().readValue( resultSet.getString( "arcs" ), List.class );
            }
            else
            {
                throw new RuntimeException( "US Geodata must be present in database to run system." );
            }
        }
    }

    public CountyOverview countyDetails(Connection conn, String id) throws SQLException
    {
        try( PreparedStatement stmt = conn.prepareStatement(
                "MATCH (county:County)-[:IN_STATE]->(state) \n" +
                "WHERE county.id = {0}\n" +
                "OPTIONAL MATCH (report:VerifiedReport)-[:IN_ZIPCODE]->(zip)-[:IN_COUNTY]->(county)\n" +
                "WITH county, state, report, report.time AS time  \n" +
                "ORDER BY time DESC \n" +
                "RETURN county, state, COLLECT(report) as reports " ))
        {
            stmt.setString( 0, id );
            ResultSet res = stmt.executeQuery();

            if(res.next())
            {
                Map<String, Object> county = (Map<String,Object>) res.getObject( "county" );
                Map<String, Object> state = (Map<String,Object>) res.getObject( "state" );
                List<Map<String, Object>> reportMaps = (List<Map<String,Object>>) res.getObject( "reports" );

                List<VerifiedReport> reports = new ArrayList<>();
                for ( Map<String,Object> report : reportMaps )
                {
                    reports.add( new VerifiedReport(
                            (String)report.get( "id" ),
                            (String)report.get( "victimName" ),
                            ((Number)report.get( "victimAge" )).intValue(),
                            "N/A",
                            ((Number)report.get( "time" )).longValue(),
                            (String)report.get( "description" ) ) );
                }

                return new CountyOverview( id, (String) county.get( "name" ), (String) state.get( "name" ),
                        (String) state.get( "code" ), reports );

            }
        }

        return null;
    }
}
