/*
 *
 * Copyright 2007 by BBN Technologies Corporation
 *
 */

package org.cougaar.test.coordinations.multicast.rir;

import org.cougaar.test.coordinations.Face;
import org.cougaar.test.coordinations.FaceEventMatcher;
import org.cougaar.test.coordinations.CoordinationEventType;

/**
 * This class comprises classes and interfaces that specify
 * the abstracted receiver-initiated-registration coordination pattern.
 * This coordination makes two faces available to the 
 * outside world: {@link Query} and {@link Response}.
 * 
 * <p> When communicating via the Query face, the external Agent receives
 * registrations from responders (see below). When the Agent submits
 * a query, it will then receive responses from each of the
 * registered responders. 
 * 
 * <p> When communicating via the Response face, the external Agent
 * must register, and then provide a response to each query it receives.
 * 
 */
public class RIRMulticast {
    public interface Matcher<R extends Face<EventType>> 
        extends FaceEventMatcher<R, EventType> {
    }

    public enum EventType implements CoordinationEventType {
        REGISTRATION,
        QUERY,
        RESPONSE;
    }
    
    public static class Query implements Face<EventType> {
        public EventType[] produces() {
            return new EventType[] {
                    EventType.REGISTRATION,
                    EventType.RESPONSE,
            };
        }

        public EventType[] consumes() {
            return new EventType[] {
                    EventType.QUERY,
            };
        }
    }
    
    public static class Response implements Face<EventType> {
        public EventType[] produces() {
            return new EventType[] {
                    EventType.QUERY,

            };
        }

        public EventType[] consumes() {
            return new EventType[] {
                    EventType.REGISTRATION,
                    EventType.RESPONSE,
            };
        }
    }

}