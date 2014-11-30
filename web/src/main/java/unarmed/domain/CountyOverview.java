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

import java.util.List;

public class CountyOverview
{
    private final String id;
    private final String name;
    private final String stateName;
    private final String stateCode;
    private final List<VerifiedReport> reports;

    public CountyOverview( String id, String name, String stateName, String stateCode, List<VerifiedReport> reports )
    {
        this.id = id;
        this.name = name;
        this.stateName = stateName;
        this.stateCode = stateCode;
        this.reports = reports;
    }

    public String id()
    {
        return id;
    }

    public String name()
    {
        return name;
    }

    public String stateName()
    {
        return stateName;
    }

    public String stateCode()
    {
        return stateCode;
    }

    public List<VerifiedReport> reports()
    {
        return reports;
    }

    @Override
    public String toString()
    {
        return "CountyOverview{" +
               "id='" + id + '\'' +
               ", name='" + name + '\'' +
               ", stateName='" + stateName + '\'' +
               ", stateCode='" + stateCode + '\'' +
               ", reports=" + reports +
               '}';
    }
}
