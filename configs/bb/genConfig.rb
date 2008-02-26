@societyName="BB"
@nodeName="Node1"

def societyConfig(x)
 # Long test take a while to startup,
 # But small tests should shutdown quickly
 workerTimeout=50*x +1000
 nodeTimeout= workerTimeout + 10000
 idleTimeout= 2*workerTimeout+ 20000
  return %{<?xml version='1.0'?>
<!--
#{@societyName}: Test Society for #{x} pingers on a single agents blackboard
-->
<society>
  <node name="#{@nodeName}">
    <component class="org.cougaar.test.ping.regression.PingSequencerPlugin">
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
      <component class="org.cougaar.test.ping.regression.PingBBTesterPlugin">
         <argument name="workerId" value="source1"/>
         <argument name="pingerCount" value="#{x}"/>
       </component>
       #{pingerConfig(x)}
    </agent>
  </node>
</society>
}
end

def pingerConfig(j)
 result=""
 1.upto(j) { |n| 
    result << %{
      <!--Pinger #{n} -->
      <component class="org.cougaar.test.ping.PingBBSenderPlugin">
        <argument name="pluginId" value="Src#{n}"/>
        <argument name="targetAgent" value="BBtester"/>
        <argument name="targetPlugin" value="Snk#{n}"/>
        <argument name="preambleCount" value="3" />
      </component>
      <component class="org.cougaar.test.ping.PingBBReceiverPlugin">
        <argument name="pluginId" value="Snk#{n}"/>
      </component>}
  }
 return result
end

# main
[1, 2, 3, 4, 5, 7, 10, 15, 20, 30, 40, 50].each { |i|
    open("test#{i}.xml","w") do |file|
    file.puts(societyConfig(i))
  end
}
 

