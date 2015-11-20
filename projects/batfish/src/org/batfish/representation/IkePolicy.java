package org.batfish.representation;

import java.util.Map;
import java.util.TreeMap;

import org.batfish.util.NamedStructure;

public final class IkePolicy extends NamedStructure {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private String _preSharedKey;

   private final Map<String, IkeProposal> _proposals;

   public IkePolicy(String name) {
      super(name);
      _proposals = new TreeMap<String, IkeProposal>();
   }

   public String getPreSharedKey() {
      return _preSharedKey;
   }

   public Map<String, IkeProposal> getProposals() {
      return _proposals;
   }

   public void setPreSharedKey(String key) {
      _preSharedKey = key;
   }

}
