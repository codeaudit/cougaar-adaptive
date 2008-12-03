/*
 *
 * Copyright 2007 by BBN Technologies Corporation
 *
 */

package org.cougaar.test.knode.experiment.bette;

import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.plugin.TodoPlugin;
import org.cougaar.core.service.IncarnationService;
import org.cougaar.core.service.ThreadService;
import org.cougaar.core.service.UIDService;
import org.cougaar.core.thread.Schedulable;
import org.cougaar.core.thread.SchedulableStatus;
import org.cougaar.test.knode.experiment.bette.ClipCaptureState.CommandKind;
import org.cougaar.util.annotations.Cougaar;
import org.cougaar.util.annotations.Subscribe;

public class ClipCreaterPlugin
        extends TodoPlugin {
    
    @Cougaar.Arg(name = "clipName", defaultValue = "DefaultStream", 
                 description = "Name of the Clip")
    public String clipName;

    @Cougaar.Arg(name = "frameRate", defaultValue = "10", 
                 description = "frames per second (max is 100fps)")
    public float frameRate;
    
    @Cougaar.Arg(name = "waitForIncarnationNumber", defaultValue = "false", 
                 description = "Wait to capture clip, until agent is assigned and IncarnationNumber")
    public boolean waitForIncarnationNumber;


    // TODO use Annotated Service lookup
    private TimedImageService imageService;
    private ThreadService threadService;
    private ClipCaptureState captureState;
    private ClipHolder clip;
    private Schedulable captureNextSchedulable;

    public void start() {
        super.start();
        ServiceBroker sb = getServiceBroker();
        imageService = sb.getService(this, TimedImageService.class, null);
        threadService = sb.getService(this,ThreadService.class, null );
    }
    
    //INIT
    @Cougaar.Execute(on = Subscribe.ModType.ADD, when = "isMyCapture")
    public void executeInitCapture(ClipCaptureState state) {
        captureState = state;
    }

    
    // START
    @Cougaar.Execute(on = Subscribe.ModType.CHANGE, when = "isMyStartCapture")
    public void executeStartCapture(ClipCaptureState state) {
    	// clear out old clip
    	deleteClipFromBlackboard(clip);
    	clip=null;
        // Indicate that we have started in capture state
        captureState.setCurrentState(ClipCaptureState.StateKind.Grabbing);
        captureState.setOutstandingCommand(null);
        blackboard.publishChange(captureState);
        // Create Clip blackboard object
        long now = System.currentTimeMillis();
        clip = new ClipHolder(uids, now, captureState.getClipName(), captureState.getClipId());
        // TODO add latitude and longitude from metric service (Node Position)
        blackboard.publishAdd(clip);
        // schedule the first frame
        long waitTime = (int) (1000 / frameRate);
        if (waitTime < 10) {
            log.warn("Frame rate too fast. Maximum frame rate is 100fps");
            waitTime = 10;
        }
        Runnable sendNext = new captureNextImageRunnable(uids, clip, now, waitTime, 1);
        captureNextSchedulable = threadService.getThread(this,sendNext,"ClipCapture",ThreadService.WILL_BLOCK_LANE);
        captureNextSchedulable.schedule(waitTime);
    }

    // STOP
    @Cougaar.Execute(on = Subscribe.ModType.CHANGE, when = "isMyStopCapture")
    public void executeStopRun(ClipCaptureState state) {
    	// update capture state to Looping
        state.setCurrentState(ClipCaptureState.StateKind.Looping);
        state.setOutstandingCommand(null);
        blackboard.publishChange(state);
    }
    
    //SEND
    @Cougaar.Execute(on = Subscribe.ModType.CHANGE, when = "isMySendClip")
    public void executeSendClip(ClipCaptureState state) {
    	// Update capture state to NoClip
        state.setCurrentState(ClipCaptureState.StateKind.NoClip);
        state.setOutstandingCommand(null);
        blackboard.publishChange(state);
        // Mark clip to send
        if (clip != null) {
        	clip.setSend(true);
        	blackboard.publishChange(clip);
        }
    }
    
    //CLEAR
    @Cougaar.Execute(on = Subscribe.ModType.CHANGE, when = "isMyClearClip")
    public void executeClearClip(ClipCaptureState state) {
        state.setCurrentState(ClipCaptureState.StateKind.NoClip);
        state.setOutstandingCommand(null);
        blackboard.publishChange(state);
        deleteClipFromBlackboard(clip);
        clip = null;
        if (captureNextSchedulable != null) {
        	captureNextSchedulable.cancel();
        	captureNextSchedulable = null;
        }
    }
    
    // CAPTURE
    private class captureNextImageRunnable
            implements Runnable {
        private long expectedCaptureTime;
        private long  waitTimeMillis;
        private UIDService uids;
        private int count;
        private ClipHolder clip;

        public captureNextImageRunnable(UIDService uids, ClipHolder clip, long time, long waitTimeMillis, int count) {
            this.expectedCaptureTime = time;
            this.uids = uids;
            this.clip = clip;
            this.waitTimeMillis=waitTimeMillis;
            this.count = count;
        }

        public void run() {
             if (captureState == null || 
                    !captureState.getCurrentState().equals(ClipCaptureState.StateKind.Grabbing)) {
                clip.setEndTime(expectedCaptureTime);
                publishChangeLater(clip);
                captureNextSchedulable.cancel();
                captureNextSchedulable=null;
                return;
            }
            // Publish frame for capture time
            long startTime =System.currentTimeMillis();
            SchedulableStatus.beginNetIO("CaptureImage");
            byte[] image = imageService.getImage(expectedCaptureTime);
            SchedulableStatus.endBlocking();
            if (image.length > 0) {
                ImageHolder imageHolder = new ImageHolder(uids, expectedCaptureTime, image, 
                                                          clipName, count, clip.getClipId());
                publishAddLater(imageHolder);
                clip.addImage(expectedCaptureTime - clip.getStartTime(), imageHolder);
                publishChangeLater(clip);          
            } else {
                log.warn("Image empty");
            }

            // Schedule Query after Next
            long now=System.currentTimeMillis();
            long nextCaptureTime = expectedCaptureTime+ waitTimeMillis;
            count += 1;
            long triggerDelayMillis = nextCaptureTime-now;
            if (triggerDelayMillis <= 0) {
                // Missed the next Capture time
                // fast forward to next time, keeping the phase
                int numberOfMissedCaptures= (int) Math.floor((now-expectedCaptureTime)/waitTimeMillis);
                count += numberOfMissedCaptures;
                nextCaptureTime = expectedCaptureTime + ((numberOfMissedCaptures+1) * waitTimeMillis);
                triggerDelayMillis = nextCaptureTime - now;
                if (log.isInfoEnabled()) {
                    log.info(clipName +" Missed "+numberOfMissedCaptures+ 
                             " captures, Skipping ahead " + (triggerDelayMillis) + " millis" +
                             " count=" +count);
                }
            } else if (triggerDelayMillis > waitTimeMillis) {
                if (log.isWarnEnabled()) {
                    log.warn("Next Capture in distant future");
                }
            }
            if (log.isInfoEnabled()) {
                log.info("Capture late by " + (now - expectedCaptureTime) + " millis."  +
                		" Triggering in " + triggerDelayMillis + " millis. " +
                		" Capture Delay " + (now -startTime));
            }
            Runnable sendNext = new captureNextImageRunnable(uids, clip, nextCaptureTime, waitTimeMillis,count);
            captureNextSchedulable = threadService.getThread(ClipCreaterPlugin.this,sendNext,"VideoCapture",ThreadService.WILL_BLOCK_LANE);
            captureNextSchedulable.schedule(triggerDelayMillis);
         }
    }
    
    public void deleteClipFromBlackboard(ClipHolder clip) {
       	if (clip != null) {
       		for (ImageHolder image : clip.getImageLoop().images()) {
       			blackboard.publishRemove(image);
       		}
            blackboard.publishRemove(clip);
    	}
    }

   
    public boolean isMyCapture(ClipCaptureState state) {
        return state != null && state.getClipName().equals(clipName);
    }
    
    public boolean isMyStartCapture(ClipCaptureState state) {
        if (state != captureState) {
            return false;
        }
        CommandKind outstandingCommand = state.getOutstandingCommand();
        return outstandingCommand !=null  && 
        outstandingCommand.equals(ClipCaptureState.CommandKind.StartCapture);
    }
    
    public boolean isMyStopCapture(ClipCaptureState state) {
        if (state != captureState) {
            return false;
        }
        CommandKind outstandingCommand = state.getOutstandingCommand();
        return outstandingCommand !=null && 
        outstandingCommand.equals(ClipCaptureState.CommandKind.StopCapture);
    }

    public boolean isMySendClip(ClipCaptureState state) {
        if (state != captureState) {
            return false;
        }
        CommandKind outstandingCommand = state.getOutstandingCommand();
        return outstandingCommand !=null && 
        outstandingCommand.equals(ClipCaptureState.CommandKind.Send);
    }
    
    public boolean isMyClearClip(ClipCaptureState state) {
        if (state != captureState) {
            return false;
        }
        CommandKind outstandingCommand = state.getOutstandingCommand();
        return outstandingCommand !=null && 
        outstandingCommand.equals(ClipCaptureState.CommandKind.Clear);
    }
}
