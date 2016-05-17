package org.batfish.datamodel;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.answers.AnswerElement;

import com.fasterxml.jackson.annotation.JsonIdentityReference;

public class FlowHistory implements AnswerElement {

   private Set<Flow> _flows;

   private Map<String, Flow> _flowsByText;

   private Map<String, Map<String, Set<FlowTrace>>> _traces;

   public FlowHistory() {
      _traces = new TreeMap<String, Map<String, Set<FlowTrace>>>();
      _flows = new TreeSet<Flow>();
      _flowsByText = new TreeMap<String, Flow>();
   }

   public void addFlowTrace(Flow flow, String environment, FlowTrace trace) {
      _flows.add(flow);
      String flowText = flow.toString();
      _flowsByText.put(flowText, flow);
      Map<String, Set<FlowTrace>> envTraceSetMap = _traces.get(flowText);
      if (envTraceSetMap == null) {
         envTraceSetMap = new TreeMap<String, Set<FlowTrace>>();
         _traces.put(flow.toString(), envTraceSetMap);
      }
      Set<FlowTrace> traces = envTraceSetMap.get(environment);
      if (traces == null) {
         traces = new TreeSet<FlowTrace>();
         envTraceSetMap.put(environment, traces);
      }
      traces.add(trace);
   }

   public Set<Flow> getFlows() {
      return _flows;
   }

   @JsonIdentityReference(alwaysAsId = true)
   public Map<String, Flow> getFlowsByText() {
      return _flowsByText;
   }

   public Map<String, Map<String, Set<FlowTrace>>> getTraces() {
      return _traces;
   }

   public void setFlows(Set<Flow> flows) {
      _flows = flows;
   }

   public void setFlowsByText(Map<String, Flow> flowsByText) {
      _flowsByText = flowsByText;
   }

   public void setTraces(Map<String, Map<String, Set<FlowTrace>>> traces) {
      _traces = traces;
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder();
      for (Flow flow : _flows) {
         sb.append("Flow: " + flow.toString() + "\n");
         Map<String, Set<FlowTrace>> envTraceSetMap = _traces.get(flow);
         for (String environmentName : envTraceSetMap.keySet()) {
            sb.append(" Environment: " + environmentName + "\n");
            Set<FlowTrace> traces = envTraceSetMap.get(environmentName);
            int i = 0;
            for (FlowTrace trace : traces) {
               i++;
               sb.append("  Trace: " + i + "\n");
               String rawTraceString = trace.toString();
               String traceString = CommonUtil.getIndentedString(
                     rawTraceString, 3);
               sb.append(traceString);
            }
         }
      }
      return sb.toString();
   }

}
