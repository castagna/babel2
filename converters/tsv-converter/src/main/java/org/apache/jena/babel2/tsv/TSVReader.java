/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jena.babel2.tsv;

import java.io.InputStream;
import java.io.LineNumberReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.apache.jena.babel2.BabelReader;
import org.apache.jena.babel2.GenericType;
import org.apache.jena.babel2.SemanticType;
import org.apache.jena.babel2.SerializationFormat;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class TSVReader implements BabelReader {
    static class Column {
        String      m_name;
        Property    m_uri;
        boolean     m_singleValue = false;
        ValueType   m_valueType = ValueType.Text;
    }
    static class Item {
        String      m_label;
        String      m_id;
        Resource    m_type;
        Resource    m_uri;
        Map<Column, List<String>>   m_properties = new HashMap<Column, List<String>>();
    }
    static enum ValueType {
        Item,
        Text,
        Number,
        Boolean,
        Date,
        URL
    }
    
    public String getDescription(Locale locale) {
        return "Tab-separated value reader";
    }

    public String getLabel(Locale locale) {
        return "TSV Reader";
    }

    public SemanticType getSemanticType() {
        return GenericType.s_singleton;
    }

    public SerializationFormat getSerializationFormat() {
        return TSVFormat.s_singleton;
    }

    public boolean takesReader() {
        return true;
    }
    
    public void read(InputStream inputStream, Model model, Properties properties, Locale locale) throws Exception {
        throw new NotImplementedException();
    }

    public void read(Reader reader, Model model, Properties properties, Locale locale) throws Exception {
        String              namespace = properties.getProperty("namespace");
        List<Column>        columns = new ArrayList<Column>();
        int                 uriColumn = -1;
        int                 idColumn = -1;
        int                 labelColumn = -1;
        int                 typeColumn = -1;
        
        LineNumberReader    lineReader = new LineNumberReader(reader);
        String              line;
        
        /*
         * Find the header row
         */
        while ((line = lineReader.readLine()) != null) {
            line = line.trim();
            if (line.length() > 0) {
                String[] columnSpecs = StringUtils.splitPreserveAllTokens(line, '\t');
                
                for (int i = 0; i < columnSpecs.length; i++) {
                    String spec = columnSpecs[i].trim();
                    Column column = null;
                    if (spec.length() > 0) {
                        column = new Column();
                        
                        int colon = spec.indexOf(':');
                        if (colon < 0) {
                            column.m_name = spec;
                        } else {
                            column.m_name = spec.substring(0, colon).trim();
                            
                            String[] details = StringUtils.splitPreserveAllTokens(spec.substring(colon + 1), ',');
                            for (int d = 0; d < details.length; d++) {
                                String detail = details[d].trim().toLowerCase();
                                if ("single".equals(detail)) {
                                    column.m_singleValue = true;
                                } else if ("item".equals(detail)) {
                                    column.m_valueType = ValueType.Item;
                                } else if ("number".equals(detail)) {
                                    column.m_valueType = ValueType.Number;
                                } else if ("boolean".equals(detail)) {
                                    column.m_valueType = ValueType.Boolean;
                                } else if ("date".equals(detail)) {
                                    column.m_valueType = ValueType.Date;
                                } else if ("url".equals(detail)) {
                                    column.m_valueType = ValueType.URL;
                                }
                            }
                        }
                        
                        /*
                         * The user might capitalize the column name in all sorts
                         * of way. Make sure we are insensitive to the capitalization.
                         */
                        if (column.m_name.equalsIgnoreCase("uri")) {
                            column.m_name = "uri";
                            uriColumn = i;
                        } else if (column.m_name.equalsIgnoreCase("type")) {
                            column.m_name = "type";
                            typeColumn = i;
                        } else if (column.m_name.equalsIgnoreCase("label")) {
                            column.m_name = "label";
                            labelColumn = i;
                        } else if (column.m_name.equalsIgnoreCase("id")) {
                            column.m_name = "id";
                            idColumn = i;
                        } else {
                            column.m_uri = ResourceFactory.createProperty(namespace, encode(column.m_name));
                        }
                    }
                    columns.add(column);
                }
                break;
            }
        }
        
        /*
         * Try to use the first non-null column as the label column 
         * if we still haven't found the label column.
         */
        if (labelColumn < 0) {
            for (int i = 0; i < columns.size(); i++) {
                if (columns.get(i) != null) {
                    labelColumn = i;
                    break;
                }
            }
        }
        
        if (labelColumn >= 0) {
            Map<String, Item> idToItem = new HashMap<String, Item>();
            
            /*
             * The first pass will collect all the items and
             * their properties as well as assign URIs to them.
             */
            while ((line = lineReader.readLine()) != null) {
                line = line.trim();
                if (line.length() > 0) {
                    String[] fields = StringUtils.splitPreserveAllTokens(line, '\t');
                    String label = fields[labelColumn].trim();
                    if (label == null || label.length() == 0) {
                        continue;
                    }
                    
                    String id = idColumn < 0 ? label : fields[idColumn].trim();
                    if (id.length() == 0) {
                        id = label;
                    }
                    
                    String uri = uriColumn < 0 ? null : fields[uriColumn].trim();
                    if (uri == null || uri.length() == 0) {
                        uri = namespace + encode(id);
                    }

                    String type = typeColumn < 0 ? "Item" : fields[typeColumn].trim();
                    if (type.length() == 0) {
                        type = "Item";
                    }

                    Item item = idToItem.get(id);
                    if (item == null) {
                        item = new Item();
                        item.m_id = id;
                        item.m_uri = ResourceFactory.createResource(uri);
                        item.m_label = label;
                        item.m_type = ResourceFactory.createResource(namespace + encode(type));
                        
                        idToItem.put(id, item);
                    }
                    
                    for (int f = 0; f < fields.length; f++) {
                        Column column = columns.get(f);
                        String field = fields[f].trim();
                        
                        if (column != null && column.m_uri != null && field.length() > 0) {
                            List<String> cells = item.m_properties.get(column);
                            if (cells == null) {
                                cells = new LinkedList<String>();
                                item.m_properties.put(column, cells);
                            }
                            cells.add(field);
                        }
                    }
                }
            }
                
            for (Item item : idToItem.values()) {
                model.add(item.m_uri, RDF.type, item.m_type);
                model.add(item.m_uri, RDFS.label, item.m_label);
                model.add(item.m_uri, ResourceFactory.createProperty("http://simile.mit.edu/2006/11/exhibit#", "id"), item.m_id);

                for (Column column : item.m_properties.keySet()) {
                    if (column.m_uri != null) {
                        List<String> cells = item.m_properties.get(column);
                        if (cells != null) {
                            for (String cell : cells) {
                                if (column.m_singleValue) {
                                    addStatement(model, item.m_uri, column.m_uri, cell, column.m_valueType, idToItem, namespace);
                                } else {
                                    String[] values = StringUtils.splitPreserveAllTokens(cell, ';');
                                    for (String value : values) {
                                        addStatement(model, item.m_uri, column.m_uri, value.trim(), column.m_valueType, idToItem, namespace);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    protected void addStatement(
        Model      			model, 
        Resource            subject, 
        Property            predicate, 
        String              object, 
        ValueType           valueType, 
        Map<String, Item>   idToItem,
        String              namespace
    ) {
        RDFNode v = null;
        if (valueType == ValueType.Item) {
            Item item = idToItem.get(object);
            if (item != null) {
                v = item.m_uri;
            } else {
                v = ResourceFactory.createResource(namespace + encode(object));
            }
        } else if (valueType.equals(ValueType.Boolean)) {
        	v = model.createTypedLiteral(new Boolean(object));
        } else if (valueType.equals(ValueType.Number)) {
            try {
                v = model.createTypedLiteral(Long.parseLong(object));
            } catch (NumberFormatException nfe) {
                try {
                    v = model.createTypedLiteral(Double.parseDouble(object));
                } catch (NumberFormatException nfe2) {
                }
            }
        } else if (valueType.equals(ValueType.Date)) {
            /**
             * TODO: How do we convert an arbitrary string to an ISO8601 date/time?
             */
        	v = model.createTypedLiteral(object, XSDDatatype.XSDdateTime);
        }
        
        if (v == null) {
            v = model.createLiteral(object);
        }
        model.add(subject, predicate, v);
    }
    
    private static final String s_urlEncoding = "UTF-8";
    private static final URLCodec s_codec = new URLCodec();
    
    static String encode(String s) {
        try {
            return s_codec.encode(s, s_urlEncoding);
        } catch (Exception e) {
            throw new RuntimeException("Exception encoding " + s + " with " + s_urlEncoding + " encoding.");
        }
    }
}
