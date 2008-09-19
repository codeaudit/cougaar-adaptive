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

    @Cougaar.Arg(name = "displayImages", defaultValue = "true", description = "Images should be displayed on GUI")
    public boolean isDisplayGifs;

    @Cougaar.Arg(name = "title", defaultValue = "Slide Client", description = "text for title on slide viewer frame")
    public String title;

    protected void setupSubscriptions() {
        super.setupSubscriptions();
        // Setup Swing frame
        String[] args = new String[2];
        args[0] = "-show-slides";
        args[1] = Boolean.toString(isDisplayGifs);
        frame = new ImageFrame(title, args, this);
        frame.setVisible(true);
    }

    @Cougaar.Execute(on = Subscribe.ModType.ADD, when = "isMyImageHolder")
    public void executeNewQueryRelay(ImageHolder imageHolder) {
        // Display an image
        frame.update(imageHolder.getImage(), imageHolder.getTimeStamp());
        // removeImage
        blackboard.publishRemove(imageHolder);
    }

    public boolean isMyImageHolder(ImageHolder imageHolder) {
        return imageHolder.length() >0;
    }

    public void quit() {
    }

}
