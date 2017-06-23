package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import org.batfish.datamodel.Prefix;

public class GeneratedRoute implements Serializable {

   /**
   *
   */
   private static final long serialVersionUID = 1L;

   private Integer _metric;

   private final Map<String, Integer> _policies;

   private Integer _preference;

   private final Prefix _prefix;

   public GeneratedRoute(Prefix prefix) {
      _prefix = prefix;
      _policies = new LinkedHashMap<>();
   }

   public Integer getMetric() {
      return _metric;
   }

   public Map<String, Integer> getPolicies() {
      return _policies;
   }

   public Integer getPreference() {
      return _preference;
   }

   public Prefix getPrefix() {
      return _prefix;
   }

   public void setMetric(int metric) {
      _metric = metric;
   }

   public void setPreference(int preference) {
      _preference = preference;
   }

}
