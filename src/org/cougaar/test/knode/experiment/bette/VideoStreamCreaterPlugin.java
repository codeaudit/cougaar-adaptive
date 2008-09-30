/*
 *
 * Copyright 2007 by BBN Technologies Corporation
 *
 */

package org.cougaar.test.knode.experiment.bette;

import org.cougaar.core.agent.service.alarm.Alarm;
import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.plugin.TodoPlugin;
import org.cougaar.core.service.IncarnationService;
import org.cougaar.core.service.ThreadService;
import org.cougaar.core.service.UIDService;
import org.cougaar.core.thread.Schedulable;
import org.cougaar.core.thread.SchedulableStatus;
import org.cougaar.test.ping.StartRequest;
import org.cougaar.test.ping.StopRequest;
import org.cougaar.util.annotations.Cougaar;
import org.cougaar.util.annotations.Subscribe;

public class VideoStreamCreaterPlugin
        extends TodoPlugin {
    
    @Cougaar.Arg(name = "frameRate", defaultValue = "10", 
                 description = "frames per second (max is 10fps)")
    public float frameRate;
    
    @Cougaar.Arg(name = "streamName", defaultValue = "DefaultStream", 
                 description = "Name of the Stream")
    public String streamName;

    // TODO Cougaar Alarm has granularity of 100ms, so can't go faster than 10fps
    // TODO use Annotated Service lookup
    private TimedImageService imageService;
    private ThreadService threadService;
    private StartRequest startRequest;
    private StopRequest stopRequest;
    private boolean failed = false;
    private Schedulable sendNextSchedulable;
    private long myIncarnation;

    public void start() {
        super.start();
        ServiceBroker sb = getServiceBroker();
        imageService = sb.getService(this, TimedImageService.class, null);
        threadService = sb.getService(this,ThreadService.class, null );
    }

    @Cougaar.Execute(on = Subscribe.ModType.ADD)
    public void executeStartRun(StartRequest request) {
        // Indicate that we have started
        startRequest = request;
        startRequest.inc();
        blackboard.publishChange(startRequest);
        // schedule the first frame
        long waitTime = (int) (1000 / frameRate);
        if (waitTime < 100) {
            log.warn("Frame rate too fast. Maximum frame rate is 10fps");
            waitTime = 100;
        }
        Runnable sendNext = new sendNextImageRunnable(uids, System.currentTimeMillis(), waitTime,1);
        sendNextSchedulable = threadService.getThread(this,sendNext,"VideoCapture",ThreadService.WILL_BLOCK_LANE);
        sendNextSchedulable.schedule(waitTime);
    }

    @Cougaar.Execute(on = Subscribe.ModType.ADD)
    public void executeStopRun(StopRequest request) {
        stopRequest = request;
    }

    
    private long getMyInarnation() {
        ServiceBroker sb = getServiceBroker();
        IncarnationService incarnationService = sb.getService(this, IncarnationService.class, null);
        long myIncarnation = incarnationService.getIncarnation(agentId);
        if (log.isInfoEnabled()) {
            log.info("Agent "+ agentId + " has incarnation number "+ myIncarnation);
        }
        return myIncarnation;
    }
    
    private class sendNextImageRunnable
            implements Runnable {
        private long expectedCaptureTime;
        private long  waitTimeMillis;
        private UIDService uids;
        private int count;

        public sendNextImageRunnable(UIDService uids, long time, long waitTimeMillis, int count) {
            this.expectedCaptureTime = time;
            this.uids = uids;
            this.waitTimeMillis=waitTimeMillis;
            this.count = count;
        }

        public void run() {
            if (myIncarnation==0) {
                myIncarnation=getMyInarnation();
 /*               if (myIncarnation == 0) {
                    // Agent not registered in WP wait
                    Runnable nextRunnable = 
                        new sendNextImageRunnable(uids,System.currentTimeMillis(),waitTimeMillis,1);
                    sendNextAlarm =  executeLater(waitTimeMillis, nextRunnable);
                   return;
                }
  */          }
            // check if test should stop
            if (stopRequest != null) {
                stopRequest.inc();
                if (failed) {
                    stopRequest.forceFailed();
                }
                sendNextSchedulable.cancel();
                blackboard.publishChange(stopRequest);
                return;
            }
            // Publish frame for capture time
            long now =System.currentTimeMillis();
            SchedulableStatus.beginNetIO("CaptureImage");
            byte[] image = imageService.getImage(expectedCaptureTime);
            SchedulableStatus.endBlocking();
            if (image.length > 0) {
                ImageHolder imageHolder = new ImageHolder(uids, expectedCaptureTime, image, 
                                                          streamName, count, myIncarnation);
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
                count += numberOfMissedCaptures;
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
            Runnable sendNext = new sendNextImageRunnable(uids, System.currentTimeMillis(), waitTimeMillis,1);
            sendNextSchedulable = threadService.getThread(VideoStreamCreaterPlugin.this,sendNext,"VideoCapture",ThreadService.WILL_BLOCK_LANE);
            sendNextSchedulable.schedule(waitTimeMillis);
         }
    }
}
