/*
 *
 * Copyright 2008 by BBN Technologies Corporation
 *
 */

package org.cougaar.test.sequencer;

import java.util.Collection;
import java.util.Set;

import org.cougaar.core.qos.stats.Statistic;
import org.cougaar.util.log.Logger;

/**
 * Walks through a collection of report sets gathered during an experiment and
 * merges all the statistics into a summary.
 */
abstract public class StatisticsAccumulator {
   private final Logger log;

   public StatisticsAccumulator(Logger log) {
      this.log = log;
   }

   /**
    * Statistic-specific accumulation
    */
   abstract protected void accumulate(Statistic statistic);

   public void accumulate(Collection<Set<Report>> reportsCollection) {
      for (Set<Report> reports : reportsCollection) {
         for (Report report : reports) {
            if (!report.isSuccessful()) {
               log.error("Summary step was successful, but unsuccessful report" + report);
            }
            log.info(report.toString());
            if (report instanceof StatisticsReport) {
               for (Statistic stat : ((StatisticsReport) report).getRawStats()) {
                  accumulate(stat);
               }
            }
         }
      }
   }
}
