package batfish.representation;

import java.io.Serializable;
import java.util.List;

public class PolicyMap implements Serializable {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   /**
    * Clauses in this list are checked in order against a candidate route until
    * one matches. If the matching clause is a permit clause, the route is
    * permitted, and modified according to the clause's transformation policy.
    * If the matching clause is a deny clause, or if there is no matching
    * clause, then the policy denies the route.
    */
   private List<PolicyMapClause> _clauses;

   /**
    * The configuration-local name identifying this policy.
    */
   private String _mapName;

   /**
    * Constructs a PolicyMap with the given name for {@link #_mapName} and list
    * of clauses for {@link #_clauses}.
    *
    * @param name
    * @param clauses
    */
   public PolicyMap(String name, List<PolicyMapClause> clauses) {
      _mapName = name;
      _clauses = clauses;
   }

   /**
    * @return {@link #_clauses}
    */
   public List<PolicyMapClause> getClauses() {
      return _clauses;
   }

   /**
    * @return {@link #_mapName}
    */
   public String getMapName() {
      return _mapName;
   }

}
