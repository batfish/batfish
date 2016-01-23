package org.batfish.grammar.flatvyos;

import org.batfish.representation.vyos.RouteMapMatch;

public class RouteMapMatchPrefixList implements RouteMapMatch {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private final String _prefixList;

   public RouteMapMatchPrefixList(String prefixList) {
      _prefixList = prefixList;
   }

   public String getPrefixList() {
      return _prefixList;
   }

}
