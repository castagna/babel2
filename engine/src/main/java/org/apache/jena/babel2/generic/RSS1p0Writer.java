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

package org.apache.jena.babel2.generic;

import java.io.OutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang.NotImplementedException;
import org.apache.jena.babel2.BabelWriter;
import org.apache.jena.babel2.GenericType;
import org.apache.jena.babel2.SemanticType;
import org.apache.jena.babel2.SerializationFormat;
import org.apache.jena.babel2.format.RSS1p0Format;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;


public class RSS1p0Writer implements BabelWriter {

	public String getLabel(Locale locale) {
		return "Serializes generic data to RSS 1.0";
	}

	public String getDescription(Locale locale) {
		return "Serializes generic data to RSS 1.0";
	}

	public SemanticType getSemanticType() {
		return GenericType.s_singleton;
	}

	public SerializationFormat getSerializationFormat() {
		return RSS1p0Format.s_singleton;
	}

	public boolean takesWriter() {
		return true;
	}

	public void write(OutputStream outputStream, Model model, Properties properties, Locale locale) throws Exception {
		throw new NotImplementedException();
	}

	public void write(Writer writer, Model model, Properties properties, Locale locale)
			throws Exception {
        String url = properties.getProperty("url");
        if (url == null) {
            url = "http://www.example.com/";
        }
        
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        
        Element rootElement = document.createElementNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf:RDF");
        {
            rootElement.setAttribute("xmlns", "http://purl.org/rss/1.0/");
            rootElement.setAttribute("xmlns:rss", "http://purl.org/rss/1.0/");
            rootElement.setAttribute("xmlns:dc", "http://purl.org/dc/elements/1.1/");
            rootElement.setAttribute("xmlns:rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
            rootElement.setAttribute("xmlns:rdfs", "http://www.w3.org/2000/01/rdf-schema#");
            
            document.appendChild(rootElement);
        }
        
        Element channelElement = document.createElement("channel");
        {
            channelElement.setAttribute("rdf:about", url);
            channelElement.appendChild(_createElementWithText(document, "title", "Exhibit Data"));
            channelElement.appendChild(_createElementWithText(document, "link", url));
            channelElement.appendChild(_createElementWithText(document, "description", "Exhibit data at " + url));
            
            rootElement.appendChild(channelElement);
        }
        
        List<String> itemURIs = new ArrayList<String>();
		
        
        StmtIterator iter = model.listStatements((Resource)null, RDF.type, (RDFNode)null);
        while ( iter.hasNext() ) {
        	Statement statement = iter.next();
        	Resource subject = statement.getSubject();
        	String subjectURI = subject.getURI();
        	
            Element itemElement = document.createElement("item");
            {
                itemElement.setAttribute("rdf:about", subjectURI);
                itemElement.appendChild(_createElementWithText(document, "title", _getObjectString(subject, RDFS.label, model)));
                itemElement.appendChild(_createElementWithText(document, "link", subjectURI));
                
                StringBuffer stringBuffer = new StringBuffer();
                {
                	StmtIterator iter2 = model.listStatements(subject, (Property)null, (RDFNode)null);
                	while ( iter2.hasNext() ) {
                		Statement statement2 = iter2.next();
                        RDFNode object = statement2.getObject();
                        stringBuffer.append(object.toString());
                        stringBuffer.append('\n');
                	}
                    itemElement.appendChild(_createElementWithText(document, "description", stringBuffer.toString()));
                }

                rootElement.appendChild(itemElement);
            }
            
            itemURIs.add(subjectURI);
        }
        
        Element seq = document.createElement("rdf:Seq");
        {
            
            for (String itemURI : itemURIs) {
                Element li = document.createElement("rdf:li");
                li.setAttribute("rdf:resource", itemURI);
                
                seq.appendChild(li);
            }
            
            channelElement.appendChild(seq);
        }
        
        // Write it out
        {
            TransformerFactory  transformerFactory = TransformerFactory.newInstance();
            Transformer         transformer = transformerFactory.newTransformer();
            DOMSource           source = new DOMSource(document);
            StreamResult        result = new StreamResult(writer);
            
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(source, result);
            
            writer.flush();
        }
	}

    static protected Element _createElementWithText(Document document, String tagName, String text) {
        Element element = document.createElement(tagName);
        element.setTextContent(text);
        return element;
    }
    
    static protected String _getObjectString(Resource subject, Property predicate, Model model) {
        RDFNode v = _getObject(subject, predicate, model);
        return v.isLiteral() ? v.asLiteral().getLexicalForm() : null;
    }
    
    static protected RDFNode _getObject(Resource subject, Property predicate, Model model) {
    	NodeIterator iter = model.listObjectsOfProperty(subject, predicate);
        try {
            if (iter.hasNext()) {
                return iter.next();
            } else {
                return null;
            }
        } finally {
            iter.close();
        }
    }
}
