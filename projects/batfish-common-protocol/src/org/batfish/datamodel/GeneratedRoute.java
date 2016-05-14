package org.batfish.datamodel;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public final class GeneratedRoute extends Route {

   private static final long serialVersionUID = 1L;

   private int _administrativeCost;

   private AsPath _asPath;

   private final Map<String, PolicyMap> _attributePolicies;

   private boolean _discard;

   private Set<PolicyMap> _generationPolicies;

   private Integer _metric;

   public GeneratedRoute(Prefix prefix, int administrativeCost,
         Set<PolicyMap> generationPolicyMaps) {
      super(prefix, null);
      _administrativeCost = administrativeCost;
      _generationPolicies = generationPolicyMaps;
      _attributePolicies = new TreeMap<String, PolicyMap>();
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

   public AsPath getAsPath() {
      return _asPath;
   }

   public Map<String, PolicyMap> getAttributePolicies() {
      return _attributePolicies;
   }

   public boolean getDiscard() {
      return _discard;
   }

   public Set<PolicyMap> getGenerationPolicies() {
      return _generationPolicies;
   }

   public Integer getMetric() {
      return _metric;
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
