/*
 *
 * Copyright 2007 by BBN Technologies Corporation
 *
 */

package org.cougaar.test.coordinations;

/**
 * This interface describes one of the "faces" a coordination presents to the
 * outside world. A face is described by what kinds of events are sent in to the
 * coordination from outside (ie events the coordination {@link #consumes}) and
 * what kinds of events are sent out by the coordination to the outside (ie,
 * events the coordination {@link #produces}).
 * 
 * <p>
 * Note: These operations have <b>nothing</b> to do with communication within
 * the coordination (ie among its internal parts).
 */
public interface Face<T extends CoordinationEventType> {
    public T[] produces();
    public T[] consumes();
}
