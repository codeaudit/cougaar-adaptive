package org.cougaar.test.knode.experiment.bette;

import java.util.Collection;
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
    
    /*
     * The time for the next image, inclusive.
     * If there is an image for the current time, return currentTime
     */
    public ImageHolder getNextImage(long currentTime ) {
            return schedule.get(getNextTime(currentTime));
    }
    
    /*
     * The time for the next image, inclusive.
     * If there is an image for the current time, return currentTime
     */
    public long getNextTime(long currentTime ) {
        if (endTime == 0) { 
            return 0; }
        long phase = currentTime % endTime;
        return schedule.tailMap(phase).firstKey();
    }
    
    /*
     * The loop time for the next image, exclusive with wrap.
     * Scan forward to the next image (not including the image at the current time)
     * If the current time is last image then skip to the first image
     */
   public long getNextLoopTimeWithWrap(long currentTime) {
	   // check for wrap
	   if (getLastTime() <= currentTime) {
		   return getFirstTime();
	   } else  {
		   return getNextTime(currentTime +1 );
	   }
    }
   
   /*
    * The wall clock time for setting a timer from current time to next image, exclusive with wrap.
    * Scan forward to the next image (not including the image at the current time).
    * Return the delta time between current time and the next image.
    * If the current time is last image then skip to the first image and 
    * include remaining time to end of loop and beginning of loop
    */
   public long getNextWallclockTimeWithWrap(long currentTime) {
	   long loopTime = getNextLoopTimeWithWrap(currentTime);
	   if (loopTime >= currentTime) {
		   return loopTime - currentTime;
	   } else {
		   return (endTime - currentTime) + (loopTime -getStartTime());
	   }
   }
 
    /* Image at loop time.
     * If no image at that time, returns null
     */
    public ImageHolder getImage(long time) {
    	return schedule.get(time);
    }
    
    public Collection<ImageHolder>  images() {
    	return schedule.values();
    }
    /*
     * Loop time for first image
     * if no image, returns null
     */
    public Long getFirstTime() {
        return schedule.firstKey();
    }
    
    public ImageHolder getFirstImage() {
        Long time = getFirstTime();
        return (time == null) ? null : schedule.get(time)  ;
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
