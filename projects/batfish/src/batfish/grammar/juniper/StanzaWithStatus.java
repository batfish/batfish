package batfish.grammar.juniper;

import java.util.ArrayList;
import java.util.List;

public abstract class StanzaWithStatus {
   
   public StanzaWithStatus () {
      _stanzaStatus = StanzaStatusType.ACTIVE;
      _ignoredStatements = new ArrayList<String>();
      _aggregateWithTitle = true;
      _applyGroups = false;
   }  
   
   public void postProcessStanza () {
      if (get_stanzaStatus() == StanzaStatusType.INACTIVE) {
         addIgnoredStatement("Inactive " + _postProcessTitle);
      }
      else {
         this.postProcessStanza();
         this.aggregateIgnoredStatments(_postProcessTitle);
      }
      // TODO [P0] :CHECK
   }

   /* ------------------------ Status-Related Members -----------------------*/
   private StanzaStatusType _stanzaStatus;
   
   public StanzaStatusType get_stanzaStatus() {
      return _stanzaStatus;
   }
   public void set_stanzaStatus(StanzaStatusType s) {
      _stanzaStatus = s;
   }
   
   /* ----------------------- Ignoring-Related Members ----------------------*/
   private List<String> _ignoredStatements;
   private String _postProcessTitle = "";
   boolean _aggregateWithTitle;
   
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
   public void set_aggregateWithTitle (boolean b) {
      _aggregateWithTitle = b;
   }
   public void aggregateIgnoredStatments(String titleStatment) {
      if (_aggregateWithTitle) {
         if (_ignoredStatements.size()>0) {
            for (String s : _ignoredStatements) {
               s = "   "+ s;
            }
         }
         _ignoredStatements.add(0,titleStatment);
      }
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
