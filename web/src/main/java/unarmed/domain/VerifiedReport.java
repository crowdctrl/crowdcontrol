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

public class VerifiedReport
{
    private final String id;
    private final String victimName;
    private final int victimAge;
    private final String zipCode;
    private final long time;
    private final String desc;
    private final String basedOnId;

    public VerifiedReport( String id, String victimName, int victimAge, String zipCode, long time, String desc )
    {
        this(id, victimName, victimAge, zipCode, time, desc, null);
    }

    public VerifiedReport( String id, String victimName, int victimAge, String zipCode, long time, String desc, String basedOnId )
    {
        this.id = id;
        this.victimName = victimName;
        this.victimAge = victimAge;
        this.zipCode = zipCode;
        this.time = time;
        this.desc = desc;
        this.basedOnId = basedOnId;
    }

    public String id()
    {
        return id;
    }

    public String zip()
    {
        return zipCode;
    }

    public String description()
    {
        return desc;
    }

    public long occurredAtTime()
    {
        return time;
    }

    public String basedOnUnverifiedReport()
    {
        return basedOnId;
    }

    public String victimName()
    {
        return victimName;
    }

    public int victimAge()
    {
        return victimAge;
    }
}
