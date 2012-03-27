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

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.Locale;
import java.util.Properties;

import org.apache.commons.lang.NotImplementedException;
import org.apache.jena.babel2.BabelReader;
import org.apache.jena.babel2.BabelWriter;
import org.apache.jena.babel2.GenericType;
import org.apache.jena.babel2.SemanticType;
import org.apache.jena.babel2.SerializationFormat;
import org.apache.jena.babel2.format.RdfXmlFormat;

import com.hp.hpl.jena.rdf.model.Model;

public class RdfXmlConverter implements BabelReader, BabelWriter {

	public String getLabel(Locale locale) {
		return "Serializes generic data to RDF/XML";
	}

	public String getDescription(Locale locale) {
		return "Serializes generic data to RDF/XML";
	}

	public SemanticType getSemanticType() {
		return GenericType.s_singleton;
	}

	public SerializationFormat getSerializationFormat() {
		return RdfXmlFormat.s_singleton;
	}

	public boolean takesReader() {
		return true;
	}
	
	public void read(InputStream inputStream, Model model, Properties properties, Locale locale) throws Exception {
		throw new NotImplementedException();
	}

	public void read(Reader reader, Model model, Properties properties, Locale locale) throws Exception {
		model.read(reader, properties.getProperty("namespace"), "RDF/XML");
	}

	public boolean takesWriter() {
		return true;
	}
	
	public void write(OutputStream outputStream, Model model, Properties properties, Locale locale) throws Exception {
		throw new NotImplementedException();
	}

	public void write(Writer writer, Model model, Properties properties, Locale locale) throws Exception {
		model.write(writer, "RDF/XML");
	}

}
