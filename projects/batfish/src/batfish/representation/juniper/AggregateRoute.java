package batfish.representation.juniper;

import java.io.Serializable;

import batfish.representation.Prefix;

public class AggregateRoute implements Serializable {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private int _preference;

   private Prefix _prefix;

   public AggregateRoute(Prefix prefix) {
      _prefix = prefix;
   }

   public int getMetric() {
      return _preference;
   }

   public Prefix getPrefix() {
      return _prefix;
   }

   public void setPreference(int preference) {
      _preference = preference;
   }

}
