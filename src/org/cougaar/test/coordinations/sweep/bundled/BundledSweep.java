/*
 *
 * Copyright 2007 by BBN Technologies Corporation
 *
 */

package org.cougaar.test.coordinations.sweep.bundled;

import org.cougaar.test.coordinations.CoordinationEventType;
import org.cougaar.test.coordinations.Face;
import org.cougaar.test.coordinations.FaceEventMatcher;


/**
 * This class comprises classes and interfaces that specify the abstracted
 * leader/follower coordination pattern with bundling. This coordination makes
 * two faces available to the outside world: {@link Leader} and {@link Follower}.
 * 
 * <p>
 * When communicating via the Leader face, the external Agent submits requests
 * and will then receive responses from each of the followers.
 * 
 * <p>
 * When communicating via the Response face, the external Agent must provide a
 * response to each query it receives.
 * 
 * <p>
 * Internal description:
 * <p>
 * The Leader face collects pending requests into a bundle and sends them to the
 * follower agent, via a relay. The follower agent will then unbundle the
 * requests and produce them through its Follower face, then collect the
 * responses, bundle them and return them to the leader agent.
 * 
 * No guarantee is made that the same number of requests will be returned by the
 * follower, but the requester will return at least one result and old results
 * may be returned in subsequent bundles.
 * 
 */
public class BundledSweep {
    public interface Matcher<R extends Face<EventType>> 
        extends FaceEventMatcher<R, EventType> {
    }

    public enum EventType implements CoordinationEventType {
        REQUEST,
        RESPONSE;
    }
    
    public static class Leader implements Face<EventType> {
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
    
    public static class Follower implements Face<EventType> {
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