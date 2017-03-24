package org.batfish.representation.juniper;

import java.util.Map;
import java.util.TreeMap;

import org.batfish.common.util.ComparableStructure;
import org.batfish.datamodel.DiffieHellmanGroup;

public class IpsecPolicy extends ComparableStructure<String> {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private final int _definitionLine;

   private DiffieHellmanGroup _pfsKeyGroup;

   private final Map<String, Integer> _proposals;

   public IpsecPolicy(String name, int definitionLine) {
      super(name);
      _definitionLine = definitionLine;
      _proposals = new TreeMap<>();
   }

   public int getDefinitionLine() {
      return _definitionLine;
   }

   public DiffieHellmanGroup getPfsKeyGroup() {
      return _pfsKeyGroup;
   }

   public Map<String, Integer> getProposals() {
      return _proposals;
   }

   public void setPfsKeyGroup(DiffieHellmanGroup dhGroup) {
      _pfsKeyGroup = dhGroup;
   }

}
