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
 * $Date: 2007-10-30 18:19:38 $
 * $Author: jzinky $
 *
 * =============================================================================
 */

package org.cougaar.test.sequencer;

import org.cougaar.core.util.UniqueObject;
import org.cougaar.test.coordinations.RIRMulticastQueryRoleCoordinationPlugin;

/**
 * Send registrations and completions to sequencer agent and receive requests
 * from sequencer agent. Transfer is accomplished by copying the blackboard
 * events onto the query field of a simple relay.
 */
public class CoordinationAgentSidePlugin extends RIRMulticastQueryRoleCoordinationPlugin {
    public boolean isResponse(UniqueObject event) {
        return event instanceof NodeCompletionEvent;
    }
   
    public boolean isRegistration(UniqueObject event) {
        return event instanceof NodeRegistrationEvent;
    }

    public boolean isQuery(UniqueObject event) {
        return event instanceof NodeRequest;
    }

}
