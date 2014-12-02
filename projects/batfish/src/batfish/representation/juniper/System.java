package batfish.representation.juniper;

import java.io.Serializable;

public class System implements Serializable {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private String _hostname;

   public String getHostname() {
      return _hostname;
   }

   public void setHostname(String hostname) {
      _hostname = hostname;
   }

}
