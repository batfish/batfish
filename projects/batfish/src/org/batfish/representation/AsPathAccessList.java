package org.batfish.representation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public final class AsPathAccessList implements Serializable {

   private static final long serialVersionUID = 1L;

   private final List<AsPathAccessListLine> _lines;

   private final String _name;

   public AsPathAccessList(String name) {
      _lines = new ArrayList<AsPathAccessListLine>();
      _name = name;
   }

   public List<AsPathAccessListLine> getLines() {
      return _lines;
   }

   public String getName() {
      return _name;
   }

}
