package org.batfish.representation.juniper;

import java.io.Serializable;

public class IsisInterfaceLevelSettings implements Serializable {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private boolean _enabled;

   private Integer _metric;

   private Integer _teMetric;

   public boolean getEnabled() {
      return _enabled;
   }

   public Integer getMetric() {
      return _metric;
   }

   public Integer getTeMetric() {
      return _teMetric;
   }

   public void setEnabled(boolean enabled) {
      _enabled = enabled;
   }

   public void setMetric(int metric) {
      _metric = metric;
   }

   public void setTeMetric(int teMetric) {
      _teMetric = teMetric;
   }

}
