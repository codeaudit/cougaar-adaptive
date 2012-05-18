/*
 *
 * Copyright 2007 by BBN Technologies Corporation
 *
 */
package org.cougaar.test.knode.experiment.bette;

import org.cougaar.core.plugin.TodoPlugin;
import org.cougaar.util.annotations.Cougaar;
import org.cougaar.util.annotations.Subscribe;

public class VideoStreamDisplayPlugin
        extends TodoPlugin
        implements Quitable {
    private ImageFrame frame;
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


    @Override
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
        frame = new ImageFrame(title, args, this);
        frame.setVisible(true);
    }

    @Cougaar.Execute(on = Subscribe.ModType.ADD, when = "isMyImageHolder")
    public void executeNewQueryRelay(ImageHolder imageHolder) {
        long newIncarnation = imageHolder.getCreatorIncarnation();
        long newCount = imageHolder.getCount();
        long newTimeStamp = imageHolder.getTimeStamp();
        if (streamIncarnation == newIncarnation) {

            if (newCount == lastCount + 1) {
                // Normal case
                lastCount = newCount;
                frame.update(imageHolder.getImage(), newTimeStamp);
                if (log.isInfoEnabled()) {
                    long now = System.currentTimeMillis();
                    log.info("Image " + imageHolder.getCount() + " transfer delay ="
                            + (now - newTimeStamp));
                }
            } else if (newCount > lastCount + 1) {
                // Dropped case
                long numberDropped = (newCount - lastCount);
                log.warn("Dropped " + numberDropped + " Images at " + newCount);
                lastCount = newCount;
                frame.update(imageHolder.getImage(), newTimeStamp);
            } else if (newCount == lastCount) {
                // Duplicate Case
                log.warn("Duplicate Image " + newCount);
                // don't display or update lastCount
            } else if (newCount < lastCount) {
                // Out of Order Case
                log.warn("Old image " + newCount + ". last was " + lastCount);
                // don't display or update lastCount
            }

        } else if (newIncarnation > streamIncarnation) {
            // Start of New Stream
            // initialize new stream incarnation
            streamIncarnation = newIncarnation;
            lastCount = newCount;
            frame.update(imageHolder.getImage(), newTimeStamp);
            if (log.isInfoEnabled()) {
                log.info("Start new image stream " + imageHolder.getStreamName() + " incarnation="
                        + newIncarnation);
            }
        } else if (newIncarnation < streamIncarnation) {
            // Image from old Stream
            // ignore
            log.warn("Old incarnation " + newIncarnation + " image " + newCount);

        }
        // Clean up: remove Image Holder
        blackboard.publishRemove(imageHolder);
    }

    public boolean isMyImageHolder(ImageHolder imageHolder) {
        return imageHolder.getStreamName().equals(streamName);
    }

    public void quit() {
        log.warn("Got Quit Command, Ignoring");
    }

}
