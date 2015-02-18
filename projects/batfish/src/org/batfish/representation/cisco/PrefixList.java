package org.batfish.representation.cisco;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PrefixList implements Serializable {

   private static final long serialVersionUID = 1L;

   private boolean _isIpV6;

   // List of lines that stores the prefix
   private List<PrefixListLine> _lines;

   // Name of the filter
   private String _name;

   private PrefixList(String n) {
      _name = n;
      _lines = new ArrayList<PrefixListLine>();
      _isIpV6 = false;
   }

   public PrefixList(String n, boolean isIpV6) {
      this(n);
      _isIpV6 = isIpV6;
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

   public boolean isIpV6() {
      return _isIpV6;
   }

}
