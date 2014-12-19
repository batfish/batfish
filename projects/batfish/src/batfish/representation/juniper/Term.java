package batfish.representation.juniper;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public final class Term implements Serializable {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private final Set<From> _froms;

   private final String _name;

   private final Set<Then> _thens;

   public Term(String name) {
      _froms = new HashSet<From>();
      _name = name;
      _thens = new HashSet<Then>();
   }

   public Set<From> getFroms() {
      return _froms;
   }

   public String getName() {
      return _name;
   }

   public Set<Then> getThens() {
      return _thens;
   }

}
