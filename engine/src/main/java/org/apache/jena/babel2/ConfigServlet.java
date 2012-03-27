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

package org.apache.jena.babel2;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.jena.babel2.BabelReader;
import org.apache.jena.babel2.BabelWriter;
import org.apache.jena.babel2.SemanticType;
import org.apache.jena.babel2.SerializationFormat;
import org.apache.jena.babel2.util.IndentWriter;
import org.apache.jena.babel2.util.JSObject;


public class ConfigServlet extends HttpServlet {

	private static final long serialVersionUID = -3750091194974192970L;

	//final static private Logger s_logger = Logger.getLogger(ConfigServlet.class);

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		
		JSObject config = new JSObject();
		
		Map<Class<? extends SerializationFormat>, SerializationFormat> formats = new HashMap<Class<? extends SerializationFormat>, SerializationFormat>();
		Map<Class<? extends SemanticType>, SemanticType> semanticTypes = new HashMap<Class<? extends SemanticType>, SemanticType>();
		
		List<JSObject> readers = new ArrayList<JSObject>();
		for (String name : Babel.s_readers.keySet()) {
			BabelReader reader = Babel.getReader(name);
			JSObject readerO = new JSObject();
			
			SerializationFormat format = reader.getSerializationFormat();
			SemanticType semanticType = reader.getSemanticType();
			formats.put(format.getClass(), format);
			semanticTypes.put(semanticType.getClass(), semanticType);
			
			readerO.put("name", name);
			readerO.put("format", format.getClass().getName());
			readerO.put("semanticType", semanticType.getClass().getName());
			
			readers.add(readerO);
		}
		config.put("readers", readers);

		List<JSObject> writers = new ArrayList<JSObject>();
		for (String name : Babel.s_writers.keySet()) {
			BabelWriter writer = Babel.getWriter(name);
			JSObject writerO = new JSObject();
			
			SerializationFormat format = writer.getSerializationFormat();
			SemanticType semanticType = writer.getSemanticType();
			formats.put(format.getClass(), format);
			semanticTypes.put(semanticType.getClass(), semanticType);
			
			writerO.put("name", name);
			writerO.put("format", format.getClass().getName());
			writerO.put("semanticType", semanticType.getClass().getName());
			if (Babel.s_previewTemplates.containsKey(name)) {
				writerO.put("previewTemplate", Babel.s_previewTemplates.get(name));
			}
			
			writers.add(writerO);
		}
		config.put("writers", writers);
		
		JSObject formatsO = new JSObject();
		for (Class<? extends SerializationFormat> c : formats.keySet()) {
			SerializationFormat format = formats.get(c);
			
			JSObject formatO = new JSObject();
			formatO.put("name", c.getName());
			formatO.put("label", format.getLabel(null));
			formatO.put("description", format.getDescription(null));
			
			formatsO.put(c.getName(), formatO);
		}
		config.put("formats", formatsO);
		
		JSObject semanticTypesO = new JSObject();
		for (Class<? extends SemanticType> c : semanticTypes.keySet()) {
			SemanticType semanticType = semanticTypes.get(c);
			
			JSObject semanticTypeO = new JSObject();
			semanticTypeO.put("name", c.getName());
			semanticTypeO.put("label", semanticType.getLabel(null));
			semanticTypeO.put("description", semanticType.getDescription(null));
			semanticTypeO.put("supertype", c.getSuperclass().getName());
			
			semanticTypesO.put(c.getName(), semanticTypeO);
		}
		config.put("semanticTypes", semanticTypesO);
		
		IndentWriter writer = new IndentWriter(new OutputStreamWriter(response.getOutputStream()));
		writer.print("var Config = ");
		JSObject.writeObject(writer, config);
		writer.println(";");
		writer.close();
	}
}
