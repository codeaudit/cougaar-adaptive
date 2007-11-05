/*
 *
 * Copyright 2007 by BBN Technologies Corporation
 *
 */

package org.cougaar.test.coordinations.sweep.bundled;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.cougaar.core.util.UniqueObject;

// TODO set for add, change, remove

public final class Bundle implements Serializable  {
	private static final long serialVersionUID = -5484732207158064060L;
	private final Set<UniqueObject> adds;
	private final Set<UniqueObject> changes;
	private final Set<UniqueObject> removes;
    
    Bundle(Set<UniqueObject> adds,
    		Set<UniqueObject> removes,
    		Set<UniqueObject> changes) {
        this.adds = new HashSet<UniqueObject>(adds);
        this.changes = new HashSet<UniqueObject>(changes);
        this.removes = new HashSet<UniqueObject>(removes);
    }

 
	public Set<UniqueObject> getAdds() {
		return adds;
	}


	public Set<UniqueObject> getChanges() {
		return changes;
	}


	public Set<UniqueObject> getRemoves() {
		return removes;
	}
}