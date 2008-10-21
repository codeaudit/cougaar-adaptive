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

public class ClipDisplayPlugin
        extends TodoPlugin
        implements Quitable {
    private ImageFrame frame;
	private Schedulable displayNextSchedulable;
	private ClipHolder loopingClip = null;
	private ThreadService threadService;
	
    
    @Cougaar.Arg(name = "clipName", defaultValue = "DefaultClip", 
                 description = "Name of the clip")
    public String clipName;

    @Cougaar.Arg(name = "title", defaultValue = "ClipDisplay", description = "text for title on slide viewer frame")
    public String title;
    
    @Cougaar.Arg(name = "xPosition", defaultValue = "0", description = "X Position for Display window")
    public int xPos;

    @Cougaar.Arg(name = "yPosition", defaultValue = "20", description = "y Position for Display window")
    public int yPos;
    public void start() {
        super.start();
        ServiceBroker sb = getServiceBroker();
        threadService = sb.getService(this,ThreadService.class, null );
    }

    protected void setupSubscriptions() {
        super.setupSubscriptions();
        // Setup Swing frame
        String[] args = new String[4];
        args[0] = "-x-position";
        args[1] = Integer.toString(xPos);
        args[2] = "-y-position";
        args[3] = Integer.toString(yPos);
        frame = new ImageFrame(title, args, this);
        frame.setVisible(true);
    }
    
    // New Clip
    @Cougaar.Execute(on = Subscribe.ModType.ADD, when = "isMyClip")
    public void executeNewClip(ClipHolder clip) {
    	loopingClip=null;
    	if (displayNextSchedulable != null) {
    		displayNextSchedulable.cancel();
    		displayNextSchedulable=null;
    	}
        loopingClip=clip;
    	log.shout("added Display Clip");
    }

    // Display While Grabbing
    @Cougaar.Execute(on = Subscribe.ModType.CHANGE, when = "isMyClipNotDone")
    public void executeDisplayImageWhileGrabing(ClipHolder clip) {
        ImageLoop loop = clip.getImageLoop();
        ImageHolder  lastImage = loop.getLastImage();
        frame.update(lastImage.getImage(), lastImage.getTimeStamp());
    }
    
    // Start Looping
    @Cougaar.Execute(on = Subscribe.ModType.CHANGE, when = "isMyClipDone")
    public void executeStartClipLooping(ClipHolder clip) {
    	loopingClip=clip;
    	ImageLoop loop = clip.getImageLoop();
    	if (loop.getFirstTime() != null) {
    	    long startTime = loop.getFirstTime();
    	    Runnable displayNext = new DisplayNextImageRunnable(threadService,clip, frame, startTime);
    	    displayNextSchedulable = threadService.getThread(ClipDisplayPlugin.this,displayNext,"DisplayImageFromClip",ThreadService.BEST_EFFORT_LANE);
    	    displayNextSchedulable.schedule(startTime);
    	    log.info("Start Looping");
    	}
    }
    
    // Stop looping when Clip is removed
    @Cougaar.Execute(on = Subscribe.ModType.REMOVE, when = "isMyClip")
    public void executeStopClipLooping(ClipHolder clip) {
    	loopingClip=null;
    	if (displayNextSchedulable != null) {
    		displayNextSchedulable.cancel();
    		displayNextSchedulable=null;
    	}
    	frame.clearImage();
    	log.info("removed Display Clip");
    }

    private class DisplayNextImageRunnable implements Runnable {
		private ThreadService threadService;
		private ClipHolder clip;
		private ImageFrame frame;
		private long loopCurrentTime;

		public DisplayNextImageRunnable(
				ThreadService threadService,
				ClipHolder clip, 
				ImageFrame frame,
				long loopDisplayTime) {
			super();
			this.threadService = threadService;
			this.clip = clip;
			this.frame = frame;
			this.loopCurrentTime = loopDisplayTime;
		}

		public void run() {
			//Check if time to stop
			if (displayNextSchedulable != this) {
				return;
			}
			if (loopingClip == null) {
				displayNextSchedulable = null;
				return;
			}
			// Find image for current time
			ImageLoop loop=clip.getImageLoop();
			ImageHolder image = loop.getImage(loopCurrentTime);
			// Display image
			if (image!=null) {
				frame.update(image.getImage(), image.getTimeStamp());
			}
			// find next frame time
			long waitTime = loop.getNextWallclockTimeWithWrap(loopCurrentTime);
			long nextLoopTime = loop.getNextLoopTimeWithWrap(loopCurrentTime);
			// Schedule next run
			Runnable displayNext = new DisplayNextImageRunnable(threadService,clip, frame, nextLoopTime);
			displayNextSchedulable = threadService.getThread(ClipDisplayPlugin.this,displayNext,"DisplayImageFromClip",ThreadService.BEST_EFFORT_LANE);
			displayNextSchedulable.schedule(waitTime);
		}

	}
    
    
    public boolean isMyClipNotDone(ClipHolder clipHolder) {
        return clipHolder.getClipName().equals(clipName) &&
        !clipHolder.isDone();
    }

    public boolean isMyClipDone(ClipHolder clipHolder) {
        return clipHolder.getClipName().equals(clipName) &&
        clipHolder.isDone() && !clipHolder.isSend();
    }

    public boolean isMyClip(ClipHolder clipHolder) {
        return clipHolder.getClipName().equals(clipName);
    }

    public void quit() {
        log.warn("Got Quit Command, Ignoring");
    }

}
