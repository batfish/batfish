package org.batfish.datamodel;

import java.util.Map;
import java.util.TreeMap;

import org.batfish.common.util.ComparableStructure;
import org.batfish.datamodel.DiffieHellmanGroup;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;

public class IpsecPolicy extends ComparableStructure<String> {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private DiffieHellmanGroup _pfsKeyGroup;

   private boolean _pfsKeyGroupDynamicIke;

   private Map<String, IpsecProposal> _proposals;

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

   @JsonIdentityReference(alwaysAsId = true)
   public Map<String, IpsecProposal> getProposals() {
      return _proposals;
   }

   public void setPfsKeyGroup(DiffieHellmanGroup dhGroup) {
      _pfsKeyGroup = dhGroup;
   }

   public void setPfsKeyGroupDynamicIke(boolean pfsKeyGroupDynamicIke) {
      _pfsKeyGroupDynamicIke = pfsKeyGroupDynamicIke;
   }

   public void setProposals(Map<String, IpsecProposal> proposals) {
      _proposals = proposals;
   }

}
