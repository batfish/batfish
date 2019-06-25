package org.batfish.representation.iptables;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.LineAction;

public class IptablesChain implements Serializable {

  public enum ChainPolicy {
    ACCEPT,
    DROP,
    RETURN
  }

  private String _name;

  private ChainPolicy _policy;

  private List<IptablesRule> _rules;

  public IptablesChain(String name) {
    _name = name;
    _policy = ChainPolicy.ACCEPT;
    _rules = new LinkedList<>();
  }

  public void addRule(IptablesRule rule, int ruleIndex) {

    if (ruleIndex == -1) { // -1 implies append
      _rules.add(rule);
    } else {
      // rule indices in iptables start at 1
      int listIndex = ruleIndex - 1;
      _rules.add(listIndex, rule);
    }
  }

  public LineAction getIpAccessListLineAction() {
    if (_policy == ChainPolicy.ACCEPT) {
      return LineAction.PERMIT;
    } else if (_policy == ChainPolicy.DROP) {
      return LineAction.DENY;
    } else {
      throw new BatfishException("Unsupported ChainPolicy for mapping to LineAction: " + _policy);
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
