package org.batfish.vendor;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import org.batfish.common.VendorConversionException;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.GenericConfigObject;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;

public abstract class VendorConfiguration implements Serializable, GenericConfigObject {

  /** */
  private static final long serialVersionUID = 1L;

  private transient ConvertConfigurationAnswerElement _answerElement;

  protected String _filename;

  protected final SortedMap<StructureType, SortedMap<String, SortedSet<Integer>>>
      _structureDefinitions;

  protected final SortedMap<
          StructureType, SortedMap<String, SortedMap<StructureUsage, SortedSet<Integer>>>>
      _structureReferences;

  private transient boolean _unrecognized;

  protected transient Warnings _w;

  public VendorConfiguration() {
    _structureDefinitions = new TreeMap<>();
    _structureReferences = new TreeMap<>();
  }

  public void defineStructure(StructureType type, String name, int line) {
    SortedMap<String, SortedSet<Integer>> byName =
        _structureDefinitions.computeIfAbsent(type, k -> new TreeMap<>());
    SortedSet<Integer> lines = byName.computeIfAbsent(name, k -> new TreeSet<>());
    lines.add(line);
  }

  @JsonIgnore
  public final ConvertConfigurationAnswerElement getAnswerElement() {
    return _answerElement;
  }

  public abstract String getHostname();

  public abstract SortedSet<String> getRoles();

  public abstract Set<String> getUnimplementedFeatures();

  public boolean getUnrecognized() {
    return _unrecognized;
  }

  @JsonIgnore
  public final Warnings getWarnings() {
    return _w;
  }

  public void referenceStructure(StructureType type, String name, StructureUsage usage, int line) {
    SortedMap<String, SortedMap<StructureUsage, SortedSet<Integer>>> byName =
        _structureReferences.computeIfAbsent(type, k -> new TreeMap<>());
    SortedMap<StructureUsage, SortedSet<Integer>> byUsage =
        byName.computeIfAbsent(name, k -> new TreeMap<>());
    SortedSet<Integer> lines = byUsage.computeIfAbsent(usage, k -> new TreeSet<>());
    lines.add(line);
  }

  public final void setAnswerElement(ConvertConfigurationAnswerElement answerElement) {
    _answerElement = answerElement;
  }

  public void setFilename(String filename) {
    _filename = filename;
  }

  public abstract void setHostname(String hostname);

  public abstract void setRoles(SortedSet<String> roles);

  public void setUnrecognized(boolean unrecognized) {
    _unrecognized = unrecognized;
  }

  public abstract void setVendor(ConfigurationFormat format);

  public final void setWarnings(Warnings warnings) {
    _w = warnings;
  }

  public abstract Configuration toVendorIndependentConfiguration() throws VendorConversionException;

  public void undefined(StructureType structureType, String name, StructureUsage usage, int line) {
    String hostname = getHostname();
    SortedMap<String, SortedMap<String, SortedMap<String, SortedSet<Integer>>>> byType =
        _answerElement.getUndefinedReferences().computeIfAbsent(hostname, k -> new TreeMap<>());
    String type = structureType.getDescription();
    SortedMap<String, SortedMap<String, SortedSet<Integer>>> byName =
        byType.computeIfAbsent(type, k -> new TreeMap<>());
    SortedMap<String, SortedSet<Integer>> byUsage =
        byName.computeIfAbsent(name, k -> new TreeMap<>());
    String usageStr = usage.getDescription();
    SortedSet<Integer> lines = byUsage.computeIfAbsent(usageStr, k -> new TreeSet<>());
    lines.add(line);
  }

  public void unused(StructureType structureType, String name, int line) {
    String hostname = getHostname();
    String type = structureType.getDescription();
    SortedMap<String, SortedMap<String, SortedSet<Integer>>> byType =
        _answerElement.getUnusedStructures().computeIfAbsent(hostname, k -> new TreeMap<>());
    SortedMap<String, SortedSet<Integer>> byName =
        byType.computeIfAbsent(type, k -> new TreeMap<>());
    SortedSet<Integer> lines = byName.computeIfAbsent(name, k -> new TreeSet<>());
    lines.add(line);
  }
}
