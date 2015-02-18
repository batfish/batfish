package org.batfish.representation.juniper;

import java.io.Serializable;

import org.batfish.representation.AsPath;
import org.batfish.representation.Prefix;

public class AggregateRoute implements Serializable {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private AsPath _asPath;

   private int _preference;

   private Prefix _prefix;

   public AggregateRoute(Prefix prefix) {
      _prefix = prefix;
   }

   public AsPath getAsPath() {
      return _asPath;
   }

   public int getMetric() {
      return _preference;
   }

   public Prefix getPrefix() {
      return _prefix;
   }

   public void setAsPath(AsPath asPath) {
      _asPath = asPath;
   }

   public void setPreference(int preference) {
      _preference = preference;
   }

}
