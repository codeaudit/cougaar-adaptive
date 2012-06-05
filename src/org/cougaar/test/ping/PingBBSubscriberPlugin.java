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
 * Workfile: PingReceiverPlugin.java
 * $Revision: 1.1 $
 * $Date: 2008-02-26 15:31:56 $
 * $Author: jzinky $
 *
 * =============================================================================
 */

package org.cougaar.test.ping;

import java.util.ArrayList;
import java.util.List;

import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.core.plugin.AnnotatedSubscriptionsPlugin;
import org.cougaar.util.UnaryPredicate;
import org.cougaar.util.annotations.Cougaar;

/**
 * A plugin that does nothing, but setup subscriptions.
 */
public class PingBBSubscriberPlugin
      extends AnnotatedSubscriptionsPlugin {
   private List<IncrementalSubscription> subscriptions = new ArrayList<IncrementalSubscription>();

   @Cougaar.Arg(name = "pluginId", defaultValue = "a", description = "Subsriber Plugin Id")
   public String pluginId;

   @Cougaar.Arg(name = "numSubscriptions", defaultValue = "1", description = "Number of subscription to create")
   public int numSubscriptions;

   @Cougaar.Arg(name = "wasteSubscriptionTime", defaultValue = "0", description = "Wallclock time for a waste spin loop for each subscription")
   public int wasteSubsriptionTime;

   @Override
   protected void setupSubscriptions() {
      for (int i = 0; i < numSubscriptions; i++) {
         Subscription sub = new Subscription(i, wasteSubsriptionTime);
         subscriptions.add(blackboard.subscribe(sub));
      }
   }

   @Override
   protected void execute() {
      log.info("Null Subscription is being executed");
   }

   private class Subscription
         implements UnaryPredicate {
      /**
       * 
       */
      private static final long serialVersionUID = 1L;
      int wasteTime;

      private Subscription(int id, int wasteTime) {
         super();
         this.wasteTime = wasteTime;
      }

      public boolean execute(Object o) {
         long count = 0;
         long stopTime = System.currentTimeMillis() + wasteTime;
         while (System.currentTimeMillis() < stopTime) {
            count = count + 1;
         }
         // do one instanceof
         return o instanceof PingQuery && false;
      }
   }

}
