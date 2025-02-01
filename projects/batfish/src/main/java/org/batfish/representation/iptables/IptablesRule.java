package org.batfish.representation.iptables;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import org.batfish.datamodel.LineAction;
import org.batfish.representation.iptables.IptablesChain.ChainPolicy;
import org.batfish.representation.iptables.IptablesMatch.MatchType;

public class IptablesRule implements Serializable {

  public enum IptablesActionType {
    ACCEPT,
    CHAIN,
    DROP,
    GOTO,
    RETURN
  }

  public static IptablesActionType fromChainPolicyToActionType(ChainPolicy policy) {
    return switch (policy) {
      case ACCEPT -> IptablesActionType.ACCEPT;
      case DROP -> IptablesActionType.DROP;
      case RETURN -> IptablesActionType.RETURN;
    };
  }

  private IptablesActionType _actionType;
  private List<IptablesMatch> _matchList;

  private String _name;
  private String _nextChain;

  public IptablesRule() {
    _matchList = new LinkedList<>();
  }

  public void addMatch(boolean inverted, MatchType matchType, Object matchData) {
    IptablesMatch match = new IptablesMatch(inverted, matchType, matchData);
    _matchList.add(match);
  }

  public IptablesActionType getActionType() {
    return _actionType;
  }

  /**
   * Coverts IpTablesAction to a LineAction for the ACCEPT and DROP cases.
   *
   * @throws IllegalArgumentException if the action is not one of those two..
   */
  public LineAction getIpAccessListLineAction() {
    if (_actionType == IptablesActionType.ACCEPT) {
      return LineAction.PERMIT;
    } else if (_actionType == IptablesActionType.DROP) {
      return LineAction.DENY;
    } else {
      throw new IllegalArgumentException(
          "Unsupported IptablesActionType for mapping to LineAction: " + _actionType);
    }
  }

  public List<IptablesMatch> getMatchList() {
    return _matchList;
  }

  public String getNextChain() {
    return _nextChain;
  }

  public String getName() {
    return _name;
  }

  public void setName(String name) {
    _name = name;
  }

  public void setAction(ChainPolicy policy) {
    _actionType = fromChainPolicyToActionType(policy);
  }

  public void setAction(IptablesActionType actionType, String nextChain) {
    _actionType = actionType;
    _nextChain = nextChain;
  }
}
