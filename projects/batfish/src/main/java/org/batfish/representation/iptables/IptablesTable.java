package org.batfish.representation.iptables;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import org.batfish.representation.iptables.IptablesChain.ChainPolicy;

public class IptablesTable implements Serializable {

  private Map<String, IptablesChain> _chains;

  private final String _name;

  public IptablesTable(String name) {
    _name = name;
    _chains = new HashMap<>();
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

  public Map<String, IptablesChain> getChains() {
    return _chains;
  }

  public String getName() {
    return _name;
  }

  public void setChainPolicy(String chainName, ChainPolicy policy) {
    addChain(chainName);
    _chains.get(chainName).setPolicy(policy);
  }
}
