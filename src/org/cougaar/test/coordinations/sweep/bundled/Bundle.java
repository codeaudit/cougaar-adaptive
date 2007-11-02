/*
 *
 * Copyright 2007 by BBN Technologies Corporation
 *
 */

package org.cougaar.test.coordinations.sweep.bundled;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.cougaar.core.util.UniqueObject;

// TODO set for add, change, delete
public final class Bundle implements Serializable, Iterable<UniqueObject>  {
    private final Set<UniqueObject> data;
    
    Bundle(Set<UniqueObject> data) {
        this.data = new HashSet<UniqueObject>(data);
    }

    public Set<UniqueObject> getData() {
        return data;
    }

    public Iterator<UniqueObject> iterator() {
        return data.iterator();
    }
}