package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public final class FwTerm implements Serializable {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private final Set<FwFromApplication> _fromApplications;

   private final Set<FwFrom> _froms;

   private boolean _ipv6;

   private final String _name;

   private final Set<FwThen> _thens;

   public FwTerm(String name) {
      _froms = new HashSet<FwFrom>();
      _fromApplications = new HashSet<FwFromApplication>();
      _name = name;
      _thens = new HashSet<FwThen>();
   }

   public Set<FwFromApplication> getFromApplications() {
      return _fromApplications;
   }

   public Set<FwFrom> getFroms() {
      return _froms;
   }

   public boolean getIpv6() {
      return _ipv6;
   }

   public String getName() {
      return _name;
   }

   public Set<FwThen> getThens() {
      return _thens;
   }

   public void setIpv6(boolean ipv6) {
      _ipv6 = ipv6;
   }

}
