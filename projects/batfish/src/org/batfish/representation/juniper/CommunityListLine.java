package org.batfish.representation.juniper;

import java.io.Serializable;

public final class CommunityListLine implements Serializable {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private String _regex;

   public CommunityListLine(String regex) {
      _regex = regex;
   }

   public String getRegex() {
      return _regex;
   }

}
