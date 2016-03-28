package org.batfish.representation.juniper;

import java.util.Set;
import java.util.TreeSet;

import org.batfish.util.ComparableStructure;

public final class IkePolicy extends ComparableStructure<String> {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private String _preSharedKeyHash;

   private final Set<String> _proposals;

   public IkePolicy(String name) {
      super(name);
      _proposals = new TreeSet<String>();
   }

   public String getPreSharedKeyHash() {
      return _preSharedKeyHash;
   }

   public Set<String> getProposals() {
      return _proposals;
   }

   public void setPreSharedKeyHash(String preSharedKeyHash) {
      _preSharedKeyHash = preSharedKeyHash;
   }

}
