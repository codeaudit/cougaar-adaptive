package org.cougaar.test.knode.experiment.bette;
/**
 * 
 * Class to convert Time as long into byte array
 * Hack for now:
 * Assume that time has granularity of 128 seconds and 1/128 of a second
 * So time is represented as fixed point number in 2 byte array
 */

public class TimeBytesConverter {
	
	
	public static byte[] timeToBytes(long time) {
		byte[] result = new byte [2];
		result[0]= (byte) ((time % 1000) * 128 / 1000); // sub seconds
		result[1] = (byte) ((time/1000) % 128 ); // seconds
		return result;		
	}
	
	public static long bytesToTime(byte[] bytes) {
		if (bytes.length == 2) {
			return (bytes[1] * 1000) + (bytes[0] * 1000 / 128 );
		}else {
			return 0;
		}
	}
}
