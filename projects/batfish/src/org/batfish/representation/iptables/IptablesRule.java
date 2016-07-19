package org.batfish.representation.iptables;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

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
   String _jumpPoint;
   
   public IptablesRule() {
      _matchList = new LinkedList<IptablesMatch>();
   }
   
   public void addMatch(boolean inverted, MatchType matchType, Object matchData) {
      IptablesMatch match = new IptablesMatch(inverted, matchType, matchData);
      _matchList.add(match);
   }
   
   public void setAction(IptablesActionType actionType, String jumpPoint) {
      _actionType = actionType;
      _jumpPoint = jumpPoint;
   }
}
