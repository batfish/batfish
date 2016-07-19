package org.batfish.representation.iptables;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import org.batfish.representation.iptables.IptablesChain.ChainPolicy;
import org.batfish.representation.iptables.IptablesMatch.MatchType;

public class IptablesRule implements Serializable {

   public enum IptablesActionType {
      Accept,
      Chain,
      Goto,
      Drop,
      Return
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
           return IptablesActionType.Accept;
      case DROP:
         return IptablesActionType.Drop;
      case RETURN:
          return IptablesActionType.Return;
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
}
