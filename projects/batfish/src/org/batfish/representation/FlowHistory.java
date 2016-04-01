package org.batfish.representation;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.batfish.common.BatfishException;
import org.batfish.util.Util;
import org.batfish.z3.Synthesizer;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

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

   public String toJsonString(String queryName,
         Map<String, Configuration> baseConfigurations, String baseEnvName,
         Map<String, Configuration> diffConfigurations, String diffEnvName) {
      try {
         JSONObject query = new JSONObject();
         query.put("name", queryName);
         query.put("type", "query");
         JSONObject views = new JSONObject();
         query.put("views", views);
         int flowNum = 0;
         for (Entry<Flow, Map<String, Set<FlowTrace>>> e : _data.entrySet()) {
            flowNum++;
            Flow flow = e.getKey();
            Map<String, Set<FlowTrace>> envTraces = e.getValue();
            for (Entry<String, Set<FlowTrace>> e2 : envTraces.entrySet()) {
               String env = e2.getKey();
               Map<String, Configuration> currentConfigurations;
               if (env.equals(baseEnvName)) {
                  currentConfigurations = baseConfigurations;
               }
               else if (diffEnvName != null && env.equals(diffEnvName)) {
                  currentConfigurations = diffConfigurations;
               }
               else {
                  throw new BatfishException(
                        "Could not determine which set of configurations to use for this set of flow traces");
               }
               Set<FlowTrace> traces = e2.getValue();
               int acceptNum = 0;
               int dropNum = 0;
               for (FlowTrace trace : traces) {
                  int num;
                  String disposition;
                  String color;
                  if (trace.getDisposition() == FlowDisposition.ACCEPTED) {
                     acceptNum++;
                     num = acceptNum;
                     disposition = "Accept";
                     color = "ok";
                  }
                  else {
                     dropNum++;
                     num = dropNum;
                     disposition = "Drop";
                     color = "error";
                  }
                  String viewName = "Flow" + flowNum + ":" + env + ":"
                        + disposition + num;
                  JSONObject view = new JSONObject();
                  views.put(viewName, view);
                  view.put("name", viewName);
                  view.put("type", "view");
                  view.put("description", flow.toString().replace("<", "&lt;")
                        .replace(">", "&gt;")
                        + "<br>" + trace.getNotes());
                  view.put("color", color);
                  JSONArray links = new JSONArray();
                  JSONObject path = new JSONObject();
                  view.put("path", path);
                  path.put("links", links);
                  int hopNum = 0;
                  for (Edge edge : trace.getHops()) {
                     hopNum++;
                     String linkName = viewName + ":Hop" + hopNum;
                     JSONObject link = new JSONObject();
                     links.put(link);
                     link.put("name", linkName);
                     link.put("type", "link");
                     String node1Name = edge.getNode1();
                     String int1Name = edge.getInt1();
                     Configuration node1 = currentConfigurations.get(node1Name);
                     Interface int1 = node1.getInterfaces().get(int1Name);
                     JSONObject interface1 = int1.toJSONObject();
                     link.put("interface1", interface1);
                     String node2Name = edge.getNode2();
                     String int2Name = edge.getInt2();
                     JSONObject interface2;
                     if (node2Name.equals(Synthesizer.NODE_NONE_NAME)) {
                        interface2 = new JSONObject();
                        interface2.put("name", int2Name);
                        String int2Type;
                        switch (int2Name) {
                        case Synthesizer.FLOW_SINK_TERMINATION_NAME:
                           int2Type = "FLOW_SINK_TERMINATION";
                           break;
                        default:
                           int2Type = "UNKNOWN";
                        }
                        interface2.put("type", int2Type);
                        interface2.put("node", node2Name);
                     }
                     else {
                        Configuration node2 = currentConfigurations
                              .get(node2Name);
                        Interface int2 = node2.getInterfaces().get(int2Name);
                        interface2 = int2.toJSONObject();
                     }
                     link.put("interface2", interface2);
                  }
               }
            }
         }
         return query.toString(3);
      }
      catch (JSONException e) {
         throw new BatfishException("Error converting history for query: \""
               + queryName + "\" to JSONObject", e);
      }
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
