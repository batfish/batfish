package batfish.grammar.juniper;

import java.util.ArrayList;
import java.util.List;

public abstract class StanzaWithStatus {
   private List<String> _ignoredStatements;
   private String _postProcessTitle = "";
   boolean _alreadyAggregated = true; 
   
   
   public StanzaWithStatus () {
      _stanzaStatus = StanzaStatusType.ACTIVE;
      _ignoredStatements = new ArrayList<String>();
      _alreadyAggregated = true;
      _applyGroups = false;
   }  

   /* ------------------------ Status-Related Members -----------------------*/
   private StanzaStatusType _stanzaStatus;
   
   public StanzaStatusType get_stanzaStatus() {
      return _stanzaStatus;
   }
   public void set_stanzaStatus(StanzaStatusType s) {
      _stanzaStatus = s;
   }
   
   /* ---------------------- Processing-Related Members ---------------------*/
   public void postProcessStanza () {
      this.processIgnoredStatements();
   }
   
   /* ----------------------- Ignoring-Related Members ----------------------*/     
   public void processIgnoredStatements () {
      if (get_stanzaStatus() == StanzaStatusType.INACTIVE) {
         clearIgnoredStatements();
         addIgnoredStatement("Inactive " + _postProcessTitle + "{...}");
      }
      else {
         if (!_alreadyAggregated && _ignoredStatements.size()>0) {
            for (int i =0; i < _ignoredStatements.size(); i++) {
               _ignoredStatements.set(i,"   "+_ignoredStatements.get(i));
            }
            _ignoredStatements.add(0,_postProcessTitle);
         }
      }
      // TODO [P0] :CHECK
   }
   
   
   public void set_postProcessTitle (String s) {
      _postProcessTitle = s;
   }
   public List<String> get_ignoredStatements() {
      return _ignoredStatements;
   }
   public void addIgnoredStatement(String s) {
      this._ignoredStatements.add(s);
   }
   public void addIgnoredStatements(List<String> s) {
      this._ignoredStatements.addAll(s);
   }
   public void clearIgnoredStatements() {
      this._ignoredStatements.clear();
   }
   public void set_alreadyAggregated (boolean b) {
      _alreadyAggregated = b;
   }
   
   /* ---------------------- ApplyGroups-Related Members --------------------*/
   private boolean _applyGroups;
   
   public void set_applyGroups (boolean b) {
      _applyGroups = b;
   }
   public boolean get_applyGroups () {
      return _applyGroups;
   }
}
