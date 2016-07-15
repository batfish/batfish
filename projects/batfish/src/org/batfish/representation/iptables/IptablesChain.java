package org.batfish.representation.iptables;

import java.util.LinkedList;
import java.util.List;

public class IptablesChain {

   private String _name;
   
   private List<IptablesRule> _rules;

   private String _target;

   public IptablesChain(String name) {
      _name = name;
      _rules = new LinkedList<IptablesRule>();      
   }

   public void addRule(IptablesRule rule, int ruleIndex) {
      
      //rule indices in iptables start at 1
      int listIndex = ruleIndex - 1;
      
      _rules.add(listIndex, rule);
   }     

   public void setTarget(String target) {
      _target = target;
   }
}
