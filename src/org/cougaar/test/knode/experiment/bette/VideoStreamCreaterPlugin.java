/*
 *
 * Copyright 2007 by BBN Technologies Corporation
 *
 */

package org.cougaar.test.knode.experiment.bette;

import org.cougaar.core.agent.service.alarm.Alarm;
import org.cougaar.core.plugin.TodoPlugin;
import org.cougaar.core.service.UIDService;
import org.cougaar.test.ping.StartRequest;
import org.cougaar.test.ping.StopRequest;
import org.cougaar.util.annotations.Cougaar;
import org.cougaar.util.annotations.Subscribe;

public class VideoStreamCreaterPlugin
        extends TodoPlugin {
    
    @Cougaar.Arg(name = "frameRate", defaultValue = "15", description = "frames per second (max is 10fps)")
    public float frameRate;

    // TODO Cougaar Alarm has granularity of 100ms, so can't go faster than 10fps
    // TODO use Annotated Service lookup
    private TimedImageService imageService;
    private StartRequest startRequest;
    private StopRequest stopRequest;
    private boolean failed = false;
    private Alarm sendNextAlarm;

    public void start() {
        super.start();
        imageService = getServiceBroker().getService(this, TimedImageService.class, null);
    }

    @Cougaar.Execute(on = Subscribe.ModType.ADD)
    public void executeStartRun(StartRequest request) {
        // Indicate that we have started
        startRequest = request;
        startRequest.inc();
        blackboard.publishChange(startRequest);
        // schedule the first frame
        long waitTime = (int) (1000 / frameRate);
        sendNextAlarm =
                executeLater(waitTime, 
                             new sendNextImageRunnable(uids, System.currentTimeMillis(), waitTime));
    }

    @Cougaar.Execute(on = Subscribe.ModType.ADD)
    public void executeStopRun(StopRequest request) {
        stopRequest = request;
    }

    private class sendNextImageRunnable
            implements Runnable {
        private long expectedCaptureTime;
        private long  waitTimeMillis;
        private UIDService uids;

        public sendNextImageRunnable(UIDService uids, long time, long waitTimeMillis) {
            this.expectedCaptureTime = time;
            this.uids = uids;
            this.waitTimeMillis=waitTimeMillis;
        }

        public void run() {
            // check if test should stop
            if (stopRequest != null) {
                stopRequest.inc();
                if (failed) {
                    stopRequest.forceFailed();
                }
                sendNextAlarm.cancel();
                blackboard.publishChange(stopRequest);
                return;
            }
            // Publish frame for capture time
            long now =System.currentTimeMillis();
            byte[] image = imageService.getImage(expectedCaptureTime);
            if (image.length > 0) {
                ImageHolder imageHolder = new ImageHolder(uids, expectedCaptureTime, image);
                blackboard.publishAdd(imageHolder);
            } else {
                log.warn("Image empty");
            }

            // Schedule Query after Next
            long nextCaptureTime = expectedCaptureTime+ waitTimeMillis;

            long triggerDelayMillis = nextCaptureTime-now;
            if (triggerDelayMillis <= 0) {
                // Missed the next Capture time
                // fast forward to next time, keeping the phase
                int numberOfMissedCaptures= (int) Math.floor((now-expectedCaptureTime)/waitTimeMillis);
                nextCaptureTime = expectedCaptureTime + ((numberOfMissedCaptures + 1) * waitTimeMillis);
                triggerDelayMillis = nextCaptureTime - now;
                if (log.isWarnEnabled()) {
                    log.warn("Missed some captures, Skipping ahead " + (triggerDelayMillis) + " millis");
                }
            } else if (triggerDelayMillis > waitTimeMillis) {
                if (log.isWarnEnabled()) {
                    log.warn("Next Capture in distant future");
                }
            }
            if (log.isInfoEnabled()) {
                log.info("Capture late by " + (now - expectedCaptureTime) + " millis."  +
                		"Triggering in " + triggerDelayMillis + " millis. " );
            }
            sendNextAlarm =
                    executeLater(triggerDelayMillis, new sendNextImageRunnable(uids,
                                                                               nextCaptureTime,
                                                                               waitTimeMillis));
        }

    }
}
