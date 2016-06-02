package org.batfish.datamodel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.batfish.common.util.ComparableStructure;

public final class AsPathAccessList extends ComparableStructure<String>
      implements Serializable {

   private static final long serialVersionUID = 1L;

   private final List<AsPathAccessListLine> _lines;

   public AsPathAccessList(String name) {
      super(name);
      _lines = new ArrayList<AsPathAccessListLine>();
   }

   public void addLine(AsPathAccessListLine a) {
      _lines.add(a);
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }
      AsPathAccessList other = (AsPathAccessList) obj;
      return other._lines.equals(_lines);
   }

   public List<AsPathAccessListLine> getLines() {
      return _lines;
   }
}
