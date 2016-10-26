package org.batfish.datamodel;

import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeSet;

import org.batfish.datamodel.routing_policy.RoutingPolicy;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class RoutingPolicyListsDiff extends ConfigDiffElement {

   protected static final String DIFF_VAR = "diff";
   protected Set<String> _diff;

   @JsonCreator()
   public RoutingPolicyListsDiff() {

   }

   public RoutingPolicyListsDiff(NavigableMap<String, RoutingPolicy> a,
         NavigableMap<String, RoutingPolicy> b) {

      super(a.keySet(), b.keySet());
      _diff = new TreeSet<>();
      for (String name : super.common()) {
         // TODO: double check equality and create _diffInfo.
         // For now, we ignore self-generated route maps.
         if (!(name.startsWith("~") && name.endsWith("~"))) {
            if (a.get(name).equals(b.get(name))) {
               _identical.add(name);
            }
            else {
               _diff.add(name);
            }
         }
      }
   }

   @JsonProperty(DIFF_VAR)
   public Set<String> getDiff() {
      return _diff;
   }

   public void setDiff(Set<String> diff) {
      _diff = diff;
   }
}
