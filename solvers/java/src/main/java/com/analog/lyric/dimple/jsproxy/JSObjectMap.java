/*******************************************************************************
*   Copyright 2014 Analog Devices, Inc.
*
*   Licensed under the Apache License, Version 2.0 (the "License");
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
********************************************************************************/

package com.analog.lyric.dimple.jsproxy;

import java.applet.Applet;
import java.util.AbstractMap;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import netscape.javascript.JSException;
import netscape.javascript.JSObject;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * A read-only Map view of a JSObject.
 * <p>
 * This class provides a way to wrap a {@link JSObject} instance representing a Javascript object as
 * a Map object.
 * <p>
 * @since 0.07
 * @author Christopher Barber
 */
@NonNullByDefault(false)
public class JSObjectMap extends AbstractMap<String, Object>
{
	private final Applet _applet;
	private final JSObject _obj;

	/*--------------
	 * Construction
	 */
	
	JSObjectMap(Applet applet, JSObject obj)
	{
		_applet = applet;
		_obj = obj;
	}

	/*----------------
	 * Object methods
	 */
	
	@Override
	public boolean equals(Object other)
	{
		return other instanceof JSObjectMap && _obj.equals(((JSObjectMap)other)._obj);
	}
	
	@Override
	public int hashCode()
	{
		return _obj.hashCode();
	}
	
	@Override
	public String toString()
	{
		return _obj.toString();
	}
	
	/*-------------
	 * Map methods
	 */
	
	@Override
	public boolean containsKey(Object key)
	{
		return get(key) != null;
	}
	
	@Override
	public Object get(Object key)
	{
		if (key instanceof String)
		{
			try
			{
				return _obj.getMember((String)key);
			}
			catch (Exception ex)
			{
			}
		}
		return null;
	}
	
	@Override
	public Set<String> keySet()
	{
		final JSObject jsObject = _obj;
		final Applet applet = _applet;
		
		// Retrieving global context - a JSObject representing a window applet belongs to
		JSObject globalContext;
		try
		{
			globalContext = JSObject.getWindow(applet);
		}
		catch (JSException ex)
		{
			return Collections.emptySet();
		}

		// Checking whether passed object is not an array
		try
		{
			jsObject.getSlot(0);
			return Collections.emptySet();
		}
		catch (JSException e) {

		}

		String keysFunctionName = String.format("_getKeys%d", Calendar.getInstance().getTimeInMillis());
		jsObject.eval("window['" + keysFunctionName + "'] = function(jsObject) { return Object.keys(jsObject) }");
		JSObject propertiesNamesJsObject = (JSObject)globalContext.call(keysFunctionName, new Object[] { jsObject });
		jsObject.eval("delete(window['" + keysFunctionName + "'])");

		Set<String> propertiesNames = new TreeSet<>();
		try
		{
			int slotIndex = 0;
			while (true)
			{
				Object propertyName = propertiesNamesJsObject.getSlot(slotIndex);
				if (propertyName instanceof String)
				{
					propertiesNames.add((String)propertyName);
				}
				slotIndex++;
			}
		}
		catch (JSException e) {

		}

		return Collections.unmodifiableSet(propertiesNames);
	}
	
	private static enum EntryComparator implements Comparator<Map.Entry<String,Object>>
	{
		INSTANCE;
		
		@Override
		public int compare(Map.Entry<String, Object> entry1, Map.Entry<String, Object> entry2)
		{
			return entry1.getKey().compareTo(entry2.getKey());
		}
	}
	
	@Override
	public Set<Map.Entry<String, Object>> entrySet()
	{
		Set<String> keys = keySet();
		Set<Map.Entry<String,Object>> entries = new TreeSet<>(EntryComparator.INSTANCE);
		for (String key : keys)
		{
			entries.add(new SimpleImmutableEntry<>(key, get(key)));
		}
		return Collections.unmodifiableSet(entries);
	}
}
