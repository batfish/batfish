package org.batfish.datamodel.collections;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.batfish.common.util.*;

public class NamedStructureEquivalenceSet<T> {

   // Jackson cannot serialize generics correctly.
   @JsonIgnore
   private T _namedStructure = null;

   private Set<String> _nodes = null;

   public NamedStructureEquivalenceSet() {
   }

   public NamedStructureEquivalenceSet(String node, T namedStructure) {
      _namedStructure = namedStructure;
      _nodes = new HashSet<String>();
      _nodes.add(node);
      addNode(node);
   }

   public void addNode(String node) {
      _nodes.add(node);
   }

   public boolean compareStructure(T s) {
      return CommonUtil.checkJsonEqual(_namedStructure, s);
   }

   public T get_namedStructure() {
      return _namedStructure;
   }

   public Set<String> get_nodes() {
      return _nodes;
   }

   public void set_namedStructure(T _namedStructure) {
      this._namedStructure = _namedStructure;
   }

   public void set_nodes(Set<String> _nodes) {
      this._nodes = _nodes;
   }

}
