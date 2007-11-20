/* *************************************************************************
 *
 * <rrl>
 * =========================================================================
 *                                  LEGEND
 *
 * Use, duplication, or disclosure by the Government is as set forth in the
 * Rights in technical data noncommercial items clause DFAR 252.227-7013 and
 * Rights in noncommercial computer software and noncommercial computer
 * software documentation clause DFAR 252.227-7014, with the exception of
 * third party software known as Sun Microsystems' Java Runtime Environment
 * (JRE), Quest Software's JClass, Oracle's JDBC, and JGoodies which are
 * separately governed under their commercial licenses.  Refer to the
 * license directory for information regarding the open source packages used
 * by this software.
 *
 * Copyright 2007 by BBN Technologies Corporation.
 * =========================================================================
 * </rrl>
 *
 * $Id: Envelope.java,v 1.1 2007-11-20 20:38:33 rshapiro Exp $
 *
 * ************************************************************************/
package org.cougaar.test.coordinations.selectserver;

import java.io.Serializable;
import java.util.Set;

import org.cougaar.core.service.BlackboardService;
import org.cougaar.core.util.UniqueObject;

/**
 * Envelopes are the data type passed around by the coordination
 */
public class Envelope implements Serializable {
    enum Operation {
        ADD,
        CHANGE,
        REMOVE
    }
    
    private final UniqueObject contents;
    private final Operation operation;
    private final Set<?> changeReports;
    
    public Envelope(UniqueObject object, Operation op) {
        this(object, op, null);
    }
    
    public Envelope(UniqueObject object, Operation op, Set<?> changeReports) {
        this.contents = object;
        this.operation = op;
        this.changeReports = changeReports;
    }
   
    public UniqueObject getContents() {
        return contents;
    }

    public Operation getOperation() {
        return operation;
    }
    
    public Set<?> getChangeReports() {
        return changeReports;
    }
    
    public void publish(BlackboardService blackboard) {
        switch (operation) {
            case ADD:
                blackboard.publishAdd(contents);
                break;
                
            case CHANGE:
                blackboard.publishChange(contents, changeReports);
                break;
                
            case REMOVE:
                blackboard.publishRemove(contents);
                break;
        }
    }
}
