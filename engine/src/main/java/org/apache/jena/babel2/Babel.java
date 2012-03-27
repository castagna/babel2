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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.jena.babel2.BabelReader;
import org.apache.jena.babel2.BabelWriter;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class Babel {

    final static public Map<String, String> s_readers = new HashMap<String, String>();
    final static public Map<String, String> s_writers =  new HashMap<String, String>();

    final static public Map<String, String> s_readersFromMimeType = new HashMap<String, String>();
    final static public Map<String, String> s_writersFromMimeType =  new HashMap<String, String>();

    final static public Map<String, String> s_previewTemplates = new HashMap<String, String>();

    static {
        s_readers.put("rdf-xml", "org.apache.jena.babel2.generic.RdfXmlConverter");
        s_readers.put("turtle", "org.apache.jena.babel2.generic.TurtleConverter");
        s_readers.put("tsv", "org.apache.jena.babel2.tsv.TSVReader");
//        s_readers.put("xls", "org.apache.jena.babel2.xls.XLSReader");
//        s_readers.put("bibtex", "org.apache.jena.babel2.bibtex.BibtexReader");
//        s_readers.put("exhibit-json", "org.apache.jena.babel2.exhibit.ExhibitJsonReader");
//        s_readers.put("exhibit-html", "org.apache.jena.babel2.exhibit.ExhibitWebPageReader");
//        s_readers.put("jpeg", "org.apache.jena.babel2.jpeg.JPEGReader");
//        s_readers.put("kml", "org.apache.jena.babel2.kml.KMLReader");

        s_readersFromMimeType.put("application/rdf+xml", "org.apache.jena.babel2.generic.RdfXmlConverter");
        s_readersFromMimeType.put("application/rdf+n3", "org.apache.jena.babel2.generic.N3Converter");
        s_readersFromMimeType.put("application/rdf+turtle", "org.apache.jena.babel2.generic.N3Converter");
        s_readersFromMimeType.put("text/tab-separated-values", "org.apache.jena.babel2.tsv.TSVReader");
//        s_readersFromMimeType.put("application/vnd.ms-excel", "org.apache.jena.babel2.xls.XLSReader");
//        s_readersFromMimeType.put("text/x-bibtex", "org.apache.jena.babel2.bibtex.BibtexReader");
//        s_readersFromMimeType.put("application/json+exhibit", "org.apache.jena.babel2.exhibit.ExhibitJsonReader");
//        s_readersFromMimeType.put("text/html+exhibit", "org.apache.jena.babel2.exhibit.ExhibitWebPageReader");
//        s_readersFromMimeType.put("image/jpeg", "org.apache.jena.babel2.jpeg.JPEGReader");
//        s_readersFromMimeType.put("application/vnd.google-earth.kml+xml", "org.apache.jena.babel2.kml.KMLReader");
        
        s_writers.put("rdf-xml", "org.apache.jena.babel2.generic.RdfXmlConverter");
        s_writers.put("turtle", "org.apache.jena.babel2.generic.TurtleConverter");
        s_writers.put("rss1.0", "org.apache.jena.babel2.generic.RSS1p0Writer");
//        s_writers.put("exhibit-json", "org.apache.jena.babel2.exhibit.ExhibitJsonWriter");
//        s_writers.put("exhibit-jsonp", "org.apache.jena.babel2.exhibit.ExhibitJsonpWriter");
//        s_writers.put("bibtex-exhibit-json", "org.apache.jena.babel2.exhibit.BibtexExhibitJsonWriter");
//        s_writers.put("bibtex-exhibit-jsonp", "org.apache.jena.babel2.exhibit.BibtexExhibitJsonpWriter");
        s_writers.put("text", "org.apache.jena.babel2.generic.TextWriter");

        s_writersFromMimeType.put("application/rdf+xml", "org.apache.jena.babel2.generic.RdfXmlConverter");
        s_writersFromMimeType.put("application/rdf+n3", "org.apache.jena.babel2.generic.TurtleConverter");
        s_writersFromMimeType.put("application/rdf+turtle", "org.apache.jena.babel2.generic.TurtleConverter");
//        s_writersFromMimeType.put("application/json+exhibit", "org.apache.jena.babel2.exhibit.ExhibitJsonWriter");
//        s_writersFromMimeType.put("application/jsonp+exhibit", "org.apache.jena.babel2.exhibit.ExhibitJsonpWriter");
        
        s_previewTemplates.put("exhibit-json", "exhibit.vt");
        s_previewTemplates.put("bibtex-exhibit-json", "bibtex-exhibit.vt");
    }

    static public BabelReader getReader(String name) {
        try {
            return (BabelReader) Class.forName(s_readers.get(name)).newInstance();
        } catch (Exception e) {
            return null;
        }
    }

    static public BabelWriter getWriter(String name) {
        try {
            return (BabelWriter) Class.forName(s_writers.get(name)).newInstance();
        } catch (Exception e) {
            return null;
        }
    }
    
    static public BabelReader getReaderFromMimeType(String mimeType) {
        try {
            return (BabelReader) Class.forName(s_readersFromMimeType.get(mimeType)).newInstance();
        } catch (Exception e) {
            return null;
        }
    }

    static public BabelWriter getWriterFromMimeType(String mimeType) {
        try {
            return (BabelWriter) Class.forName(s_writersFromMimeType.get(mimeType)).newInstance();
        } catch (Exception e) {
            return null;
        }
    }
    
    static public void main(String[] args) throws Exception {

        File input_file = null;
        String input_encoding = "ISO-8859-1";
        String input_format = null;

        File output_file = null;
        String output_encoding = "ISO-8859-1";
        String output_format = "exhibit-json";

        CommandLineParser parser = new PosixParser();

        Options options = new Options();
        options.addOption( "h", "help", false, "show this help screen" );
        options.addOption( "i", "input-encoding <name>", true, "the input file encoding (default: " + input_encoding + ")");
        options.addOption( "o", "output-encoding <name>", true, "the output file encoding (default: " + output_encoding + ")");
        
        try {
            CommandLine line = parser.parse(options, args);
            String[] clean_args = line.getArgs();
                                                
            if (line.hasOption("h") || clean_args.length < 2) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("babel [options] input_file input_format (output_file output_format)", options);
                System.exit(1);
            }
            
            if (line.hasOption("i")) {
                input_encoding = line.getOptionValue("i");
            }
            
            if (line.hasOption("o")) {
                output_encoding = line.getOptionValue("o");
            }
            
            input_file = new File(clean_args[0]);
            if (!input_file.exists()) fatal("Can't find the input file '" + input_file + "'.");
            if (!input_file.canRead()) fatal("You don't have permission to read from the input file '" + input_file + "'.");
            
            input_format = clean_args[1];

            if (clean_args.length > 2) {
                output_file = new File(clean_args[2]);
                if (!output_file.exists()) output_file.mkdirs();
            }
            
            if (clean_args.length > 3) {
                output_format = clean_args[3];
            }

        } catch (Exception e) {
            fatal("Error found initializing: " + e.getMessage());
        }
        
        if (input_encoding == null) {
            input_encoding = "ISO-8859-1";
        }
        
        if (output_encoding == null) {
            output_encoding = input_encoding;
        }
        
        BabelReader babelReader = Babel.getReader(input_format);
        BabelWriter babelWriter = Babel.getWriter(output_format);
        
        Model model = ModelFactory.createDefaultModel();

        Properties properties = new Properties();
        properties.setProperty("namespace", "urn:babel:");
        properties.setProperty("url", "urn:babel:/");
        
        Locale locale = Locale.getDefault();
        
        InputStream input = null;
        OutputStream output = null;

        try {
            input = new FileInputStream(input_file);
            output = (output_file == null) ? System.out : new FileOutputStream(output_file); 
            
            if (babelReader.takesReader()) {
                Reader reader = new BufferedReader(new InputStreamReader(input,input_encoding));
                Writer writer = new OutputStreamWriter(output, output_encoding);
                babelReader.read(reader, model, properties, locale);
                babelWriter.write(writer, model, properties, locale);
            } else {
                babelReader.read(input, model, properties, locale);
                babelWriter.write(output, model, properties, locale);
            }
        } finally {
            output.close();
            input.close();
        }
    }
    
    private static void fatal(String msg) {
        System.err.println(msg);
        System.exit(-1);
    }
}
