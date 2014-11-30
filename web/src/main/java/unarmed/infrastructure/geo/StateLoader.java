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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class StateLoader
{
    public static void main(String ... args) throws Throwable
    {
        Class.forName( "org.neo4j.jdbc.Driver" );

        try(Connection conn = DriverManager.getConnection( "jdbc:neo4j://localhost:7474" ))
        {
            PreparedStatement stmt = conn.prepareStatement(
                    "MERGE (s:State {code:{0}}) SET s.name={1}"
            );

            for ( UsState usState : UsState.values() )
            {
                stmt.setString( 0, usState.code() );
                stmt.setString( 1, usState.name().replace( "_", " " ) );
                stmt.execute();
            }
        }
    }

}
