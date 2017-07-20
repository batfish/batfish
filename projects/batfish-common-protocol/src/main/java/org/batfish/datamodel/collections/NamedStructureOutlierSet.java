package org.batfish.datamodel.collections;

import java.util.Objects;
import java.util.SortedSet;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class NamedStructureOutlierSet<T>
      implements Comparable<NamedStructureOutlierSet<T>> {

   // a lower bound on the probability at which a hypothesis should be considered to be true
   private static final double THRESHOLD_PROBABILITY = 0.9;
   
   private final String STRUCT_TYPE_VAR = "structType";
   private final String NAME_VAR = "name";
   
   // the name of the structure type (e.g., CommunityList, IpAccessList)
   private String _structType;
   
   // the name of the structure
   private String _name;
   
   // the structure definition that is hypothesized to be the correct one.
   // a null _namedStructure represents a hypothesis that no such named
   // structure should exist in a node
   private T _namedStructure;

   // the nodes that satisfy the above hypothesis
   private SortedSet<String> _conformers;
   
   // the nodes that violate the above hypothesis
   private SortedSet<String> _outliers;

   @JsonCreator
   public NamedStructureOutlierSet(String structType, String name, T namedStructure, 
         SortedSet<String> conformers, SortedSet<String> outliers) {
      _structType = structType;
      _name = name;
      _namedStructure = namedStructure;
      _conformers = conformers;
      _outliers = outliers;      
   }

   public double zScore() {
      double e = _conformers.size();
      double n = e + _outliers.size();
      double numerator = e/n - THRESHOLD_PROBABILITY;
      double denominator = Math.sqrt(THRESHOLD_PROBABILITY * (1.0 - THRESHOLD_PROBABILITY) / n);
      return numerator / denominator;
   }
   
   @Override
   // sort in reverse order of zScore, which is a measure of how likely it is that
   // our hypothesis is correct
   public int compareTo(NamedStructureOutlierSet<T> other) {
      int zComp = Double.compare(other.zScore(), this.zScore());
      if (zComp != 0)
         return zComp;
      int structComp = _structType.compareTo(other.getStructType());
      if (structComp != 0)
         return structComp;
      return _name.compareTo(other.getName());
   }

   @Override
   public boolean equals(Object o) {
      NamedStructureOutlierSet<?> rhs = (NamedStructureOutlierSet<?>) o;
      return _structType.equals(rhs.getStructType()) && _name.equals(rhs.getName());
   }

   @Override
   public int hashCode() {
      return Objects.hash(_structType, _name);
   }

   @JsonProperty(STRUCT_TYPE_VAR)
   public String getStructType() {
      return _structType;
   }
   
   @JsonProperty(NAME_VAR)
   public String getName() {
      return _name;
   }

   // ignore for now to avoid encoding large amounts of information in answer
   @JsonIgnore
   public T getNamedStructure() {
      return _namedStructure;
   }

   public SortedSet<String> getConformers() {
      return _conformers;
   }
   
   public SortedSet<String> getOutliers() {
      return _outliers;
   }
}
