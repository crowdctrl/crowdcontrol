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
package unarmed.domain.auth;


import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import javax.sql.DataSource;

public class Users
{
    private final DataSource ds;
    private MessageDigest hashMethod;

    public Users(DataSource ds) throws NoSuchAlgorithmException
    {
        this.ds = ds;
        this.hashMethod = MessageDigest.getInstance( "SHA-256" );
    }

    public boolean validateLogin( Connection conn, String email, String password ) throws SQLException
    {
        try ( PreparedStatement stmt = conn.prepareStatement( "MATCH (n:User {email:{0}}) RETURN n.password, n.salt" ) )
        {
            stmt.setString( 0, email );
            ResultSet resultSet = stmt.executeQuery();
            while(resultSet.next())
            {
                String storedHash = resultSet.getString( "n.password" );
                String salt = resultSet.getString( "n.salt" );
                String hash = hash( password, salt );
                return hash.equals( storedHash );
            }
        }
        return false;
    }

    public void ensureAdminExists( String adminEmail, String adminPassword ) throws SQLException
    {
        try(Connection conn = ds.getConnection())
        {
            PreparedStatement statement = conn.prepareStatement( "MERGE (u:User {email:{0}}) ON CREATE SET u.password={1}, u.salt={2}" );
            String salt = newSalt();
            statement.setString( 0, adminEmail );
            statement.setString( 1, hash( adminPassword, salt ) );
            statement.setString( 2, salt );
            statement.execute();
        }
    }

    private String hash( String pwd, String salt )
    {
        return hex( hashMethod.digest( (pwd + salt).getBytes( Charset.forName( "UTF-8" ) ) ) );
    }

    private String hex( byte[] bytes )
    {
        StringBuilder sb = new StringBuilder();
        for ( byte b : bytes )
        {
            sb.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }

    private String newSalt()
    {
        return UUID.randomUUID().toString();
    }
}
