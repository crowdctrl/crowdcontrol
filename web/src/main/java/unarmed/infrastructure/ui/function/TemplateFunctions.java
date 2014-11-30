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
package unarmed.infrastructure.ui.function;

import com.google.common.base.Function;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Helper functions used when rendering templates.
 */
public class TemplateFunctions
{
    public static final DateFormatFunction dateFormat = new DateFormatFunction();

    public static class DateFormatFunction implements Function<String, String>
    {
        @Override
        public String apply( String def )
        {
            String[] parts = def.split( "\\|" );
            if(parts.length != 2)
            {
                return "[ERROR: Expected 2 parameters, format|timestamp]";
            }
            String format = parts[0];
            long timestamp = Long.parseLong( parts[1].trim() );
            return new SimpleDateFormat(format).format( new Date(timestamp) );
        }
    }
}
