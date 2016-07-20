package org.batfish.representation.iptables;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import org.batfish.common.BatfishException;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.SubRange;

public class IptablesMatch implements Serializable {

   public enum MatchType {
      DESTINATION,
      DESTINATION_PORT,
      IN_INTERFACE,
      OUT_INTERFACE,
      SOURCE,
      SOURCE_PORT,
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
   
   public boolean getInverted() {
      return _inverted;
   }
   
   public Object getMatchData() {
      return _matchData;
   }
   
   public MatchType getMatchType() {
      return _matchType;
   }

   public IpWildcard toIpWildcard() {
      
      if (_inverted) {
         //_warnings.redFlag("Inversion of src/dst matching is not supported. Current analysis will match everything.");
         //return IpWildcard.ANY;
         throw new BatfishException("Unknown matchdata type");
      }
      
      if (_matchData instanceof Ip) {
         Prefix pfx = new Prefix((Ip) _matchData, 32);
         return new IpWildcard(pfx);
      }
      else if (_matchData instanceof Prefix) {
         return new IpWildcard((Prefix) _matchData);
      }
      else {
         throw new BatfishException("Unknown matchdata type");
      }      
   }

   public List<SubRange> toPortRanges() {
      
      List<SubRange> subRange = new LinkedList<SubRange>();
      
      if (! _inverted) {
         
      }
      else {
         
      }
      
      return subRange;
   }
}
