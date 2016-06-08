package org.batfish.datamodel;

import java.util.Map;
import java.util.TreeMap;

import org.batfish.common.util.ComparableStructure;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class IkePolicy extends ComparableStructure<String> {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private String _preSharedKeyHash;

   private Map<String, IkeProposal> _proposals;

   @JsonCreator
   public IkePolicy(@JsonProperty(NAME_VAR) String name) {
      super(name);
      _proposals = new TreeMap<String, IkeProposal>();
   }

   public String getPreSharedKeyHash() {
      return _preSharedKeyHash;
   }

   @JsonIdentityReference(alwaysAsId = true)
   public Map<String, IkeProposal> getProposals() {
      return _proposals;
   }

   public void setPreSharedKeyHash(String preSharedKeyHash) {
      _preSharedKeyHash = preSharedKeyHash;
   }

   public void setProposals(Map<String, IkeProposal> proposals) {
      _proposals = proposals;
   }

}
