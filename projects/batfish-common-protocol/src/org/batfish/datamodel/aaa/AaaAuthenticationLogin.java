package org.batfish.datamodel.aaa;

import java.io.Serializable;

public class AaaAuthenticationLogin implements Serializable {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private boolean _privilegeMode;

   public boolean getPrivilegeMode() {
      return _privilegeMode;
   }

   public void setPrivilegeMode(boolean privilegeMode) {
      _privilegeMode = privilegeMode;
   }

}
