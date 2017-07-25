package org.batfish.representation.iptables;

import java.util.HashMap;
import java.util.Map;
import org.batfish.common.util.ComparableStructure;
import org.batfish.representation.iptables.IptablesChain.ChainPolicy;

public class IptablesTable extends ComparableStructure<String> {

  /** */
  private static final long serialVersionUID = 1L;

  private Map<String, IptablesChain> _chains;

  public IptablesTable(String name) {
    super(name);
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

  public void setChainPolicy(String chainName, ChainPolicy policy) {
    addChain(chainName);
    _chains.get(chainName).setPolicy(policy);
  }
}
