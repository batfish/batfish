package org.batfish.representation.iptables;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import org.batfish.common.BatfishException;
import org.batfish.datamodel.LineAction;
import org.batfish.representation.iptables.IptablesChain.ChainPolicy;
import org.batfish.representation.iptables.IptablesMatch.MatchType;

public class IptablesRule implements Serializable {

   public enum IptablesActionType {
      ACCEPT,
      CHAIN,
      GOTO,
      DROP,
      RETURN
   }
   
   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   List<IptablesMatch> _matchList;   
   IptablesActionType _actionType;
   String _nextChain;
   
   public IptablesRule() {
      _matchList = new LinkedList<IptablesMatch>();
   }
   
   public void addMatch(boolean inverted, MatchType matchType, Object matchData) {
      IptablesMatch match = new IptablesMatch(inverted, matchType, matchData);
      _matchList.add(match);
   }
   
   public void setAction(IptablesActionType actionType, String nextChain) {
      _actionType = actionType;
      _nextChain = nextChain;
   }

   public void setAction(ChainPolicy policy) {
      _actionType = fromChainPolicyToActionType(policy);
   }
   
   public static IptablesActionType fromChainPolicyToActionType(ChainPolicy policy) {
      switch (policy) {
      case ACCEPT:
           return IptablesActionType.ACCEPT;
      case DROP:
         return IptablesActionType.DROP;
      case RETURN:
          return IptablesActionType.RETURN;
      }      
      return null;
   }
   
   public IptablesActionType getActionType() {
      return _actionType;
   }
   
   public List<IptablesMatch> getMatchList() {
      return _matchList;
   }
   
   public String getNextChain() {
      return _nextChain;
   }

   public LineAction getIpAccessListLineAction() {
      if (_actionType == IptablesActionType.ACCEPT)
         return LineAction.ACCEPT;
      else if (_actionType == IptablesActionType.DROP)
         return LineAction.REJECT;
      else 
         throw new BatfishException("Unsupported IptablesActionType for mapping to LineAction: " + _actionType.toString());
   }
}
