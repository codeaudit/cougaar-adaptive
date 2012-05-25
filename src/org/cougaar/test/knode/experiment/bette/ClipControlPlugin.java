/*
 *
 * Copyright 2007 by BBN Technologies Corporation
 *
 */
package org.cougaar.test.knode.experiment.bette;

import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.plugin.TodoPlugin;
import org.cougaar.core.service.ThreadService;
import org.cougaar.core.thread.Schedulable;
import org.cougaar.util.annotations.Cougaar;
import org.cougaar.util.annotations.Subscribe;

public class ClipControlPlugin
      extends TodoPlugin
      implements ClipControlInterface {

   private ClipControlFrame frame;
   private ClipCaptureState clipCaptureState;
   private ClipHolder capturingClip;
   private Schedulable displayNextSchedulable;
   private ThreadService threadService;

   @Cougaar.Arg(defaultValue = "DefaultClip", description = "Name of the Clip")
   public String clipName;

   // TODO should the clip be displayed in separate window or on control panel?
   @Cougaar.Arg(name = "displayImages", defaultValue = "true", description = "Images should be displayed on GUI during capture")
   public boolean isDisplayGifs;

   @Cougaar.Arg(defaultValue = "Clip Capture", description = "text for title on clip capture frame")
   public String title;

   @Cougaar.Arg(defaultValue = "0", description = "X Position for Display window")
   public int xPosition;

   @Cougaar.Arg(defaultValue = "20", description = "y Position for Display window")
   public int yPosition;

   @Override
   public void start() {
      super.start();
      ServiceBroker sb = getServiceBroker();
      threadService = sb.getService(this, ThreadService.class, null);
   }

   @Override
   protected void setupSubscriptions() {
      super.setupSubscriptions();
      // Setup Swing frame
      clipCaptureState = new ClipCaptureState(uids, clipName);
      blackboard.publishAdd(clipCaptureState);
      String[] args = new String[6];
      args[0] = "-show-slides";
      args[1] = Boolean.toString(isDisplayGifs);
      args[2] = "-x-position";
      args[3] = Integer.toString(xPosition);
      args[4] = "-y-position";
      args[5] = Integer.toString(yPosition);
      frame = new ClipControlFrame(title, args, this);
      frame.setVisible(true);
   }

   // Clip Capture interface
   // Called by a swing thread
   public void quit() {
      log.warn("Got Quit Command, Ignoring");
   }

   public void startCapture() {
      executeLater(new Runnable() {
         public void run() {
            log.info("Got START Capture Command");
            clipCaptureState.setOutstandingCommand(ClipCaptureState.CommandKind.StartCapture);
            blackboard.publishChange(clipCaptureState);
         }
      });
   }

   public void stopCapture() {
      executeLater(new Runnable() {
         public void run() {
            log.info("Got STOP Capture Command");
            clipCaptureState.setOutstandingCommand(ClipCaptureState.CommandKind.StopCapture);
            blackboard.publishChange(clipCaptureState);
         }
      });
   }

   public void clear() {
      executeLater(new Runnable() {
         public void run() {
            log.info("Got Clear Command");
            capturingClip = null;
            executeStopClipLooping();
            clipCaptureState.setOutstandingCommand(ClipCaptureState.CommandKind.Clear);
            blackboard.publishChange(clipCaptureState);
         }
      });
   }

   public void send() {
      executeLater(new Runnable() {
         public void run() {
            log.info("Got Send Command");
            capturingClip = null;
            executeStopClipLooping();
            clipCaptureState.setOutstandingCommand(ClipCaptureState.CommandKind.Send);
            blackboard.publishChange(clipCaptureState);
         }
      });
   }

   // Display Clip while captured
   // New Clip
   @Cougaar.Execute(on = Subscribe.ModType.ADD, when = "isMyClip")
   public void executeNewClip(ClipHolder clip) {
      // is new Clip is newer than displaying Clip?
      if (capturingClip == null || capturingClip.getStartTime() < clip.getStartTime()) {
         // make new clip the displaying clip
         capturingClip = null;
         // turn off old clip's looping display
         // if (displayNextSchedulable != null) {
         // displayNextSchedulable.cancel();
         // displayNextSchedulable = null;
         // }
         capturingClip = clip;
         // if (clip.isDone()) {
         // // Clip is done and may have a subset of images loaded
         // startLoopingDisplay(clip);
         // }
         log.shout("added Display Clip");
      }
   }

   // Start Looping, if not already started
   @Cougaar.Execute(on = Subscribe.ModType.CHANGE, when = "isMyClipDone")
   public void executeStartClipLooping(ClipHolder clip) {
      if (clip.equals(capturingClip)) {
         startLoopingDisplay(clip);
      }
   }

   private void startLoopingDisplay(ClipHolder clip) {
      ImageLoop loop = clip.getImageLoop();
      if (loop.getFirstTime() != null) {
         long startTime = loop.getFirstTime();
         scheduleNextDisplay(startTime, startTime, clip);
         log.info("Start Looping");
      }
   }

   public void executeStopClipLooping() {
      capturingClip = null;
      if (displayNextSchedulable != null) {
         displayNextSchedulable.cancel();
         displayNextSchedulable = null;
      }
      frame.clearImage();
      log.info("removed Display Clip");
   }

   private class DisplayNextImageRunnable
         implements Runnable {
      private ClipHolder clip;
      private long loopCurrentTime;
      private Schedulable schedulable;

      public DisplayNextImageRunnable(ClipHolder clip, long loopDisplayTime) {
         super();
         this.clip = clip;
         this.loopCurrentTime = loopDisplayTime;
      }

      public void setSchedulable(Schedulable schedulable) {
         this.schedulable = schedulable;
      }

      public void run() {
         // Check if time to stop
         if (schedulable != displayNextSchedulable || !clip.equals(capturingClip)) {
            return;
         }
         // Find image for current time
         ImageLoop loop = clip.getImageLoop();
         ImageHolder image = loop.getImage(loopCurrentTime);
         // Display image
         if (image != null) {
            frame.update(image.getImage(), image.getTimeStamp());
         }
         // find next frame time
         long waitTime = loop.getNextWallclockTimeWithWrap(loopCurrentTime);
         long nextLoopTime = loop.getNextLoopTimeWithWrap(loopCurrentTime);
         // Schedule next run
         scheduleNextDisplay(waitTime, nextLoopTime, clip);
      }

   }

   private void scheduleNextDisplay(long waitTime, long nextLoopTime, ClipHolder clip) {
      DisplayNextImageRunnable displayNext = new DisplayNextImageRunnable(clip, nextLoopTime);
      displayNextSchedulable =
            threadService.getThread(ClipControlPlugin.this, displayNext, "DisplayImageFromClip", ThreadService.BEST_EFFORT_LANE);
      displayNext.setSchedulable(displayNextSchedulable);
      displayNextSchedulable.schedule(waitTime);
   }

   // Display While Grabbing
   @Cougaar.Execute(on = Subscribe.ModType.CHANGE, when = "isMyClipNotDone")
   public void executeDisplayImageWhileGrabing(ClipHolder clip) {
      ImageLoop loop = clip.getImageLoop();
      ImageHolder lastImage = loop.getLastImage();
      frame.update(lastImage.getImage(), lastImage.getTimeStamp());
   }

   public boolean isMyClipNotDone(ClipHolder clip) {
      return clip.equals(capturingClip) && !clip.isDone();
   }

   public boolean isMyClipDone(ClipHolder clip) {
      return clip.equals(capturingClip) && clip.isDone() && !clip.isSend();
   }

   public boolean isMyClip(ClipHolder clip) {
      return clip.getClipName().equals(clipName);
   }

}
