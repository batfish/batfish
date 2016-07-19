package org.batfish.representation.iptables;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class IptablesChain implements Serializable {

   public enum ChainPolicy {
      ACCEPT,
      DROP,
      RETURN
   }
   
   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   private String _name;
   
   private List<IptablesRule> _rules;

   private ChainPolicy _policy;

   public IptablesChain(String name) {
      _name = name;
      _rules = new LinkedList<IptablesRule>();      
   }

   public void addRule(IptablesRule rule, int ruleIndex) {
      
      if (ruleIndex == -1) { //-1 implies append
         _rules.add(rule);         
      }
      else {
         //rule indices in iptables start at 1
         int listIndex = ruleIndex - 1;
         _rules.add(listIndex, rule);
      }
   }     

   public String getName() {
      return _name;
   }
   
   public ChainPolicy getPolicy() {
      return _policy;
   }
   
   public List<IptablesRule> getRules() {
      return _rules;
   }
   
   public void setPolicy(ChainPolicy policy) {
      _policy = policy;
   }
}
