package batfish.representation.juniper;

import java.io.Serializable;

public class JuniperConfiguration implements Serializable {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   protected System _system;

   public System getSystem() {
      return _system;
   }

   public void setSystem(System system) {
      _system = system;
   }
}
