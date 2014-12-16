package batfish.representation.juniper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import batfish.representation.Prefix;

public class GeneratedRoute implements Serializable {

   /**
   *
   */
   private static final long serialVersionUID = 1L;

   private int _metric;

   private List<String> _policies;

   private Prefix _prefix;

   public GeneratedRoute(Prefix prefix) {
      _prefix = prefix;
      _policies = new ArrayList<String>();
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

   public void setMetric(int metric) {
      _metric = metric;
   }

}
