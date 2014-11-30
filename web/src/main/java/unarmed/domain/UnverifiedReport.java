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
import java.util.Map;

public class UnverifiedReport
{
    private final String id;
    private final String victimName;
    private final int victimAge;
    private final String zipCode;
    private final long occuredAtTime;
    private final long submitTime;
    private final String desc;
    private final String countyName;
    private final String links;
    private final String verifiedReportId;
    private final List<Map<String, Object>> attachments;

    public UnverifiedReport( String id, String victimName, int victimAge, String zipCode, long occuredAtTime,
            long submitTime, String desc,
            String countyName, String links,
            String verifiedReportId, List<Map<String, Object>> attachments )
    {
        this.id = id;
        this.victimName = victimName;
        this.victimAge = victimAge;
        this.zipCode = zipCode;
        this.occuredAtTime = occuredAtTime;
        this.submitTime = submitTime;
        this.desc = desc;
        this.countyName = countyName;
        this.links = links;
        this.verifiedReportId = verifiedReportId;

        this.attachments = attachments;
    }

    public String description()
    {
        return desc;
    }

    public String zip()
    {
        return zipCode;
    }

    public long occurredAtTime()
    {
        return occuredAtTime;
    }

    public long submitTime()
    {
        return submitTime;
    }

    public String id()
    {
        return id;
    }

    public String countyName() { return countyName; }

    public List<Map<String, Object>> attachments()
    {
        return attachments;
    }

    public String leadToVerifiedReport() { return verifiedReportId; }

    public String victimName()
    {
        return victimName;
    }

    public int victimAge()
    {
        return victimAge;
    }

    public String links()
    {
        return links;
    }
}
