/*
 *
 * Copyright 2007 by BBN Technologies Corporation
 *
 */
package org.cougaar.test.knode.experiment.bette;

import org.cougaar.core.plugin.TodoPlugin;
import org.cougaar.util.annotations.Cougaar;

public class ClipControlPlugin
        extends TodoPlugin
        implements ClipControlInterface {
 
	private ClipControlFrame frame;
	private ClipCaptureState clipCaptureState;
    
    @Cougaar.Arg(name = "clipName", defaultValue = "DefaultClip", 
                 description = "Name of the Clip")
    public String clipName;

    //TODO should the clip be displayed in separate window or on control panel?
    @Cougaar.Arg(name = "displayImages", defaultValue = "true", 
    		description = "Images should be displayed on GUI during capture")
    public boolean isDisplayGifs;

    @Cougaar.Arg(name = "title", defaultValue = "Clip Capture", description = "text for title on clip capture frame")
    public String title;
    
    @Cougaar.Arg(name = "xPosition", defaultValue = "0", description = "X Position for Display window")
    public int xPos;

    @Cougaar.Arg(name = "yPosition", defaultValue = "20", description = "y Position for Display window")
    public int yPos;


    protected void setupSubscriptions() {
        super.setupSubscriptions();
        // Setup Swing frame
        String[] args = new String[6];
        args[0] = "-show-slides";
        args[1] = Boolean.toString(isDisplayGifs);
        args[2] = "-x-position";
        args[3] = Integer.toString(xPos);
        args[4] = "-y-position";
        args[5] = Integer.toString(yPos);
        frame = new ClipControlFrame(title, args, this);
        frame.setVisible(true);
    }
    

    // Clip Capture interface
    // Called by a swing thread
    public void quit() {
        log.warn("Got Quit Command, Ignoring");
    }

    public void startCapture() {
    	executeLater( new Runnable() {
    		public void run (){	
    			if (clipCaptureState == null) {
    				clipCaptureState=new ClipCaptureState(uids,clipName);
    				blackboard.publishAdd(clipCaptureState);
    			}
    			log.info("Got START Capture Command");
    			clipCaptureState.setOutstandingCommand(ClipCaptureState.CommandKind.StartCapture);
    			blackboard.publishChange(clipCaptureState);
    		}
    	}
    	);
    }

    public void stopCapture() {
    	executeLater( new Runnable() {
    		public void run (){	
    			log.info("Got STOP Capture Command");
    			clipCaptureState.setOutstandingCommand(ClipCaptureState.CommandKind.StopCapture);
    			blackboard.publishChange(clipCaptureState);
    		}
    	}
    	);
    }

    public void clear() {
    	executeLater( new Runnable() {
    		public void run (){	
    			if (clipCaptureState==null) {
    				log.info("Got Clear Command, Ignoring because no Clip");
    				return;
    			}
    			log.info("Got Clear Command");
    			clipCaptureState.setOutstandingCommand(ClipCaptureState.CommandKind.Clear);
    			blackboard.publishChange(clipCaptureState);
    		}
    	}
    	);
    }

    public void send() {
    	executeLater( new Runnable() {
    		public void run (){	
    			if (clipCaptureState==null) {
    				log.info("Got Send Command, Ignoring because no Clip");
    				return;
    			}
    			log.info("Got Send Command");
    			clipCaptureState.setOutstandingCommand(ClipCaptureState.CommandKind.Send);
    			blackboard.publishChange(clipCaptureState);
    		}
    	}
    	);
    }
}
