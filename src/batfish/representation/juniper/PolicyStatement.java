package batfish.representation.juniper;

import java.util.Arrays;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;

public class PolicyStatement {
   private NavigableMap<Integer, PolicyStatementClause> _clauses;
   private String _mapName;

   public PolicyStatement(String name) {
      _mapName = name;
      _clauses = new TreeMap<Integer, PolicyStatementClause>();
   }

   public void addClause(PolicyStatementClause rmc) {
      _clauses.put(rmc.getSeqNum(), rmc);
   }

   public List<PolicyStatementClause> getClauseList() {
      return Arrays.asList(_clauses.values().toArray(new PolicyStatementClause[0]));
   }

   public NavigableMap<Integer, PolicyStatementClause> getClauseMap() {
      return _clauses;
   }

   public String getMapName() {
      return _mapName;
   }

}
