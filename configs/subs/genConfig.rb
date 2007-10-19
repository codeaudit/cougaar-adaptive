@societyName="subs"
@nodeName="Node1"

def societyConfig(x)
 # Long test take a while to startup,
 # But small tests should shutdown quickly
 workerTimeout=50*x +1000
 nodeTimeout= workerTimeout + 10000
 idleTimeout= 2*workerTimeout+ 20000
  return %{<?xml version='1.0'?>
<!--
#{@societyName}: Test Society for one extra plugin with #{x} subscriptions
  on a single agents blackboard
-->
<society>
  <node name="#{@nodeName}">
    <component class="org.cougaar.test.regression.ping.PingSequencerPlugin">
      <argument name="suiteName" value="#{@societyName} #{x}" />
      <argument name="nodeCount" value="1" />
      <argument name="defaultWorkerTimeout" value="#{workerTimeout}" />
      <argument name="defaultNodeTimeout" value="#{nodeTimeout}" />
      <argument name="csvFileName" value="#{@societyName}.csv" />
      <argument name="collectionLength" value="15000" />
      <argument name="steadyStateWait" value="1000" />
    </component>
    <component class="org.cougaar.test.regression.RegressionAggregatorPlugin">
      <argument name="workerCount" value="1" />
      <argument name="maxIdleTime" value="#{idleTimeout}" />
    </component>
	
    <agent name='NameServer' class='org.cougaar.core.agent.SimpleAgent'>
      <facet role='NameServer'/>
      <component class='org.cougaar.core.wp.server.Server'/>
    </agent>

    <agent name="BBtester">
      <component class="org.cougaar.test.regression.ping.PingBBTesterPlugin">
         <argument name="workerId" value="source1"/>
         <argument name="pingerCount" value="1"/>
       </component>

      <!--Single Pinger  -->
      <component class="org.cougaar.test.regression.ping.PingBBSenderPlugin">
        <argument name="pluginId" value="Src"/>
        <argument name="targetAgent" value="BBtester"/>
        <argument name="targetPlugin" value="Snk"/>
        <argument name="preambleCount" value="3" />
      </component>
      <component class="org.cougaar.test.regression.ping.PingBBReceiverPlugin">
        <argument name="pluginId" value="Snk"/>
      </component>

      <component class="org.cougaar.test.regression.ping.PingBBSubscriberPlugin">
        <argument name="pluginId" value="Subs1"/>
        <argument name="wasteSubscriptionTime" value="0"/>
        <argument name="numSubscriptions" value="#{x}"/>
      </component>

    </agent>
  </node>
</society>
}
end


# main
[1, 10, 100, 1000].each { |i|
    open("test#{i}.xml","w") do |file|
    file.puts(societyConfig(i))
  end
}
 

