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

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.junit.Test;

import com.ios.IObject;
import com.ios.PluginManager;
import com.ios.PluginManager.PluginConfiguration;
import com.ios.triggers.MasterSlaveTrigger;


public class IObjectTest {
	
	static {
		PluginManager.initialize(new PluginConfiguration());
	}
	
	public static class Node extends IObject {
		public Node right, down;
		
		public String content;
		
		public String contentCopy;
		
		public String nonConnected;
		
		public Node() {
			addTrigger(new MasterSlaveTrigger(this, "content", "right.content", "contentCopy"));
		}
		
		public Node(boolean downConnected) {
			addTrigger(new MasterSlaveTrigger(this, "content", "right.content", "down.content", "contentCopy"));
		}
		
	}

	@Test
	public void testCopy() {
		final int DIM = 20;
		final int TIMES = 100;
		
		Node[][] grid = new Node[DIM][DIM];
		
		long startingTime = System.currentTimeMillis();
		
		for(int i = DIM-1; i >= 0; i--) {
			for(int j = DIM-1; j >= 0; j--) {
				Node node = j > 0 ? new Node() : new Node(true);
				grid[i][j] = node;
				if (i+1 < DIM)
					node.setContent("down",  grid[i+1][j]);
				if (j+1 < DIM)
					node.setContent("right", grid[i][j+1]);
			}
		}
		
		long elapsed = System.currentTimeMillis() - startingTime;
		System.out.println("Grid prepared in: " + elapsed);
		
		Node copy = grid[4][4].copy();
		
		startingTime = System.currentTimeMillis();
		
		copy.setContent("content", "Hello");
		
		elapsed = System.currentTimeMillis() - startingTime;
		assertEquals("Hello", copy.right.right.right.content);
		System.out.println("Change propagated in copy in: " + elapsed);
		
		startingTime = System.currentTimeMillis();
		
		copy.setContent("nonConnected", "Hello");
		
		elapsed = System.currentTimeMillis() - startingTime;
		System.out.println("Not listened change propagated in copy in: " + elapsed);
		
		assertEquals("Hello", copy.right.content);
		assertEquals("Hello", copy.contentCopy);
		assertEquals("Hello", copy.right.contentCopy);
		assertEquals(null, grid[4][4].content);
		
		startingTime = System.currentTimeMillis();
		
		grid[0][0].setContent("content", "Hello 2");
		
		elapsed = System.currentTimeMillis() - startingTime;
		System.out.println("Change propagated in original in: " + elapsed);
		
		assertEquals("Hello 2", grid[0][4].content);
		assertEquals("Hello 2", grid[4][0].content);
		assertEquals("Hello 2", grid[DIM-1][DIM-1].content);
		assertEquals("Hello 2", grid[DIM-1][DIM-1].contentCopy);
		
//		assertEquals(1, copy.getBoundProperties().size());
		assertEquals(2, grid[4][4].getBoundProperties().size());
		
		startingTime = System.currentTimeMillis();
		
		for(int i = 0; i < TIMES; i++) {
			grid[4][4].copy();
		}
		
		elapsed = System.currentTimeMillis() - startingTime;
		System.out.println(TIMES + " copies in: " + elapsed);
	}
	
	@Test
	public void testSerialization() {
		final int DIM = 12;
		
		Node[][] grid = new Node[DIM][DIM];
		
		long startingTime = System.currentTimeMillis();
		
		for(int i = DIM-1; i >= 0; i--) {
			for(int j = DIM-1; j >= 0; j--) {
				Node node = j > 0 ? new Node() : new Node(true);
				grid[i][j] = node;
				if (i+1 < DIM)
					node.setContent("down",  grid[i+1][j]);
				if (j+1 < DIM)
					node.setContent("right", grid[i][j+1]);
			}
		}
		
		long elapsed = System.currentTimeMillis() - startingTime;
		System.out.println("Grid prepared in: " + elapsed);
		
		startingTime = System.currentTimeMillis();
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		grid[0][0].write(out);
		
		elapsed = System.currentTimeMillis() - startingTime;
		System.out.println("Serialized in: " + elapsed);
		
		startingTime = System.currentTimeMillis();
		
		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		Node copy = IObject.load(in);
		
		elapsed = System.currentTimeMillis() - startingTime;
		System.out.println("Deserialized in: " + elapsed);
		
		copy.setContent("content", "Hello");
		
		assertEquals("Hello", copy.right.content);
		assertEquals("Hello", copy.contentCopy);
		assertEquals("Hello", copy.right.contentCopy);
		assertEquals("Hello", copy.down.content);		
	}

}
