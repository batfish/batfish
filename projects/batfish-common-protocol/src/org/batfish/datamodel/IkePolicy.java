package org.batfish.datamodel;

import java.util.Map;
import java.util.TreeMap;

import org.batfish.common.util.ComparableStructure;

public final class IkePolicy extends ComparableStructure<String> {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private String _preSharedKeyHash;

   private final Map<String, IkeProposal> _proposals;

   public IkePolicy(String name) {
      super(name);
      _proposals = new TreeMap<String, IkeProposal>();
   }

   public String getPreSharedKeyHash() {
      return _preSharedKeyHash;
   }

   public Map<String, IkeProposal> getProposals() {
      return _proposals;
   }

   public void setPreSharedKeyHash(String preSharedKeyHash) {
      _preSharedKeyHash = preSharedKeyHash;
   }

}
