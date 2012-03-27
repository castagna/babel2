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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A map from keys to a list of objects
 */
@SuppressWarnings("rawtypes")
public class ListMap extends HashMap<Object,Object> {
	
    private static final long serialVersionUID = 3856407370527187912L;

	public boolean check(Object key, Object value){
		List val = (List) super.get(key);
		if (val == null)
			return false;
		for(int i = 0; i<val.size(); i++)
			if(value.equals(val.get(i)))
				return true;
		return false;
	}

	public int count(Object key){
		List val = (List) super.get(key);
		return (val == null)
			? 0
			: ((List) val).size();
	}

	public Object get(Object key){
		List val = (List) super.get(key);
		return (val == null)
			? null
			: ((List) val).get(0);
	}

	public Object get(Object key, int index){
		List val = (List) super.get(key);
		return (val == null)
			? null
			: ((List) val).get(index);
	}

	public Object put(Object key, Object value){
		List val = (List) super.get(key);
		if(val == null)
			val = new ArrayList();
		val.add(value);
		return super.put(key, val);
	}
}