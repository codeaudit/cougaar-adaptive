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
* Created : Aug 14, 2007
* Workfile: PingWorkerPlugin.java
* $Revision: 1.1 $
* $Date: 2008-02-28 16:16:15 $
* $Author: jzinky $
*
* =============================================================================
*/
 
package org.cougaar.test.knode.experiment;

import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;

import org.cougaar.test.sequencer.Context;
import org.cougaar.test.sequencer.ReportBase;
import org.cougaar.test.sequencer.experiment.ExperimentStep;
import org.cougaar.test.sequencer.experiment.ExperimentWorkerPlugin;
import org.cougaar.util.annotations.Cougaar;

public class KnodeWorkerPlugin extends ExperimentWorkerPlugin implements KnodeSteps {
    private String reason = "no reason";
    private PrintStream stream;
    
    @Cougaar.Arg(name="knodeControllerHost", required=true)
    public InetAddress knodeControllerHost;
    
    @Cougaar.Arg(name="knodeControllerPort", defaultValue="1175")
    public short knodeControllerPort;
    
    protected void doStep(ExperimentStep step, Context context) {
        if (KNODE_ADD_LINK.equals(step)) { 
            // send KNODE message to socket, 
            String link = context.getParameter(LINK_PROPERTY);
            if (link != null) {
                String text = "add link " + link;
                log.info("Sending command \"" + text+ "\"");
                stream.println(text);
                // No feedback from KNODE
                stepCompeleted(step, new ReportBase(workerId, true, reason));
            } else {
                stepCompeleted(step, new ReportBase(workerId, false, "no link"));
            }
        } else if (KNODE_DEL_LINK.equals(step)) { 
            String link = context.getParameter(LINK_PROPERTY);
            if (link != null) {
                String text = "del link " + link;
                log.info("Sending command \"" + text+ "\"");
                stream.println(text);
                // No feedback from KNODE
                stepCompeleted(step, new ReportBase(workerId, true, reason));
            } else {
                stepCompeleted(step, new ReportBase(workerId, false, "no link"));
            }
        } else if (SOCIETY_READY.equals(step)) {
            try {
                Socket skt = new Socket(knodeControllerHost, knodeControllerPort);
                stream = new PrintStream(skt.getOutputStream());
                log.info("Opened connection to " + skt);
                stepCompeleted(step, new ReportBase(workerId, true, reason));
            } catch (IOException e) {
                String msg = "Failed to connect to KNode Controller: " + e.getMessage();
                log.error(msg);
                stepCompeleted(step, new ReportBase(workerId, false, msg));
            }
        } else if (SHUTDOWN.equals(step)) {
            if (stream != null) {
                stream.close();
            }
            stepCompeleted(step, new ReportBase(workerId, true, reason));
        } else {
            stepCompeleted(step, new ReportBase(workerId, true, reason));
        }
    }
    
}
