/*
 *
 * Copyright 2007 by BBN Technologies Corporation
 *
 */

package org.cougaar.test.coordinations.selectserver;

import org.cougaar.test.coordinations.CoordinationEventType;
import org.cougaar.test.coordinations.Face;
import org.cougaar.test.coordinations.FaceEventMatcher;


/**
 *
 */
public class ServerSelection {
    public interface Matcher<R extends Face<EventType>> 
        extends FaceEventMatcher<R, EventType> {
    }

    public enum EventType implements CoordinationEventType {
        REQUEST,
        RESPONSE;
    }
    
    public static class Client implements Face<EventType> {
        public EventType[] produces() {
            return new EventType[] {
                    EventType.RESPONSE,
            };
        }

        public EventType[] consumes() {
            return new EventType[] {
                    EventType.REQUEST,
            };
        }
    }
    
    public static class Server implements Face<EventType> {
        public EventType[] produces() {
            return new EventType[] {
                    EventType.REQUEST,

            };
        }

        public EventType[] consumes() {
            return new EventType[] {
                    EventType.RESPONSE,
            };
        }
    }

}