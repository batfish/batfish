package org.batfish.vendor;

import java.io.Serializable;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.batfish.common.VendorConversionException;
import org.batfish.common.Warnings;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.datamodel.collections.RoleSet;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.GenericConfigObject;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class VendorConfiguration
      implements Serializable, GenericConfigObject {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private transient ConvertConfigurationAnswerElement _answerElement;

   protected String _filename;

   protected final SortedMap<StructureType, SortedMap<String, SortedSet<Integer>>> _structureDefinitions;

   protected final SortedMap<StructureType, SortedMap<String, SortedMap<StructureUsage, SortedSet<Integer>>>> _structureReferences;

   private transient boolean _unrecognized;

   protected transient Warnings _w;

   public VendorConfiguration() {
      _structureDefinitions = new TreeMap<>();
      _structureReferences = new TreeMap<>();
   }

   public void defineStructure(StructureType type, String name, int line) {
      SortedMap<String, SortedSet<Integer>> byName = _structureDefinitions
            .get(type);
      if (byName == null) {
         byName = new TreeMap<>();
         _structureDefinitions.put(type, byName);
      }
      SortedSet<Integer> lines = byName.get(name);
      if (lines == null) {
         lines = new TreeSet<>();
         byName.put(name, lines);
      }
      lines.add(line);
   }

   @JsonIgnore
   public final ConvertConfigurationAnswerElement getAnswerElement() {
      return _answerElement;
   }

   public abstract String getHostname();

   public abstract RoleSet getRoles();

   public abstract Set<String> getUnimplementedFeatures();

   public boolean getUnrecognized() {
      return _unrecognized;
   }

   @JsonIgnore
   public final Warnings getWarnings() {
      return _w;
   }

   public void referenceStructure(StructureType type, String name,
         StructureUsage usage, int line) {
      SortedMap<String, SortedMap<StructureUsage, SortedSet<Integer>>> byName = _structureReferences
            .get(type);
      if (byName == null) {
         byName = new TreeMap<>();
         _structureReferences.put(type, byName);
      }
      SortedMap<StructureUsage, SortedSet<Integer>> byUsage = byName.get(name);
      if (byUsage == null) {
         byUsage = new TreeMap<>();
         byName.put(name, byUsage);
      }
      SortedSet<Integer> lines = byUsage.get(usage);
      if (lines == null) {
         lines = new TreeSet<>();
         byUsage.put(usage, lines);
      }
      lines.add(line);
   }

   public final void setAnswerElement(
         ConvertConfigurationAnswerElement answerElement) {
      _answerElement = answerElement;
   }

   public void setFilename(String filename) {
      _filename = filename;
   }

   public abstract void setHostname(String hostname);

   public abstract void setRoles(RoleSet roles);

   public void setUnrecognized(boolean unrecognized) {
      _unrecognized = unrecognized;
   }

   public abstract void setVendor(ConfigurationFormat format);

   public final void setWarnings(Warnings warnings) {
      _w = warnings;
   }

   public abstract Configuration toVendorIndependentConfiguration()
         throws VendorConversionException;

   public void undefined(StructureType structureType, String name,
         StructureUsage usage, int line) {
      String hostname = getHostname();
      String type = structureType.getDescription();
      String usageStr = usage.getDescription();
      SortedMap<String, SortedMap<String, SortedMap<String, SortedSet<Integer>>>> byType = _answerElement
            .getUndefinedReferences().get(hostname);
      if (byType == null) {
         byType = new TreeMap<>();
         _answerElement.getUndefinedReferences().put(hostname, byType);
      }
      SortedMap<String, SortedMap<String, SortedSet<Integer>>> byName = byType
            .get(type);
      if (byName == null) {
         byName = new TreeMap<>();
         byType.put(type, byName);
      }
      SortedMap<String, SortedSet<Integer>> byUsage = byName.get(name);
      if (byUsage == null) {
         byUsage = new TreeMap<>();
         byName.put(name, byUsage);
      }
      SortedSet<Integer> lines = byUsage.get(usageStr);
      if (lines == null) {
         lines = new TreeSet<>();
         byUsage.put(usageStr, lines);
      }
      lines.add(line);
   }

   public void unused(StructureType structureType, String name, int line) {
      String hostname = getHostname();
      String type = structureType.getDescription();
      SortedMap<String, SortedMap<String, SortedSet<Integer>>> byType = _answerElement
            .getUnusedStructures().get(hostname);
      if (byType == null) {
         byType = new TreeMap<>();
         _answerElement.getUnusedStructures().put(hostname, byType);
      }
      SortedMap<String, SortedSet<Integer>> byName = byType.get(type);
      if (byName == null) {
         byName = new TreeMap<>();
         byType.put(type, byName);
      }
      SortedSet<Integer> lines = byName.get(name);
      if (lines == null) {
         lines = new TreeSet<>();
         byName.put(name, lines);
      }
      lines.add(line);
   }

}
