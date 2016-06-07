package org.batfish.datamodel;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class GeneratedRoute extends Route implements
      Comparable<GeneratedRoute> {

   private static final String ADMINISTRATIVE_COST_VAR = "administrativeCost";

   private static final String AS_PATH_VAR = "asPath";

   private static final String ATTRIBUTE_POLICIES_VAR = "attributePolicies";

   private static final String DISCARD_VAR = "discard";

   private static final String GENERATION_POLICIES_VAR = "generationPolicies";

   private static final String METRIC_VAR = "metric";

   private static final long serialVersionUID = 1L;

   private int _administrativeCost;

   private AsPath _asPath;

   private final Map<String, PolicyMap> _attributePolicies;

   private boolean _discard;

   private Set<PolicyMap> _generationPolicies;

   private Integer _metric;

   @JsonCreator
   public GeneratedRoute(
         @JsonProperty(PREFIX_VAR) Prefix prefix,
         @JsonProperty(ADMINISTRATIVE_COST_VAR) int administrativeCost,
         @JsonProperty(AS_PATH_VAR) AsPath asPath,
         @JsonProperty(ATTRIBUTE_POLICIES_VAR) Map<String, PolicyMap> attributePolicies,
         @JsonProperty(DISCARD_VAR) boolean discard,
         @JsonProperty(GENERATION_POLICIES_VAR) Set<PolicyMap> generationPolicies,
         @JsonProperty(METRIC_VAR) Integer metric) {
      super(prefix, null);
      _administrativeCost = administrativeCost;
      _asPath = asPath;
      _attributePolicies = attributePolicies;
      _discard = discard;
      _generationPolicies = generationPolicies;
      _metric = metric;
   }

   public GeneratedRoute(Prefix prefix, int administrativeCost,
         Set<PolicyMap> generationPolicyMaps) {
      super(prefix, null);
      _administrativeCost = administrativeCost;
      _generationPolicies = generationPolicyMaps;
      _attributePolicies = new TreeMap<String, PolicyMap>();
   }

   @Override
   public int compareTo(GeneratedRoute o) {
      return _prefix.compareTo(o._prefix);
   }

   @Override
   public boolean equals(Object o) {
      GeneratedRoute rhs = (GeneratedRoute) o;
      return _prefix.equals(rhs._prefix);
   }

   @Override
   public int getAdministrativeCost() {
      return _administrativeCost;
   }

   @JsonProperty(AS_PATH_VAR)
   public AsPath getAsPath() {
      return _asPath;
   }

   @JsonProperty(ATTRIBUTE_POLICIES_VAR)
   public Map<String, PolicyMap> getAttributePolicies() {
      return _attributePolicies;
   }

   @JsonProperty(DISCARD_VAR)
   public boolean getDiscard() {
      return _discard;
   }

   @JsonProperty(GENERATION_POLICIES_VAR)
   public Set<PolicyMap> getGenerationPolicies() {
      return _generationPolicies;
   }

   @JsonProperty(METRIC_VAR)
   public Integer getMetric() {
      return _metric;
   }

   @Override
   @JsonIgnore
   public Ip getNextHopIp() {
      return super.getNextHopIp();
   }

   @Override
   public RouteType getRouteType() {
      return RouteType.AGGREGATE;
   }

   @Override
   public int hashCode() {
      return _prefix.hashCode();
   }

   public void setAdministrativePreference(int preference) {
      _administrativeCost = preference;
   }

   public void setAsPath(AsPath asPath) {
      _asPath = asPath;
   }

   public void setDiscard(boolean discard) {
      _discard = discard;
   }

   public void setMetric(int metric) {
      _metric = metric;
   }

}
