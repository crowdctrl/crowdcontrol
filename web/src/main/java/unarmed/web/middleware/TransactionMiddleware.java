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
package unarmed.web.middleware;

import java.sql.Connection;

import javax.sql.DataSource;

import holon.api.middleware.MiddlewareHandler;
import holon.api.middleware.Pipeline;

/**
 * Wraps incoming requests in JDBC transactions.
 *
 * What's the point of middleware?
 *
 * To avoid repeating ourselves. Middleware can provide common behavior (like authentication, sessions, blah), in this
 * case, we provide transactions to the endpoints we ultimately want to invoke.
 *
 * Middleware needs to be chainable, and the chain should preferably be fairly well defined. Middleware also needs
 * to be able to provide "temporary" injectables, injectables that are made available by the middleware on a per-request
 * basis.
 *
 * Middleware could also provide things like exception handling - if something goes wrong-o, middleware can catch that
 * and return an appropriate error page.
 */
public class TransactionMiddleware
{
    private final DataSource database;

    /** Middleware constructors can ask for any component that is generally available for injection. */
    public TransactionMiddleware( DataSource database )
    {
        this.database = database;
    }

    @MiddlewareHandler
    public void handle( Pipeline pipeline ) throws Exception
    {
        Connection conn = database.getConnection();
        conn.setAutoCommit( false );
        boolean doCommit = false;

        try
        {
            pipeline.satisfyDependency( Connection.class, conn );
            pipeline.call();
            doCommit = true;
        }
        finally
        {
            if(doCommit)
            {
                conn.commit();
            }
            else
            {
                conn.rollback();
            }
            conn.close();
        }
    }
}
