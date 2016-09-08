package org.batfish.representation.cisco;

import java.util.ArrayList;
import java.util.List;

import org.batfish.common.util.ComparableStructure;

public class StandardAccessList extends ComparableStructure<String> {

   private static final long serialVersionUID = 1L;

   private boolean _isIpv6;

   private List<StandardAccessListLine> _lines;

   public StandardAccessList(String id) {
      super(id);
      _lines = new ArrayList<>();
   }

   public void addLine(StandardAccessListLine all) {
      _lines.add(all);
   }

   public List<StandardAccessListLine> getLines() {
      return _lines;
   }

   public boolean isIpV6() {
      return _isIpv6;
   }

   public void setIpv6(boolean isIpv6) {
      _isIpv6 = isIpv6;
   }

   public ExtendedAccessList toExtendedAccessList() {
      ExtendedAccessList eal = new ExtendedAccessList(_key);
      eal.setParent(this);
      eal.getLines().clear();
      for (StandardAccessListLine sall : _lines) {
         eal.addLine(sall.toExtendedAccessListLine());
      }
      return eal;
   }

}
