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

import java.util.HashMap;
import java.util.Map;

public enum UsState
{
    Alabama("AL"),
    Alaska("AK"),
    Arizona("AZ"),
    Arkansas("AR"),
    California("CA"),
    Colorado("CO"),
    Connecticut("CT"),
    Delaware("DE"),
    Florida("FL"),
    Georgia("GA"),
    Hawaii("HI"),
    Idaho("ID"),
    Illinois("IL"),
    Indiana("IN"),
    Iowa("IA"),
    Kansas("KS"),
    Kentucky("KY"),
    Louisiana("LA"),
    Maine("ME"),
    Maryland("MD"),
    Massachusetts("MA"),
    Michigan("MI"),
    Minnesota("MN"),
    Mississippi("MS"),
    Missouri("MO"),
    Montana("MT"),
    Nebraska("NE"),
    Nevada("NV"),
    New_Hampshire("NH"),
    New_Jersey("NJ"),
    New_Mexico("NM"),
    New_York("NY"),
    North_Carolina("NC"),
    North_Dakota("ND"),
    Ohio("OH"),
    Oklahoma("OK"),
    Oregon("OR"),
    Pennsylvania("PA"),
    Rhode_Island("RI"),
    South_Carolina("SC"),
    South_Dakota("SD"),
    Tennessee("TN"),
    Texas("TX"),
    Utah("UT"),
    Vermont("VT"),
    Virginia("VA"),
    Washington("WA"),
    West_Virginia("WV"),
    Wisconsin("WI"),
    Wyoming("WY");

    private static Map<String, UsState> codeLookup = new HashMap<>();
    private static Map<String, UsState> nameLookup = new HashMap<>();

    static
    {
        for ( UsState state : UsState.values() )
        {
            codeLookup.put( state.code, state );
            nameLookup.put( state.name().toLowerCase().replace( "_", " " ), state );
        }
    }

    private final String code;

    private UsState( String code )
    {
        this.code = code;
    }

    public static UsState lookup(String codeOrName)
    {
        if(codeOrName.length() == 2)
        {
            return codeLookup.get( codeOrName );
        }
        else
        {
            return nameLookup.get( codeOrName.toLowerCase() );
        }
    }

    public String code()
    {
        return code;
    }
}
