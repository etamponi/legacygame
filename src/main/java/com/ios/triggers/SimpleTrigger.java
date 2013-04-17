package com.ios.triggers;

import java.util.ArrayList;
import java.util.List;

import com.ios.IObject;
import com.ios.Listener;
import com.ios.Property;
import com.ios.Trigger;

public abstract class SimpleTrigger<T extends IObject> extends Trigger<T> {
	
	private final List<Listener> listeners = new ArrayList<>();

	public SimpleTrigger(Listener... listeners) {
		for(Listener l: listeners)
			this.listeners.add(l);
	}

	@Override
	public void action(Property changedPath) {
		boolean stop = true;
		for(Listener l: listeners) {
			if (l.isListeningOn(changedPath)) {
				stop = false;
				break;
			}
		}
		if (stop)
			return;
		makeAction(changedPath);
	}

	protected abstract void makeAction(Property changedPath);
	
	public List<Listener> getListeners() {
		return listeners;
	}

}
