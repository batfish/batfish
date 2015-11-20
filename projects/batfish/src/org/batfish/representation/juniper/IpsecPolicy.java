package org.batfish.representation.juniper;

import java.util.Set;
import java.util.TreeSet;

import org.batfish.representation.DiffieHellmanGroup;
import org.batfish.util.NamedStructure;

public class IpsecPolicy extends NamedStructure {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private DiffieHellmanGroup _pfsKeyGroup;

   private Set<String> _proposals;

   public IpsecPolicy(String name) {
      super(name);
      _proposals = new TreeSet<String>();
   }

   public DiffieHellmanGroup getPfsKeyGroup() {
      return _pfsKeyGroup;
   }

   public Set<String> getProposals() {
      return _proposals;
   }

   public void setPfsKeyGroup(DiffieHellmanGroup dhGroup) {
      _pfsKeyGroup = dhGroup;
   }

}
