package org.batfish.representation;

import java.util.Map;
import java.util.TreeMap;

import org.batfish.representation.DiffieHellmanGroup;
import org.batfish.util.ComparableStructure;

public class IpsecPolicy extends ComparableStructure<String> {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private DiffieHellmanGroup _pfsKeyGroup;

   private boolean _pfsKeyGroupDynamicIke;

   private Map<String, IpsecProposal> _proposals;

   public IpsecPolicy(String name) {
      super(name);
      _proposals = new TreeMap<String, IpsecProposal>();
   }

   public DiffieHellmanGroup getPfsKeyGroup() {
      return _pfsKeyGroup;
   }

   public boolean getPfsKeyGroupDynamicIke() {
      return _pfsKeyGroupDynamicIke;
   }

   public Map<String, IpsecProposal> getProposals() {
      return _proposals;
   }

   public void setPfsKeyGroup(DiffieHellmanGroup dhGroup) {
      _pfsKeyGroup = dhGroup;
   }

   public void setPfsKeyGroupDynamicIke(boolean pfsKeyGroupDynamicIke) {
      _pfsKeyGroupDynamicIke = pfsKeyGroupDynamicIke;
   }

}
