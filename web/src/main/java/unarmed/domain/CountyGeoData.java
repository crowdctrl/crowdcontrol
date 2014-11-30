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

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.neo4j.helpers.collection.MapUtil.map;

public class CountyGeoData
{
    //{"type":"Polygon","arcs":[[0,1,2,3,4]],"id":"0500000US01001"}
    private static final ObjectMapper mapper = new ObjectMapper();

    private final String type;
    private final String id;
    private final List<Object> arcs;
    private final int reports;
    private final String name;

    public CountyGeoData( Map<String, Object> marshalledData, int reports ) throws IOException
    {
        type = (String) marshalledData.get("geo_type");
        id = (String) marshalledData.get( "id" );
        arcs = (List<Object>)mapper.readValue( (String) marshalledData.get( "geo_arcs" ), List.class );
        name = (String)marshalledData.get("name");
        this.reports = reports;
    }

    @JsonGetter
    public String type()
    {
        return type;
    }

    @JsonGetter
    public String id()
    {
        return id;
    }

    @JsonGetter
    public List<Object> arcs()
    {
        return arcs;
    }

    @JsonGetter
    public Map<String, Object> properties()
    {
        return map(
                "name", name,
                "numberOfReports", reports);
    }
}
