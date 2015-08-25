package org.batfish.representation;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.batfish.util.Util;

public class FlowHistory {

   private final Map<Flow, Map<String, Set<FlowTrace>>> _data;

   public FlowHistory() {
      _data = new TreeMap<Flow, Map<String, Set<FlowTrace>>>();
   }

   public void addFlowTrace(Flow flow, String environment, FlowTrace trace) {
      Map<String, Set<FlowTrace>> envTraceSetMap = _data.get(flow);
      if (envTraceSetMap == null) {
         envTraceSetMap = new TreeMap<String, Set<FlowTrace>>();
         _data.put(flow, envTraceSetMap);
      }
      Set<FlowTrace> traces = envTraceSetMap.get(environment);
      if (traces == null) {
         traces = new TreeSet<FlowTrace>();
         envTraceSetMap.put(environment, traces);
      }
      traces.add(trace);
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder();
      for (Flow flow : _data.keySet()) {
         sb.append("Flow: " + flow.toString() + "\n");
         Map<String, Set<FlowTrace>> envTraceSetMap = _data.get(flow);
         for (String environmentName : envTraceSetMap.keySet()) {
            sb.append(" Environment: " + environmentName + "\n");
            Set<FlowTrace> traces = envTraceSetMap.get(environmentName);
            int i = 0;
            for (FlowTrace trace : traces) {
               i++;
               sb.append("  Trace: " + i + "\n");
               String rawTraceString = trace.toString();
               String traceString = Util.getIndentedString(rawTraceString, 3);
               sb.append(traceString);
            }
         }
      }
      return sb.toString();
   }

}
