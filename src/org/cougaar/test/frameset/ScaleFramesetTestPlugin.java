/*
 * <copyright>
 *  
 *  Copyright 1997-2004 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects
 *  Agency (DARPA).
 * 
 *  You can redistribute this software and/or modify it under the
 *  terms of the Cougaar Open Source License as published on the
 *  Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 * </copyright>
 */

package org.cougaar.test.frameset;

import java.text.DecimalFormat;
import java.util.Properties;
import java.util.Set;

import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.plugin.TodoPlugin;
import org.cougaar.core.qos.frame.DataFrame;
import org.cougaar.core.qos.frame.FrameSet;
import org.cougaar.core.qos.frame.FrameSetService;
import org.cougaar.core.qos.frame.scale.Root;
import org.cougaar.util.annotations.Cougaar;
import org.cougaar.util.annotations.Subscribe;

/*
 * Runs the scale test framset through a series of operations
 * The frameset is created using standard FrameSetLoaderPlugin.
 */
public class ScaleFramesetTestPlugin extends TodoPlugin implements
		FrameSetService.Callback {

	@Cougaar.Arg(name = "frame-set", defaultValue = "ScaleTest", description = "frameset to be tested")
	private String frameSetName;

	private FrameSet frameset;
	private static final DecimalFormat f2_1 = new DecimalFormat("0.0");
	private static final DecimalFormat f2_3 = new DecimalFormat("0.000");

	@Override
   public void start() {
		super.start();
		//TODO use Annotated Service lookup
		ServiceBroker sb = getServiceBroker();
		FrameSetService fss = sb.getService(this,
				FrameSetService.class, null);
		if (fss == null) {
			log.error("Couldn't find FrameSetService");
		} else {
			frameset = fss.findFrameSet(frameSetName, this);
		}
	}

	//Initialize Root
	@Cougaar.Execute(on = Subscribe.ModType.ADD, when = "isMyRoot")
	public void initializeRoot(Root root) {
		root.setRootSlotFloat(1);
		//TODO publish change?
	}

	//Increment to 100,000
	@Cougaar.Execute(on = Subscribe.ModType.CHANGE, when = "isMyRoot")
	public void incrementRoot(Root root) {
		float currentValue = root.getRootSlotFloat();
		if (currentValue < 100000) {
			root.setRootSlotFloat(currentValue + 1);
			//TODO publish change?
		} else {
			removeFrames();
		}
	}
	
	private void removeFrameType(String prototype ) {
		long startTime=System.nanoTime();
		Properties slotsWithValues = new Properties();
		Set<DataFrame> frames = frameset.findFrames(prototype, slotsWithValues);
		int size = frames.size();
		for (DataFrame frame : frames) {
			frameset.removeFrameAndRelations(frame);
		}
		frames = null;
		frames = frameset.findFrames("level1", slotsWithValues);
		if (frames.size() != 0) {
			log.error("After delete, " +frames.size()+ " " + prototype+ " frames were not removed");
		}
		if (log.isShoutEnabled()) {
			double deleteTime = (System.nanoTime()-startTime)/1000000000.0;
			double deletesPerMilli = size/(deleteTime*1000);
			log.shout ("Removed " +size+ " " +prototype+ " frames in " +f2_3.format(deleteTime)+ " seconds" +
					  "(" +f2_1.format(deletesPerMilli)+ " deletes/ms)");
		}
		
	}

	private void removeFrames() {
		removeFrameType("level2");
		removeFrameType("level1");
	}

	// FrameSetService.Callback interface
	public void frameSetAvailable(String name, FrameSet set) {
		this.frameset = set;
	}

	// Predicate to check if a Root has the same frameset as ours
	public boolean isMyRoot(Root aRoot) {
		return aRoot.getFrameSet() == frameset;
	}

}
