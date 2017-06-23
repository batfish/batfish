package org.batfish.representation.juniper;

import java.util.Map;
import java.util.TreeMap;

import org.batfish.common.util.ComparableStructure;

public final class IkePolicy extends ComparableStructure<String> {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private final int _definitionLine;

   private String _preSharedKeyHash;

   private final Map<String, Integer> _proposals;

   public IkePolicy(String name, int definitionLine) {
      super(name);
      _definitionLine = definitionLine;
      _proposals = new TreeMap<>();
   }

   public int getDefinitionLine() {
      return _definitionLine;
   }

   public String getPreSharedKeyHash() {
      return _preSharedKeyHash;
   }

   public Map<String, Integer> getProposals() {
      return _proposals;
   }

   public void setPreSharedKeyHash(String preSharedKeyHash) {
      _preSharedKeyHash = preSharedKeyHash;
   }

}
