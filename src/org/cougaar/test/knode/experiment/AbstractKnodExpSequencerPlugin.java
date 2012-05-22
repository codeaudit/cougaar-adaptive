package org.cougaar.test.knode.experiment;

import org.cougaar.test.sequencer.Report;
import org.cougaar.test.sequencer.experiment.AbstractExperimentSequencerPlugin;

public abstract class AbstractKnodExpSequencerPlugin
      extends AbstractExperimentSequencerPlugin<Report>
      implements KnodeSteps {
   // The K-Node configuration assumes the following basic Tee-Topology
   //
   // L1 L2 L3 L4 L5 L6 M1 M2 M3 R4 R5 R6 R1 R2 R3 (Hairy Nodes)
   // LeftServer---LL---LM---MM---RM---RR---RightServer
   // |
   // MN
   // |
   // MovingClient

   @SuppressWarnings("unused")
   private static final String LEFT_SERVER = "192.168.163.100";
   private static final String RIGHT_SERVER = "192.168.167.100";
   private static final String MOVING_CLIENT = "192.168.162.100";

   private static final String MN = "162";
   private static final String LL = "163";
   private static final String LM = "164";
   private static final String MM = "165";
   private static final String RM = "166";
   private static final String RR = "167";
   private static final String L1 = "140";
   private static final String L2 = "141";
   private static final String L3 = "142";
   private static final String L4 = "143";
   private static final String L5 = "144";
   private static final String L6 = "145";
   private static final String M1 = "146";
   private static final String M2 = "147";
   private static final String M3 = "148";
   private static final String R1 = "152";
   private static final String R2 = "153";
   private static final String R4 = "149";
   private static final String R3 = "154";
   private static final String R5 = "150";
   private static final String R6 = "151";

   private final int SLOW_DOWN = 25;

   protected void addRestartKnodeSteps() {
      String from = MOVING_CLIENT;
      String to = RIGHT_SERVER;
      String path = "IpFlow(" + from + "," + to + "):CapacityMax()";
      addStep(KNODE_SET_METRIC, 0, null, METRIC_PATH_PROPERTY + "=" + path);
      addStep(SOCIETY_READY, 0, null);
      // Basic 5 Hop Line MN->LL->LM->MM->RM->RR
      addAddLinkSteps(MN, LL, SLOW_DOWN);
      addAddLinkSteps(LL, LM, SLOW_DOWN);
      addAddLinkSteps(LM, MM, SLOW_DOWN);
      addAddLinkSteps(MM, RM, SLOW_DOWN);
      addAddLinkSteps(RM, RR, SLOW_DOWN);
      // Remove extra TEE Links
      addDeleteLinkSteps(LM, MN, SLOW_DOWN);
      addDeleteLinkSteps(MM, MN, SLOW_DOWN);
      addDeleteLinkSteps(RM, MN, SLOW_DOWN);
      addDeleteLinkSteps(RR, MN, SLOW_DOWN);
      // Remove extra STAR
      addDeleteLinkSteps(RR, LL, SLOW_DOWN);
      addDeleteLinkSteps(RR, LM, SLOW_DOWN);
      addDeleteLinkSteps(RR, MM, SLOW_DOWN);
      // Remove extra Hairy 1 Line
      addDeleteLinkSteps(LL, L1, SLOW_DOWN);
      addDeleteLinkSteps(LM, L4, SLOW_DOWN);
      addDeleteLinkSteps(MM, M1, SLOW_DOWN);
      addDeleteLinkSteps(RM, R4, SLOW_DOWN);
      addDeleteLinkSteps(RR, R1, SLOW_DOWN);
      // Remove extra Hairy 2 Line
      addDeleteLinkSteps(LL, L2, SLOW_DOWN);
      addDeleteLinkSteps(LM, L5, SLOW_DOWN);
      addDeleteLinkSteps(MM, M2, SLOW_DOWN);
      addDeleteLinkSteps(RM, R5, SLOW_DOWN);
      addDeleteLinkSteps(RR, R2, SLOW_DOWN);
      // Remove extra Hairy 3 Line
      addDeleteLinkSteps(LL, L3, SLOW_DOWN);
      addDeleteLinkSteps(LM, L6, SLOW_DOWN);
      addDeleteLinkSteps(MM, M3, SLOW_DOWN);
      addDeleteLinkSteps(RM, R6, SLOW_DOWN);
      addDeleteLinkSteps(RR, R3, SLOW_DOWN);
      // Wait for topology to settle
      addStep(KNODE_WAIT_METRIC, 0, null, METRIC_VALUE_PROPERTY + "=30.0");
   }

   protected void addMoveLinkSteps(String from, String to, String desiredCapacity) {
      addStep(KNODE_ADD_LINK, 0, null, LINK_PROPERTY + "= " + MN + " " + to);
      addStep(KNODE_DEL_LINK, 0, null, LINK_PROPERTY + "= " + MN + " " + from);
      addStep(KNODE_WAIT_METRIC, 0, null, METRIC_VALUE_PROPERTY + "=" + desiredCapacity);
   }

   protected void addDeleteLinkSteps(String from, String to) {
      addDeleteLinkSteps(from, to, 0);
   }

   protected void addDeleteLinkSteps(String from, String to, int wait) {
      addStep(KNODE_DEL_LINK, wait, null, LINK_PROPERTY + "= " + from + " " + to);
   }

   protected void addAddLinkSteps(String from, String to, int wait) {
      addStep(KNODE_ADD_LINK, wait, null, LINK_PROPERTY + "= " + from + " " + to);
   }

   protected void addAddLinkSteps(String from, String to) {
      addAddLinkSteps(from, to, 0);
   }

}
