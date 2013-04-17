/*******************************************************************************
 * Copyright (c) 2012 Emanuele Tamponi.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Emanuele Tamponi - initial API and implementation
 ******************************************************************************/
package com.ios;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class IMap<V> extends IObject implements Map<String, V> {

	private final Class<V> valueType;
	
	private final Map<String, V> internal = new TreeMap<>();
	
	private boolean propagate = true;

	public IMap(Class<V> valueType) {
		this.valueType = valueType;
	}
	
	public IMap(Class<V> valueType, Map<String, V> internal) {
		this.valueType = valueType;
		
		this.internal.putAll(internal);
	}

	@Override
	public void clear() {
		propagate = false;
		for(String key: new HashSet<>(internal.keySet()))
			remove(key);
		propagate = true;
		propagateChange(new Property(this, ""));
	}

	@Override
	public boolean containsKey(Object key) {
		return internal.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return internal.containsValue(value);
	}

	@Override
	public Set<java.util.Map.Entry<String, V>> entrySet() {
		return internal.entrySet();
	}

	@Override
	public V get(Object key) {
		return internal.get(key);
	}

	@Override
	public boolean isEmpty() {
		return internal.isEmpty();
	}

	@Override
	public Set<String> keySet() {
		return internal.keySet();
	}

	@Override
	public V put(String key, V value) {
		V ret = internal.containsKey(key) ? internal.get(key) : null;
		
		setContent(key, value);
		
		return ret;
	}

	@Override
	public void putAll(Map<? extends String, ? extends V> other) {
		for(Entry<? extends String, ? extends V> entry: other.entrySet()) {
			setContent(entry.getKey(), entry.getValue());
		}
	}

	@Override
	public V remove(Object key) {
		Property property = new Property(this, key.toString());
		
		V oldContent = get(key);
		
		internal.remove(key);
		
		if (oldContent instanceof IObject) {
			((IObject) oldContent).getParentsLinksToThis().remove(property);
		}
		
		if (propagate)
			propagateChange(new Property(this, ""));
		
		return oldContent;
	}

	@Override
	public int size() {
		return internal.size();
	}

	@Override
	public Collection<V> values() {
		return internal.values();
	}

	@Override
	protected Object getLocal(String propertyName) {
		if (getFieldPropertyNames().contains(propertyName))
			return super.getLocal(propertyName);
		else
			return internal.get(propertyName);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void setLocal(String propertyName, Object content) {
		if (getFieldPropertyNames().contains(propertyName)) {
			super.setLocal(propertyName, content);
		} else {
			internal.put(propertyName, (V)content);
		}
	}

	@Override
	protected List<Property> getInstanceProperties() {
		List<Property> ret = super.getInstanceProperties();
		for(String key: internal.keySet())
			ret.add(new Property(this, key));
		return ret;
	}

	@Override
	public Class<?> getContentType(String propertyName, boolean runtime) {
		if (getFieldPropertyNames().contains(propertyName)) {
			return super.getContentType(propertyName, runtime);
		} else {
			if (runtime) {
				Object content = getContent(propertyName);
				return content == null ? null : content.getClass();
			} else {
				return getValueType();
			}
		}
	}
	
	public Class<V> getValueType() {
		return valueType;
	}
	
	@Override
	public String toString() {
		return internal.toString();
	}

}
