package org.batfish.representation.cisco;

import java.io.Serializable;

import org.batfish.representation.IsisLevel;
import org.batfish.representation.IsoAddress;

public class IsisProcess implements Serializable {

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   private IsisLevel _level;

   private IsoAddress _netAddress;

   public IsisLevel getLevel() {
      return _level;
   }

   public IsoAddress getNetAddress() {
      return _netAddress;
   }

   public void setLevel(IsisLevel level) {
      _level = level;
   }
   
   public void setNetAddress(IsoAddress netAddress) {
      _netAddress = netAddress;
   }
   
}
