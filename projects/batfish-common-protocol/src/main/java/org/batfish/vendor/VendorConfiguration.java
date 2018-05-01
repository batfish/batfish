package org.batfish.vendor;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import org.batfish.common.BatfishException;
import org.batfish.common.VendorConversionException;
import org.batfish.common.Warnings;
import org.batfish.common.util.DefinedStructure;
import org.batfish.common.util.ReferenceCountedStructure;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.DefinedStructureInfo;
import org.batfish.datamodel.GenericConfigObject;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;

public abstract class VendorConfiguration implements Serializable, GenericConfigObject {

  /** */
  private static final long serialVersionUID = 1L;

  private transient ConvertConfigurationAnswerElement _answerElement;

  protected String _filename;

  private VendorConfiguration _overlayConfiguration;

  protected final SortedMap<
          StructureType, SortedMap<String, SortedMap<StructureUsage, SortedSet<Integer>>>>
      _structureReferences;

  private transient boolean _unrecognized;

  protected transient Warnings _w;

  public VendorConfiguration() {
    _structureReferences = new TreeMap<>();
  }

  public String canonicalizeInterfaceName(String name) {
    return name;
  }

  @JsonIgnore
  public final ConvertConfigurationAnswerElement getAnswerElement() {
    return _answerElement;
  }

  public String getFilename() {
    return _filename;
  }

  public abstract String getHostname();

  public VendorConfiguration getOverlayConfiguration() {
    return _overlayConfiguration;
  }

  public abstract Set<String> getUnimplementedFeatures();

  public boolean getUnrecognized() {
    return _unrecognized;
  }

  @JsonIgnore
  public final Warnings getWarnings() {
    return _w;
  }

  /**
   * Mark all structures of a particular type with its recorded usages. This function should
   * typically be called prior to warning about unused structures of that type.
   *
   * @param type The type of the structure to which a reference will be added
   * @param usage The usage mode of the structure that was pre-recorded
   * @param maps A list of maps to check for the structure to be updated. Each map could be null.
   *     There must be at least one element. The structure may exist in more than one map.
   */
  protected void markStructure(
      StructureType type,
      StructureUsage usage,
      List<Map<String, ? extends ReferenceCountedStructure>> maps) {
    if (maps.isEmpty()) {
      throw new BatfishException("List of maps must contain at least one element");
    }
    SortedMap<String, SortedMap<StructureUsage, SortedSet<Integer>>> byName =
        _structureReferences.get(type);
    if (byName != null) {
      byName.forEach(
          (name, byUsage) -> {
            SortedSet<Integer> lines = byUsage.get(usage);
            if (lines != null) {
              List<Map<String, ? extends ReferenceCountedStructure>> matchingMaps =
                  maps.stream()
                      .filter(map -> map != null && map.containsKey(name))
                      .collect(ImmutableList.toImmutableList());
              if (matchingMaps.isEmpty()) {
                for (int line : lines) {
                  undefined(type, name, usage, line);
                }
              } else {
                String msg = usage.getDescription();
                for (Map<String, ? extends ReferenceCountedStructure> matchingMap : matchingMaps) {
                  matchingMap.get(name).getReferers().put(this, msg);
                }
              }
            }
          });
    }
  }

  protected void markStructure(
      StructureType type,
      StructureUsage usage,
      Map<String, ? extends ReferenceCountedStructure> map) {
    markStructure(type, usage, Collections.singletonList(map));
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

  public void setOverlayConfiguration(VendorConfiguration overlayConfiguration) {
    _overlayConfiguration = overlayConfiguration;
  }

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

  public void recordStructure(
      StructureType structureType, String name, int numReferrers, int line) {
    String hostname = getHostname();
    String type = structureType.getDescription();
    SortedMap<String, SortedMap<String, DefinedStructureInfo>> byType =
        _answerElement.getDefinedStructures().computeIfAbsent(hostname, k -> new TreeMap<>());
    SortedMap<String, DefinedStructureInfo> byName =
        byType.computeIfAbsent(type, k -> new TreeMap<>());
    DefinedStructureInfo info =
        byName.computeIfAbsent(name, k -> new DefinedStructureInfo(new TreeSet<>(), numReferrers));
    info.getDefinitionLines().add(line);
  }

  public void recordStructure(
      ReferenceCountedStructure structure, StructureType structureType, String name, int line) {
    recordStructure(structureType, name, structure.getReferers().size(), line);
  }

  protected <T extends DefinedStructure<String>> void recordStructure(
      Map<String, T> map, StructureType type) {
    for (Entry<String, T> e : map.entrySet()) {
      String name = e.getKey();
      if (name.startsWith("~")
          || e.getValue().getDefinitionLine() == DefinedStructure.IGNORED_DEFINITION_LINE) {
        continue;
      }
      T t = e.getValue();
      recordStructure(t, type, name, t.getDefinitionLine());
    }
  }
}
