package org.batfish.representation.iptables;

import java.util.HashMap;
import java.util.Map;

public class Table {

   private String _name;
   
	private Map<String, IptablesChain> _chains;
	
	public Table(String name) {
	   _name = name;
	   _chains = new HashMap<String, IptablesChain>();
	}

   public void addChain(String chainName) {
      if (!_chains.containsKey(chainName)) {
         _chains.put(chainName, new IptablesChain(chainName));
      }
   }

   public void addRule(String chainName, IptablesRule rule, int index) {
      addChain(chainName);
      _chains.get(chainName).addRule(rule, index);
   }

   public void setChainTarget(String chainName, String target) {
       addChain(chainName);
      _chains.get(chainName).setTarget(target);
   }
}