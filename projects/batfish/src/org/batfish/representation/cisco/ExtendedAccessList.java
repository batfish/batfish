package org.batfish.representation.cisco;

import java.util.ArrayList;
import java.util.List;

import org.batfish.common.util.ComparableStructure;

public class ExtendedAccessList extends ComparableStructure<String> {

   private static final long serialVersionUID = 1L;

   private boolean _ipv6;

   private List<ExtendedAccessListLine> _lines;

   private StandardAccessList _parent;

   public ExtendedAccessList(String id) {
      super(id);
      _lines = new ArrayList<ExtendedAccessListLine>();
   }

   public void addLine(ExtendedAccessListLine all) {
      _lines.add(all);
   }

   public boolean getIpv6() {
      return _ipv6;
   }

   public List<ExtendedAccessListLine> getLines() {
      return _lines;
   }

   public StandardAccessList getParent() {
      return _parent;
   }

   public void setIpv6(boolean ipv6) {
      _ipv6 = ipv6;
   }

   public void setParent(StandardAccessList parent) {
      _parent = parent;
   }

   @Override
   public String toString() {
      String output = super.toString() + "\n" + "Identifier: " + _key;
      for (ExtendedAccessListLine line : _lines) {
         output += "\n" + line;
      }
      return output;
   }
}
