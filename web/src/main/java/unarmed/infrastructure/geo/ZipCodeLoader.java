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

import au.com.bytecode.opencsv.CSVReader;

import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ZipCodeLoader
{
    public static void main(String ... args) throws Throwable
    {
        Class.forName( "org.neo4j.jdbc.Driver" );
        File zipFile = new File( "web/src/main/data/zip_codes.csv" );

        try(Connection conn = DriverManager.getConnection( "jdbc:neo4j://localhost:7474" ))
        {
            conn.createStatement().executeQuery( "CREATE INDEX ON :ZipCode(code)" );

            CSVReader reader = new CSVReader(new FileReader(zipFile));
            String [] fields;
            int loaded = 0;
            while ((fields = reader.readNext()) != null) {
                String code = fields[0];
                String state = fields[5];
                String countyName = fields[6];

                // Try to find the county
                String countyCode = findCountyId( conn, state, countyName );

                if(countyCode == null)
                {
                    System.out.println("Missing: " + state + ", " + countyName);
                }
                else
                {
                    PreparedStatement stmt = conn.prepareStatement(
                            "MATCH (s:County {id:{0}})" +
                            "MERGE (z:ZipCode {code:{1}})-[:IN_COUNTY]->(s)" );
                    stmt.setString( 0, countyCode );
                    stmt.setString( 1, code );
                    stmt.execute();
                    if(loaded++ % 100 == 0)
                    {
                        System.out.println("L: " + loaded);
                    }
                }
            }
        }
    }

    private static String findCountyId( Connection conn, String state, String countyName ) throws SQLException
    {
        PreparedStatement stmt = conn.prepareStatement(
                "MATCH (n:State {code:{0}})<-[:IN_STATE]-(county:County {name:{1}}) " +
                "RETURN county.id as id" );
        stmt.setString( 0, state );
        stmt.setString( 1, countyName.trim() );
        try ( ResultSet res = stmt.executeQuery() )
        {
            while ( res.next() )
            {
                return res.getString( "id" );
            }
        }
        return null;
    }
}
