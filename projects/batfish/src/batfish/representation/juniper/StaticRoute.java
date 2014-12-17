package batfish.representation.juniper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import batfish.representation.Prefix;

public class StaticRoute implements Serializable {

   /**
   *
   */
   private static final long serialVersionUID = 1L;

   private boolean _drop;

   private int _metric;

   private List<String> _policies;

   private Prefix _prefix;

   private Integer _tag;

   public StaticRoute(Prefix prefix) {
      _prefix = prefix;
      _policies = new ArrayList<String>();
   }

   public boolean getDrop() {
      return _drop;
   }

   public int getMetric() {
      return _metric;
   }

   public List<String> getPolicies() {
      return _policies;
   }

   public Prefix getPrefix() {
      return _prefix;
   }

   public Integer getTag() {
      return _tag;
   }

   public void setDrop(boolean drop) {
      _drop = true;
   }

   public void setMetric(int metric) {
      _metric = metric;
   }

   public void setTag(int tag) {
      _tag = tag;
   }

}
