package org.batfish.representation.juniper;

import java.util.Set;
import java.util.TreeSet;

import org.batfish.util.NamedStructure;

public final class IkePolicy extends NamedStructure {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private String _preSharedKey;

   private final Set<String> _proposals;

   public IkePolicy(String name) {
      super(name);
      _proposals = new TreeSet<String>();
   }

   public String getPreSharedKey() {
      return _preSharedKey;
   }

   public Set<String> getProposals() {
      return _proposals;
   }

   public void setPreSharedKey(String key) {
      _preSharedKey = key;
   }

}
