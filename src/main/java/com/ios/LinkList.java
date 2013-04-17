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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoCopyable;

public class LinkList extends ArrayList<Property> implements KryoCopyable<LinkList> {

	public LinkList() {
		
	}
	
	public LinkList(Collection collection) {
		super(collection);
	}

	@Override
	public LinkList copy(Kryo kryo) {
		List<Property> temp = new ArrayList<>(this);
		IObject root = (IObject) kryo.getContext().get("root");
		Set<IObject> descendents = (Set<IObject>) kryo.getContext().get("descendents");
		Set<IObject> nondescendents = (Set<IObject>) kryo.getContext().get("nondescendents");
		if (root != null) {
			for(Property property: this) {
				if (!isAncestor(root, property.getRoot(), descendents, nondescendents))
					temp.remove(property);
			}
		}
		LinkList ret = new LinkList(kryo.copy(temp));
		return ret;
	}

	private static boolean isAncestor(IObject root, IObject object, Set<IObject> descendents, Set<IObject> nondescendents) {
		if (descendents.contains(object))
			return true;
		if (nondescendents.contains(object))
			return false;
		return recursiveIsAncestor(root, object, descendents, nondescendents, new HashSet<Property>());
	}
	
	private static boolean recursiveIsAncestor(IObject root, IObject object, Set<IObject> descendents, Set<IObject> nondescendents, Set<Property> seen) {
		if (root == object) {
			descendents.add(object);
			return true;
		}
		for (Property linkToThis : object.parentsLinkToThis) {
			if (seen.contains(linkToThis))
				continue;
			seen.add(linkToThis);
			if (recursiveIsAncestor(root, linkToThis.getRoot(), descendents, nondescendents, seen))
				return true;
		}
		nondescendents.add(object);
		return false;
	}
	
}
