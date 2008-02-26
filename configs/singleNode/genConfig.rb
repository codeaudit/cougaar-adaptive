@societyName="singleNode"
@nodeName="Node1"

def societyConfig(x)
 # Long test take a while to startup,
 # But small tests should shutdown quickly
 workerTimeout=50*x +5000
 nodeTimeout= workerTimeout + 10000
 idleTimeout= 2*workerTimeout+ 20000
  return %{<?xml version='1.0'?>
<!--
#{@societyName}: Test Society for #{x} pingers on Single Node
-->
<society>
  <node name="#{@nodeName}">
    <component class="org.cougaar.test.ping.regression.PingSequencerPlugin">
      <argument name="suiteName" value="#{@societyName} #{x}" />
      <argument name="nodeCount" value="1" />
      <argument name="defaultWorkerTimeout" value="#{workerTimeout}" />
      <argument name="defaultNodeTimeout" value="#{nodeTimeout}" />
      <argument name="csvFileName" value="#{@societyName}.csv" />
      <argument name="collectionLength" value="20000" />
      <argument name="steadyStateWait" value="1000" />
    </component>
    <component class="org.cougaar.test.regression.RegressionAggregatorPlugin">
      <argument name="workerCount" value="#{x}" />
      <argument name="maxIdleTime" value="#{idleTimeout}" />
    </component>
	
    <agent name='NameServer' class='org.cougaar.core.agent.SimpleAgent'>
      <facet role='NameServer'/>
      <component class='org.cougaar.core.wp.server.Server'/>
    </agent>

    #{pingerConfig(x)}

  </node>
</society>
}
end

def pingerConfig(j)
 result=""
 1.upto(j) { |n| 
    src="Src#{n}"
    snk="Snk#{n}"
    result << %{
      <!--Pinger #{n} -->
    <agent name="#{src}">
      <component class="org.cougaar.test.ping.regression.PingTesterPlugin">
         <argument name="workerId" value="#{src}"/>
         <argument name="pingerCount" value="1"/>
       </component>

      <component class="org.cougaar.test.ping.PingSenderPlugin">
        <argument name="pluginId" value="1"/>
        <argument name="targetAgent" value="#{snk}"/>
        <argument name="targetPlugin" value="1"/>
        <argument name="preambleCount" value="3" />
      </component>
    </agent>
    <agent name="#{snk}">
      <component class="org.cougaar.test.ping.PingReceiverPlugin">
        <argument name="pluginId" value="1"/>
      </component>
    </agent>}
  }
 return result
end

# Main: Generate a set of society config files
[1, 2, 3, 4, 5, 7, 10, 15, 20, 30, 40, 50 ].each { |i|
    open("test#{i}.xml","w") do |file|
    file.puts(societyConfig(i))
  end
}
 

