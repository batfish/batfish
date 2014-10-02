package batfish.grammar.juniper.policy_options;

import java.util.ArrayList;
import java.util.List;

import batfish.grammar.juniper.StanzaStatusType;
import batfish.representation.juniper.PolicyStatement;
import batfish.representation.juniper.PolicyStatement_Term;

public class PO_PolicyStatementStanza extends POStanza {
   
   private String _name;
   private List<POPS_TermStanza> _termStanzas;
   
   private PolicyStatement _policyStatement;
   
   /* ------------------------------ Constructor ----------------------------*/
   public PO_PolicyStatementStanza(String n) {
      _name = n;
      _termStanzas = new ArrayList<POPS_TermStanza> ();
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   public void addTerm(POPS_TermStanza ts) { 
      if(!ts.get_isEmpty()) {
         _termStanzas.add(ts);
      }
   }
   
   /* ---------------------------- Getters/Setters --------------------------*/
   public PolicyStatement get_policyStatement () {
      return _policyStatement;
   }

   /* --------------------------- Inherited Methods -------------------------*/
   @Override
   public void postProcessStanza() {
      super.postProcessStanza();
      
      List<PolicyStatement_Term> _terms = new ArrayList<PolicyStatement_Term> ();
      
      for (POPS_TermStanza t : _termStanzas) {
         t.postProcessStanza();
         
         if (t.get_stanzaStatus() == StanzaStatusType.ACTIVE) {
            _terms.add(t.get_term());
         }
         this.addIgnoredStatements(t.get_ignoredStatements());
         this.set_postProcessTitle("Policy Statement " + _name);
      }
      
      _policyStatement = new PolicyStatement(_name,_terms);
   }
  
   @Override
   public POType getType() {
      return POType.POLICY_STATEMENT;
   }

}
