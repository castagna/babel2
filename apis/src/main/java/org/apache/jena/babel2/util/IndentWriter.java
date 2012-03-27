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

package org.apache.jena.babel2.util;

import java.io.IOException;
import java.io.Writer;

public class IndentWriter {
    final static private int s_max = 20;
    
    static private String[] s_indents = new String[s_max];
    static {
        for (int i = 0; i < s_max; i++) {
            StringBuffer sb = new StringBuffer(s_max);
            for (int j = 0; j < i; j++) {
                sb.append('\t');
            }
            s_indents[i] = sb.toString();
        }
    }
    
    private Writer		m_writer;
    private int         m_count = 0;
    private boolean     m_indent = true;
    
    public IndentWriter(Writer writer) {
    	m_writer = writer;
    }
    
    public void close() throws IOException {
        m_writer.close();
    }
    
    public void flush() throws IOException {
    	m_writer.flush();
    }
    
    public void print(Object o) throws IOException {
        printIndent();
        m_writer.write(o.toString());
        m_indent = false;
    }
    
    public void println() throws IOException {
        printIndent();
        m_writer.write("\n");
        m_indent = true;
    }
    
    public void println(Object o) throws IOException  {
        printIndent();
        m_writer.write(o.toString());
        m_writer.write("\n");
        m_indent = true;
    }
    
    public void indent() {
        m_count++;
    }
    
    public void unindent() {
        m_count--;
    }
    
    private void printIndent() throws IOException {
        if (m_indent) {
            m_writer.write(s_indents[m_count]);
        }
    }
}
