/*
 *
 * Copyright 2007 by BBN Technologies Corporation
 *
 */
package org.cougaar.test.knode.experiment.bette;

import org.cougaar.core.plugin.TodoPlugin;
import org.cougaar.util.annotations.Cougaar;
import org.cougaar.util.annotations.Subscribe;

public class ClipCapturePlugin
        extends TodoPlugin
        implements Quitable {
    private ClipCaptureFrame frame;
    private long lastCount = 0;
    private long streamIncarnation=0;
    
    @Cougaar.Arg(name = "streamName", defaultValue = "DefaultStream", 
                 description = "Name of the Stream")
    public String streamName;

    @Cougaar.Arg(name = "displayImages", defaultValue = "true", description = "Images should be displayed on GUI")
    public boolean isDisplayGifs;

    @Cougaar.Arg(name = "title", defaultValue = "Slide Client", description = "text for title on slide viewer frame")
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
        frame = new ClipCaptureFrame(title, args, this,
                                     blackboard,
                                     new ClipCaptureState(uids,streamName));
        frame.setVisible(true);
    }

    public void quit() {
        log.warn("Got Quit Command, Ignoring");
    }

}
