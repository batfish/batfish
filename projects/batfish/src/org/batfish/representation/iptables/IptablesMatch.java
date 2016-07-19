package org.batfish.representation.iptables;

import java.io.Serializable;

public class IptablesMatch implements Serializable {

   public enum MatchType {
      Destination,
      DestinationPort,
      InInterface,
      OutInterface,
      Source,
      SourcePort,
   }
   
   /**
    * 
    */
   private static final long serialVersionUID = 1L;
   
   private boolean _inverted;
   
   private MatchType _matchType;

   private Object _matchData;
   
   public IptablesMatch(boolean inverted, MatchType matchType, Object matchData) {
      _inverted = inverted;
      _matchType = matchType;
      _matchData = matchData;
   }
}
