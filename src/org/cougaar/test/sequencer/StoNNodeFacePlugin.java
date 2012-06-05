/* =============================================================================
 *
 *                  COPYRIGHT 2007 BBN Technologies Corp.
 *                  10 Moulton St
 *                  Cambridge MA 02138
 *                  (617) 873-8000
 *
 *       This program is the subject of intellectual property rights
 *       licensed from BBN Technologies
 *
 *       This legend must continue to appear in the source code
 *       despite modifications or enhancements by any party.
 *
 *
 * =============================================================================
 *
 * Created : Aug 13, 2007
 * Workfile: NodeLocalSequencerPlugin.java
 * $Revision: 1.2 $
 * $Date: 2008-04-25 21:30:42 $
 * $Author: jzinky $
 *
 * =============================================================================
 */

package org.cougaar.test.sequencer;

import org.cougaar.core.qos.coordinations.multicast.rir.RIRMulticast.EventType;
import org.cougaar.core.qos.coordinations.multicast.rir.RIRMulticastResponseFacePlugin;
import org.cougaar.core.util.UniqueObject;

/*
 * Send registrations and completions to sequencer agent and receive requests
 * from sequencer agent. Transfer is accomplished by copying the blackboard
 * events onto the query field of a simple relay.
 */
public class StoNNodeFacePlugin
      extends RIRMulticastResponseFacePlugin {
   private final StoNCoordinationBlackboardPredicates predicates = new StoNCoordinationBlackboardPredicates();

   public boolean match(EventType type, UniqueObject event) {
      switch (type) {
         case QUERY:
            return predicates.isQuery(event);

         case REGISTRATION:
            return predicates.isRegistration(event);

         case RESPONSE:
            return predicates.isResponse(event);
      }
      return false;
   }

}
