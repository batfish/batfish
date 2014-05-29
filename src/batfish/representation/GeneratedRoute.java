package batfish.representation;

import java.util.Set;

import batfish.util.Util;

public class GeneratedRoute extends Route {

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

   public String getIFString(int indentLevel) {

	   String retString = "";

	   retString += String.format("%s GR \n", Util.getIndentString(indentLevel));
	   retString += String.format("%s AdminCost %d", Util.getIndentString(indentLevel + 1), _administrativeCost);
	   // TODO: check why we even have _asPath
	   //       retString += _asPath.getIFString(Util.getIndentString(indentLevel + 1));

	   for (PolicyMap pm : _generationPolicies) {
		   retString += pm.getIFString(indentLevel + 1);
	   }
	   
	   return retString;
   }

   public boolean sameParseTree(GeneratedRoute gr, String prefix) {
      boolean res = equals(gr);
      boolean finalRes = res;
      res = res && (_administrativeCost == gr._administrativeCost);
      if (res == false) {
         System.out.println("GenRoute:AdminCost " + prefix);
         finalRes = res;
      }

      if (_asPath != null) {
         res = (_asPath.equals(gr._asPath));
      }
      else {
         res = (gr._asPath == null);
      }
      if (res == false) {
         System.out.println("GenRoute:AsPath " + prefix);
         finalRes = res;
      }

      if (_generationPolicies.size() != gr._generationPolicies.size()) {
         System.out.println("GenRoute:GenPolicyMap:Size " + prefix);
         return false;
      }
      else {
         for (PolicyMap lhs : _generationPolicies) {
            boolean found = false;
            for (PolicyMap rhs : gr._generationPolicies) {
               if (lhs.getMapName().equals(rhs.getMapName())) {
                  res = lhs.sameParseTree(rhs, "GenRoute:GenPolicyMap "+prefix);
                  if (res == false) {
                     finalRes = false;
                  }
                  found = true;
                  break;
               }
            }
            if (found == false) {
               System.out.println("GenRoute:GenPolicyMap:NotFound "+prefix);
               finalRes = false;
            }
         }
      }
      return finalRes;
   }

}
