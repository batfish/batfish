package batfish.representation.juniper;

import java.util.List;

public class PolicyStatement_Term {

   private String _name;
   private PolicyStatement_LineAction _lineAction;
   private List<PolicyStatement_MatchLine> _matchList;
   private List<PolicyStatement_SetLine> _setList;
   
   /* ------------------------------ Constructor ----------------------------*/
   public PolicyStatement_Term(String n, List<PolicyStatement_MatchLine> ml, List<PolicyStatement_SetLine> sl, PolicyStatement_LineAction la) {
      _name = n;
      _matchList = ml;
      _setList = sl;
      _lineAction = la;
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   
   /* ---------------------------- Getters/Setters --------------------------*/
   public String get_name() {
      return _name;
   }
   public List<PolicyStatement_MatchLine> get_matchList() {
      return _matchList;
   }   
   public List<PolicyStatement_SetLine> get_setList() {
      return _setList;
   }   
   public PolicyStatement_LineAction get_lineAction () {
      return _lineAction;
   }
   
   /* --------------------------- Inherited Methods -------------------------*/
}
