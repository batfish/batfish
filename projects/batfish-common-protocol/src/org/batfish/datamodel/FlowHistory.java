package org.batfish.datamodel;

import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.answers.AnswerElement;

import com.fasterxml.jackson.annotation.JsonIdentityReference;

public class FlowHistory implements AnswerElement {

   private SortedSet<Flow> _flows;

   private SortedMap<String, Flow> _flowsByText;

   private SortedMap<String, SortedMap<String, SortedSet<FlowTrace>>> _traces;

   public FlowHistory() {
      _traces = new TreeMap<>();
      _flows = new TreeSet<>();
      _flowsByText = new TreeMap<>();
   }

   public void addFlowTrace(Flow flow, String environment, FlowTrace trace) {
      Flow canonicalFlow = flow;
      String flowText = flow.toString();
      if (_flows.contains(flow)) {
         canonicalFlow = _flowsByText.get(flowText);
      }
      _flows.add(canonicalFlow);
      _flowsByText.put(flowText, canonicalFlow);
      SortedMap<String, SortedSet<FlowTrace>> envTraceSetMap = _traces
            .get(flowText);
      if (envTraceSetMap == null) {
         envTraceSetMap = new TreeMap<>();
         _traces.put(canonicalFlow.toString(), envTraceSetMap);
      }
      SortedSet<FlowTrace> traces = envTraceSetMap.get(environment);
      if (traces == null) {
         traces = new TreeSet<>();
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

   public SortedMap<String, SortedMap<String, SortedSet<FlowTrace>>> getTraces() {
      return _traces;
   }

   @Override
   public String prettyPrint() {
      StringBuilder retString = new StringBuilder("\n");
      for (String flowStr : _traces.keySet()) {
         Flow flow = _flowsByText.get(flowStr);
         retString.append(flow.prettyPrint("") + "\n");
         for (String environment : _traces.get(flowStr).keySet()) {
            retString.append("  environment:" + environment + "\n");
            for (FlowTrace trace : _traces.get(flowStr).get(environment)) {
               retString.append(trace.toString("    ") + "\n");
            }
         }
      }
      return retString.toString();
   }

   public void setFlows(SortedSet<Flow> flows) {
      _flows = flows;
   }

   public void setFlowsByText(SortedMap<String, Flow> flowsByText) {
      _flowsByText = flowsByText;
   }

   public void setTraces(
         SortedMap<String, SortedMap<String, SortedSet<FlowTrace>>> traces) {
      _traces = traces;
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder();
      for (Flow flow : _flows) {
         String flowString = flow.toString();
         sb.append("Flow: " + flowString + "\n");
         SortedMap<String, SortedSet<FlowTrace>> envTraceSetMap = _traces
               .get(flowString);
         for (String environmentName : envTraceSetMap.keySet()) {
            sb.append(" Environment: " + environmentName + "\n");
            Set<FlowTrace> traces = envTraceSetMap.get(environmentName);
            int i = 0;
            for (FlowTrace trace : traces) {
               i++;
               sb.append("  Trace: " + i + "\n");
               String rawTraceString = trace.toString();
               String traceString = CommonUtil.getIndentedString(rawTraceString,
                     3);
               sb.append(traceString);
            }
         }
      }
      return sb.toString();
   }
}
