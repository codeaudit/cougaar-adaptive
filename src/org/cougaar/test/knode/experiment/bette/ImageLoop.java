package org.cougaar.test.knode.experiment.bette;

import java.util.SortedMap;
import java.util.TreeMap;

/*
 * Holds a series of time stamped images
 * The loop goes from 0 to endTime.
 * The time between images is variable (i.e. not deterministic), this allow missing images
 */
public class ImageLoop {
    
    SortedMap<Long,ImageHolder>  schedule =  new TreeMap<Long,ImageHolder>();
    long endTime;
    
    public ImageLoop(long loopTime) {
            super();
            this.endTime = loopTime;
    }
    
    

    /*
     * Add an image for time relative to startTime.
     */
    public void add(long time, ImageHolder image) {
            if (endTime > 0 && time > endTime) {
                    System.out.println("Ignoring add of image after Loop Time=" +endTime + "image time=" + time);
                    return;
            }
            schedule.put(time,image);
    }
    
    public ImageHolder getNextImage(long currentTime ) {
            return schedule.get(getNextTime(currentTime));
    }
    
    public long getNextTime(long currentTime ) {
        if (endTime == 0) { 
            return 0; }
        long phase = currentTime % endTime;
        return schedule.tailMap(phase).firstKey();
    }

    public Long getFirstTime() {
        return schedule.firstKey();
    }
    
    public ImageHolder getFirstImage() {
        Long time = getFirstTime();
        return (time ==null) ? null : schedule.get(time)  ;
    }
    
    public Long getLastTime() {
        return schedule.lastKey();
    }
    
    public ImageHolder getLastImage() {
        Long time = getLastTime();
        return (time ==null) ? null : schedule.get(time)  ;
    }
     
    
    public long getEndTime() {
            return endTime;
    }

    public void setEndTime(long loopTime) {
            this.endTime = loopTime;
    }
    
    public int getImageCount() {
        return schedule.size();
    }
    
    public long getStartTime () {
        return 0;
    }
}
