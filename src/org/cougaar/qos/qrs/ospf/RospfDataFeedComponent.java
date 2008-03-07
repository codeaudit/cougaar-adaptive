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

package org.cougaar.qos.qrs.ospf;

import org.cougaar.core.qos.metrics.DataFeedRegistrationService;
import org.cougaar.core.qos.metrics.QosComponent;
import org.cougaar.core.service.LoggingService;
import org.cougaar.util.ConfigFinder;
import org.cougaar.util.annotations.Cougaar.Arg;
import org.cougaar.util.annotations.Cougaar.ObtainService;

/**
 * This Components uses the {@link DataFeedRegistrationService} to register an
 * RSS properties feed that uses the {@link ConfigFinder} to resolve the URL.
 */
public class RospfDataFeedComponent extends QosComponent {

    @Arg(name="transform", defaultValue="org.cougaar.qos.qrs.ospf.ClosenessTransform")
    public String transformClassName;
    
    @Arg(name="name")
    public String name;
    
    @Arg(name="snmpArgs")
    public String snmpArgsString;
    
    @Arg(name="pollPeriod", defaultValue="1000")
    public long pollPeriod;
    
    @ObtainService
    public DataFeedRegistrationService svc;
    
    @ObtainService
    public LoggingService log;
    
    public RospfDataFeedComponent() {
    }

    public void load() {
        super.load();
        String[] snmpArgs = snmpArgsString.split(" ");
        RospfDataFeed feed = new RospfDataFeed(transformClassName, pollPeriod, snmpArgs);
        svc.registerFeed(feed, name);
        getServiceBroker().releaseService(this, DataFeedRegistrationService.class, svc);

    }
}
