package org.batfish.representation;

import java.util.List;

import org.batfish.util.ComparableStructure;

public class IpAccessList extends ComparableStructure<String> {

   private static final long serialVersionUID = 1L;

   private List<IpAccessListLine> _lines;

   public IpAccessList(String name, List<IpAccessListLine> lines) {
      super(name);
      _lines = lines;
   }

   public List<IpAccessListLine> getLines() {
      return _lines;
   }

   @Override
   public String toString() {
      String output = super.toString() + "\n" + "Identifier: " + _key;
      for (IpAccessListLine line : _lines) {
         output += "\n" + line;
      }
      return output;
   }

}
