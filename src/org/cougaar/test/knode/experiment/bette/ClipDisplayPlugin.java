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
	private ClipHolder displayingClip = null;
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
    	// is new Clip is newer than displaying Clip?
		if (displayingClip == null
				|| displayingClip.getStartTime() < clip.getStartTime()) {
			// make new clip the displaying clip
			displayingClip = null;
			// turn off old clip's looping display
			if (displayNextSchedulable != null) {
				displayNextSchedulable.cancel();
				displayNextSchedulable = null;
			}
			displayingClip = clip;
			if (clip.isDone()) {
				// Clip is done and may have a subset of images loaded
				startLoopingDisplay(clip);
			}
			log.shout("added Display Clip");
		}
	}

    // Display While Grabbing
    @Cougaar.Execute(on = Subscribe.ModType.CHANGE, when = "isMyClipNotDone")
    public void executeDisplayImageWhileGrabing(ClipHolder clip) {
        ImageLoop loop = clip.getImageLoop();
        ImageHolder  lastImage = loop.getLastImage();
        frame.update(lastImage.getImage(), lastImage.getTimeStamp());
    }
    
    // Start Looping, if not already started
    @Cougaar.Execute(on = Subscribe.ModType.CHANGE, when = "isMyClipDone")
    public void executeStartClipLooping(ClipHolder clip) {
        if (clip.equals(displayingClip)) {
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
    
    // Stop looping when Clip is removed
	@Cougaar.Execute(on = Subscribe.ModType.REMOVE, when = "isMyClip")
	public void executeStopClipLooping(ClipHolder clip) {
		if (clip.equals(displayingClip)) {
			displayingClip = null;
			if (displayNextSchedulable != null) {
				displayNextSchedulable.cancel();
				displayNextSchedulable = null;
			}
			frame.clearImage();
			log.info("removed Display Clip");
		}
	}

    private class DisplayNextImageRunnable implements Runnable {
		private ClipHolder clip;
		private long loopCurrentTime;
		private Schedulable schedulable;

		public DisplayNextImageRunnable(
				ClipHolder clip, 
				long loopDisplayTime) {
			super();
			this.clip = clip;
			this.loopCurrentTime = loopDisplayTime;
		}
		
		public void setSchedulable(Schedulable schedulable) {
			this.schedulable = schedulable;
		}

		public void run() {
			//Check if time to stop
			if (schedulable != displayNextSchedulable || ! clip.equals(displayingClip)) {
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
			scheduleNextDisplay(waitTime, nextLoopTime, clip);
		}

	}
    
	private void scheduleNextDisplay(long waitTime, long nextLoopTime, ClipHolder clip ) {
		DisplayNextImageRunnable displayNext = new DisplayNextImageRunnable(clip, nextLoopTime);
		displayNextSchedulable = threadService.getThread(ClipDisplayPlugin.this,displayNext,"DisplayImageFromClip",ThreadService.BEST_EFFORT_LANE);
		displayNext.setSchedulable(displayNextSchedulable);
		displayNextSchedulable.schedule(waitTime);
	}

    public boolean isMyClipNotDone(ClipHolder clip) {
        return clip.equals(displayingClip) &&
        !clip.isDone();
    }

    public boolean isMyClipDone(ClipHolder clip) {
        return clip.equals(displayingClip) &&
        clip.isDone() && !clip.isSend();
    }

    public boolean isMyClip(ClipHolder clip) {
        return clip.getClipName().equals(clipName);
    }

    public void quit() {
        log.warn("Got Quit Command, Ignoring");
    }

}
