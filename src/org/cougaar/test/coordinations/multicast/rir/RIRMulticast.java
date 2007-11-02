/*
 *
 * Copyright 2007 by BBN Technologies Corporation
 *
 */

package org.cougaar.test.coordinations.multicast.rir;

import org.cougaar.test.coordinations.RoleSpec;
import org.cougaar.test.coordinations.RoleEventMatcher;
import org.cougaar.test.coordinations.CoordinationEventType;

/**
 * A collection of classes and interfaces that specify
 * the abstracted receiver-initiated-registration pattern.
 */
public class RIRMulticast {
    public interface Matcher<R extends RoleSpec<EventType>> 
        extends RoleEventMatcher<R, EventType> {
    }

    public enum EventType implements CoordinationEventType {
        REGISTRATION,
        QUERY,
        RESPONSE;
    }
    
    public static class Query implements RoleSpec<EventType> {
        public EventType[] getPublishes() {
            return new EventType[] {
                    EventType.REGISTRATION,
                    EventType.RESPONSE,
            };
        }

        public EventType[] getSubscribes() {
            return new EventType[] {
                    EventType.QUERY,
            };
        }
    }
    
    public static class Response implements RoleSpec<EventType> {
        public EventType[] getPublishes() {
            return new EventType[] {
                    EventType.QUERY,

            };
        }

        public EventType[] getSubscribes() {
            return new EventType[] {
                    EventType.REGISTRATION,
                    EventType.RESPONSE,
            };
        }
    }

}