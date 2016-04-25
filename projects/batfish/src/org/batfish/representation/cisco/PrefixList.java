package org.batfish.representation.cisco;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PrefixList implements Serializable {

   private static final long serialVersionUID = 1L;

   // List of lines that stores the prefix
   private List<PrefixListLine> _lines;

   // Name of the filter
   private String _name;

   public PrefixList(String n) {
      _name = n;
      _lines = new ArrayList<PrefixListLine>();
   }

   public void addLine(PrefixListLine r) {
      _lines.add(r);
   }

   public void addLines(List<PrefixListLine> r) {
      _lines.addAll(r);
   }

   public List<PrefixListLine> getLines() {
      return _lines;
   }

   public String getName() {
      return _name;
   }

}
