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
package com.ios.tests;

import static org.junit.Assert.*;


import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.ios.Constraint;
import com.ios.PluginManager;
import com.ios.PluginManager.PluginConfiguration;
import com.ios.plugins.ChildAA;
import com.ios.plugins.ChildB;
import com.ios.plugins.ChildC;
import com.ios.plugins.Interface;
import com.ios.plugins.Parent;
import com.ios.plugins.subpack.ChildD;


public class PluginManagerTest {
	
	public static class ChildNested extends Parent {
		
	}
	
	public class ChildInner extends Parent {
		
	}
	
	@Test
	public void test() {
		PluginConfiguration conf = new PluginConfiguration();
		conf.packages.add("com.ios");
		PluginManager.initialize(conf);
		
		Set<Class> set = PluginManager.getImplementationsOf(Parent.class);
		Set<Class> real = new HashSet<>();
		real.add(ChildAA.class);
		real.add(ChildB.class);
		real.add(ChildD.class);
		real.add(ChildNested.class);
		assertEquals(4, set.size());
		assertTrue(set.containsAll(real));
		
		set = PluginManager.getImplementationsOf(Interface.class);
		real.clear();
		real.add(ChildAA.class);
		real.add(ChildC.class);
		assertEquals(2, set.size());
		assertTrue(set.containsAll(real));
		
		set = PluginManager.getValidImplementationsOf(Parent.class, new Constraint() {
			@Override
			public boolean isValid(Object o) {
				return o.getClass().getSimpleName().length() < 8;
			}
		});
		real.clear();
		real.add(ChildAA.class);
		real.add(ChildB.class);
		real.add(ChildD.class);
		assertEquals(3, set.size());
		assertTrue(set.containsAll(real));
	}

}
