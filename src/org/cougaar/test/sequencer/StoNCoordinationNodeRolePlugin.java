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
 * $Revision: 1.1 $
 * $Date: 2007-10-30 19:19:14 $
 * $Author: jzinky $
 *
 * =============================================================================
 */

package org.cougaar.test.sequencer;

import org.cougaar.core.util.UniqueObject;
import org.cougaar.test.coordinations.multicast.rir.RIRMulticastResponseRoleCoordinationPlugin;

/*
 * Send registrations and completions to sequencer agent and receive requests
 * from sequencer agent. Transfer is accomplished by copying the blackboard
 * events onto the query field of a simple relay.
 */
public class StoNCoordinationNodeRolePlugin extends RIRMulticastResponseRoleCoordinationPlugin {
    private final StoNCoordinationBlackboardPredicates predicates = 
         new StoNCoordinationBlackboardPredicates();

    public boolean isQuery(UniqueObject event) {
        return predicates.isQuery(event);
    }

    public boolean isRegistration(UniqueObject event) {
        return predicates.isRegistration(event);
    }

    public boolean isResponse(UniqueObject event) {
        return predicates.isResponse(event);
    }
    
}
