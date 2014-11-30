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
package unarmed.domain.subscriptions;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Subscriptions
{
    public void createTentative( Connection conn, boolean subscribeToReports, boolean subscribeToNewsletter,
            String zipFilter, String stateFilter,
            String email ) throws SQLException
    {
        PreparedStatement stmt =
                conn.prepareStatement( "CREATE (n:TentativeEmailSubscription {email:{0}}) " +
                                       "SET n.reports = {1} " +
                                       "SET n.newsletter = {2} " +
                                       "SET n.zipFilter = {3} " +
                                       "SET n.stateFilter = {4} " );
        stmt.setString( 0, email );
        stmt.setBoolean( 1, subscribeToReports );
        stmt.setBoolean( 2, subscribeToNewsletter );
        stmt.setString( 3, zipFilter );
        stmt.setString( 4, stateFilter );
        stmt.execute();
    }
}
