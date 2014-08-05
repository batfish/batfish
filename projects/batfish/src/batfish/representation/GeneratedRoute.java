package batfish.representation;

import java.util.Set;

public class GeneratedRoute extends Route {

   private static final long serialVersionUID = 1L;

   private int _administrativeCost;
   private AsPath _asPath;
   private Set<PolicyMap> _generationPolicies;

   public GeneratedRoute(Ip prefix, int prefixLength, int administrativeCost,
         Set<PolicyMap> generationPolicyMaps) {
      super(prefix, prefixLength, null);
      _administrativeCost = administrativeCost;
      _generationPolicies = generationPolicyMaps;
   }

   @Override
   public boolean equals(Object o) {
      GeneratedRoute rhs = (GeneratedRoute) o;
      return _prefix.equals(rhs._prefix) && _prefixLength == rhs._prefixLength;
   }

   @Override
   public int getAdministrativeCost() {
      return _administrativeCost;
   }

   public AsPath getAsPath() {
      return _asPath;
   }

   public Set<PolicyMap> getGenerationPolicies() {
      return _generationPolicies;
   }

   @Override
   public RouteType getRouteType() {
      return RouteType.AGGREGATE;
   }

   public void setAdministrativePreference(int preference) {
      _administrativeCost = preference;
   }

   public void setAsPath(AsPath asPath) {
      _asPath = asPath;
   }

}
