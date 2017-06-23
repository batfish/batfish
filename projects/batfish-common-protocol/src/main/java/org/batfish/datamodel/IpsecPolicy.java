package org.batfish.datamodel;

import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.batfish.common.util.ComparableStructure;
import org.batfish.datamodel.DiffieHellmanGroup;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public class IpsecPolicy extends ComparableStructure<String> {

   private static final String PROPOSALS_VAR = "proposals";

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private DiffieHellmanGroup _pfsKeyGroup;

   private boolean _pfsKeyGroupDynamicIke;

   private transient SortedSet<String> _proposalNames;

   private SortedMap<String, IpsecProposal> _proposals;

   @JsonCreator
   public IpsecPolicy(@JsonProperty(NAME_VAR) String name) {
      super(name);
      _proposals = new TreeMap<>();
   }

   public DiffieHellmanGroup getPfsKeyGroup() {
      return _pfsKeyGroup;
   }

   public boolean getPfsKeyGroupDynamicIke() {
      return _pfsKeyGroupDynamicIke;
   }

   @JsonProperty(PROPOSALS_VAR)
   @JsonPropertyDescription("IPSEC proposals to try with this policy")
   public SortedSet<String> getProposalNames() {
      if (_proposals != null && !_proposals.isEmpty()) {
         return new TreeSet<>(_proposals.keySet());
      }
      else {
         return _proposalNames;
      }
   }

   @JsonIgnore
   public SortedMap<String, IpsecProposal> getProposals() {
      return _proposals;
   }

   public void resolveReferences(Configuration owner) {
      if (_proposalNames != null) {
         for (String name : _proposalNames) {
            _proposals.put(name, owner.getIpsecProposals().get(name));
         }
      }
   }

   public void setPfsKeyGroup(DiffieHellmanGroup dhGroup) {
      _pfsKeyGroup = dhGroup;
   }

   public void setPfsKeyGroupDynamicIke(boolean pfsKeyGroupDynamicIke) {
      _pfsKeyGroupDynamicIke = pfsKeyGroupDynamicIke;
   }

   @JsonProperty(PROPOSALS_VAR)
   public void setProposalNames(SortedSet<String> proposalNames) {
      _proposalNames = proposalNames;
   }

   public void setProposals(SortedMap<String, IpsecProposal> proposals) {
      _proposals = proposals;
   }

}
