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
package unarmed.infrastructure.geo;

import holon.util.collection.Pair;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static holon.util.collection.Pair.pair;
import static java.util.Arrays.asList;

public class CountyLoader
{
    public static void main(String ... args) throws Throwable
    {
        Class.forName( "org.neo4j.jdbc.Driver" );
        File topoFile = new File( "web/src/main/data/us-counties.topo.json" );

        Map<String, Pair<String,String>> codeToStateAndCounty = loadCountyCodes();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree( topoFile );
        List<List<Object>> params = new ArrayList<>();
        for(JsonNode countyNode : jsonNode.get("objects").get("us_counties_20m").get("geometries"))
        {
            Map<String, Object> props = new HashMap<>();
            String id = countyNode.get("id").getTextValue().substring("0500000".length());

            Pair<String, String> stateAndName = codeToStateAndCounty.get( id );

            props.put( "id", id );
            props.put( "name", stateAndName.second() );
            props.put( "geo_type", countyNode.get("type").getTextValue() );
            props.put( "geo_arcs", countyNode.get( "arcs" ).toString() );

            params.add( asList( stateAndName.first(), props, id) );
        }
        try(Connection conn = DriverManager.getConnection( "jdbc:neo4j://localhost:7474" ))
        {
            conn.createStatement().executeQuery( "CREATE INDEX ON :County(name)" );
            conn.createStatement().executeQuery( "CREATE CONSTRAINT ON (c:County) ASSERT c.id IS UNIQUE" );
            conn.createStatement().executeQuery( "CREATE CONSTRAINT ON (c:State) ASSERT c.code IS UNIQUE" );

            PreparedStatement stmt = conn.prepareStatement(
                    "FOREACH( row in {0} | " +
                    "  MERGE (c:County {id:row[2]}) " +
                    "    ON CREATE SET c=row[1]" +
                    "  MERGE (s:State {code:row[0]})" +
                    "  MERGE (c)-[:IN_STATE]->(s) )"
            );
            stmt.setObject( 0, params );
            stmt.execute();

            // Create US geodata node
            stmt = conn.prepareStatement( "MERGE (us:Geodata {key:'US'}) ON CREATE SET us.arcs={0}" );
            stmt.setString( 0, jsonNode.get("arcs").toString());
            stmt.execute();
        }
    }

    private static Map<String, Pair<String, String>> loadCountyCodes() throws IOException
    {
        Map<String, Pair<String,String>> output = new HashMap<>();
        for ( String s : Files.readAllLines( Paths.get( "web/src/main/data/countycodes.csv" ) ) )
        {
            String[] parts = s.split( "," );
            output.put( "US" + parts[1] + parts[2], pair( parts[0], parts[3] ) );
        }
        return output;

    }
}
