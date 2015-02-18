package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.batfish.util.NamedStructure;

public final class CommunityList extends NamedStructure implements Serializable {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private final List<CommunityListLine> _lines;

   public CommunityList(String name) {
      super(name);
      _lines = new ArrayList<CommunityListLine>();
   }

   public List<CommunityListLine> getLines() {
      return _lines;
   }

}
