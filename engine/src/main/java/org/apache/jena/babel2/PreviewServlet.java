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

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.jena.babel2.BabelWriter;
import org.apache.jena.babel2.util.Util;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;

public class PreviewServlet extends TranslatorServlet {
	private static final long serialVersionUID = -2862110707968976815L;

	//final static private Logger s_logger = Logger.getLogger(PreviewServlet.class);
	
	private VelocityEngine m_ve;
	
	@Override
	public void init() throws ServletException {
		super.init();
		
        try {
    		File webapp = new File(getServletContext().getRealPath("/"));
    		
            Properties velocityProperties = new Properties();
            velocityProperties.setProperty(
                    RuntimeConstants.FILE_RESOURCE_LOADER_PATH, 
                    new File(new File(webapp, "WEB-INF"), "templates").getAbsolutePath());
    		
            m_ve = new VelocityEngine();
			m_ve.init(velocityProperties);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String template = null;
        
        String[] params = StringUtils.splitPreserveAllTokens(request.getQueryString(), '&');
        for (int i = 0; i < params.length; i++) {
            String param = params[i];
            int equalIndex = param.indexOf('=');

            if (equalIndex >= 0) {
                String name = param.substring(0, equalIndex);
                if ("template".equals(name)) {
                	template = Util.decode(param.substring(equalIndex + 1));
                	break;
                }
            }
        }

        StringWriter writer = new StringWriter();
		try {
			ResponseInfo responseInfo = internalService(request, response, params, writer);
			if (responseInfo.m_status != HttpServletResponse.SC_OK) {
				writeBufferedResponse(response, writer, responseInfo);
			} else {
	            VelocityContext vcContext = new VelocityContext();
		            
	            vcContext.put("data", writer.toString());
	            vcContext.put("utilities", new PreviewUtilities());
		            
	    		response.setCharacterEncoding("UTF-8");
	    		response.setContentType("text/html");
	    		response.setStatus(HttpServletResponse.SC_OK);
	    		
	            m_ve.mergeTemplate(template, vcContext, response.getWriter());
			}
		} catch (Exception e) {
			writeError(writer, "Internal error", e);
		} finally {
			writer.close();
		}
	}
	
	@Override
	protected void setContentEncodingAndMimetype(
			ResponseInfo responseInfo, BabelWriter writer, String mimetype) {
		responseInfo.m_contentEncoding = "UTF-8";
		responseInfo.m_mimeType = "text/html";
	}
}
