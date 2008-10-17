/*
 *
 * Copyright 2007 by BBN Technologies Corporation
 *
 */
package org.cougaar.test.knode.experiment.bette;

import org.cougaar.core.plugin.TodoPlugin;
import org.cougaar.util.annotations.Cougaar;
import org.cougaar.util.annotations.Subscribe;

public class ClipDisplayPlugin
        extends TodoPlugin
        implements Quitable {
    private ImageFrame frame;
    private long lastCount = 0;
    private long streamIncarnation=0;
    
    @Cougaar.Arg(name = "clipName", defaultValue = "DefaultClip", 
                 description = "Name of the clip")
    public String clipName;

    @Cougaar.Arg(name = "title", defaultValue = "ClipDisplay", description = "text for title on slide viewer frame")
    public String title;
    
    @Cougaar.Arg(name = "xPosition", defaultValue = "0", description = "X Position for Display window")
    public int xPos;

    @Cougaar.Arg(name = "yPosition", defaultValue = "20", description = "y Position for Display window")
    public int yPos;


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

    @Cougaar.Execute(on = Subscribe.ModType.CHANGE, when = "isMyClip")
    public void executeNewQueryRelay(ClipHolder clip) {
        ImageLoop loop = clip.getImageLoop();
        ImageHolder  lastImage = loop.getLastImage();
        frame.update(lastImage.getImage(), lastImage.getTimeStamp());
    }

    public boolean isMyClip(ClipHolder clipHolder) {
        return clipHolder.getClipName().equals(clipName);
    }

    public void quit() {
        log.warn("Got Quit Command, Ignoring");
    }

}
