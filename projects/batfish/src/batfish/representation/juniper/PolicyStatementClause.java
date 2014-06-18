package batfish.representation.juniper;

import java.util.List;

import batfish.representation.LineAction;

public class PolicyStatementClause {

   private String _mapName;
   private List<PolicyStatementMatchLine> _matchList;
   private List<PolicyStatementSetLine> _setList;
   private int _seqNum;
   private LineAction _type;
   private String _clauseName;

   public PolicyStatementClause(LineAction type, String name, int num,
         List<PolicyStatementMatchLine> mlist, List<PolicyStatementSetLine> slist) {
      _type = type;
      _mapName = name;
      _seqNum = num;
      _matchList = mlist;
      _setList = slist;
   }

   public void setClauseName(String n){
      _clauseName = n;
   }
   
   public void addMatchLines(List<PolicyStatementMatchLine> m){
      _matchList.addAll(m);
   }
   
   public void addSetLines(List<PolicyStatementSetLine> s){
      _setList.addAll(s);
   }
   
   public void setAction(LineAction a){
      _type = a;
   }
   
   public String getClauseName(){
      return _clauseName;
   }
   
   public LineAction getAction() {
      return _type;
   }

   public String getMapName() {
      return _mapName;
   }

   public List<PolicyStatementMatchLine> getMatchList() {
      return _matchList;
   }

   public List<PolicyStatementSetLine> getSetList() {
      return _setList;
   }

   public int getSeqNum() {
      return _seqNum;
   }

}
