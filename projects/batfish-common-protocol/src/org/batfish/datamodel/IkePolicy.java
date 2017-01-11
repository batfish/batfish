package org.batfish.datamodel;

import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.batfish.common.util.ComparableStructure;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public final class IkePolicy extends ComparableStructure<String> {

   private static final String PROPOSALS_VAR = "proposals";

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private String _preSharedKeyHash;

   private transient SortedSet<String> _proposalNames;

   private SortedMap<String, IkeProposal> _proposals;

   @JsonCreator
   public IkePolicy(@JsonProperty(NAME_VAR) String name) {
      super(name);
      _proposals = new TreeMap<>();
   }

   @JsonPropertyDescription("SHA-256 hash of salted version of pre-shared-key stored in original configuration")
   public String getPreSharedKeyHash() {
      return _preSharedKeyHash;
   }

   @JsonProperty(PROPOSALS_VAR)
   @JsonPropertyDescription("Dictionary of IKE proposals attached to this policy. Each stored as @id")
   public SortedSet<String> getProposalNames() {
      if (_proposals != null && !_proposals.isEmpty()) {
         return new TreeSet<>(_proposals.keySet());
      }
      else {
         return _proposalNames;
      }
   }

   @JsonIgnore
   public SortedMap<String, IkeProposal> getProposals() {
      return _proposals;
   }

   public void resolveReferences(Configuration owner) {
      if (_proposalNames != null) {
         for (String proposalName : _proposalNames) {
            _proposals.put(proposalName,
                  owner.getIkeProposals().get(proposalName));
         }
      }
   }

   public void setPreSharedKeyHash(String preSharedKeyHash) {
      _preSharedKeyHash = preSharedKeyHash;
   }

   @JsonProperty(PROPOSALS_VAR)
   public void setProposalNames(SortedSet<String> proposalNames) {
      _proposalNames = proposalNames;
   }

   @JsonIgnore
   public void setProposals(SortedMap<String, IkeProposal> proposals) {
      _proposals = proposals;
   }

}
