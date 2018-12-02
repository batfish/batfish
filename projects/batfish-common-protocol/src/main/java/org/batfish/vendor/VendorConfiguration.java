package org.batfish.vendor;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multiset;
import com.google.common.collect.SortedMultiset;
import com.google.common.collect.TreeMultiset;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import org.batfish.common.VendorConversionException;
import org.batfish.common.Warnings;
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

  protected final SortedMap<String, SortedMap<String, DefinedStructureInfo>> _structureDefinitions;

  protected final SortedMap<
          StructureType, SortedMap<String, SortedMap<StructureUsage, SortedMultiset<Integer>>>>
      _structureReferences;

  private transient boolean _unrecognized;

  protected transient Warnings _w;

  public VendorConfiguration() {
    _structureDefinitions = new TreeMap<>();
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

  public boolean getUnrecognized() {
    return _unrecognized;
  }

  @JsonIgnore
  public final Warnings getWarnings() {
    return _w;
  }

  /**
   * Updates referrers and/or warns for undefined structures based on references to an abstract
   * {@link StructureType}: a reference type that may refer to one of a number of defined structure
   * types passed in {@code structureTypesToCheck}.
   *
   * <p>For example using Cisco devices, see {@code CiscoStructureType.ACCESS_LIST} and how it
   * expands to a list containing many types of IPv4 and IPv6 access lists.
   */
  protected void markAbstractStructure(
      StructureType type, StructureUsage usage, Collection<StructureType> structureTypesToCheck) {
    Map<String, SortedMap<StructureUsage, SortedMultiset<Integer>>> references =
        firstNonNull(_structureReferences.get(type), Collections.emptyMap());
    references.forEach(
        (name, byUsage) -> {
          Multiset<Integer> lines = firstNonNull(byUsage.get(usage), TreeMultiset.create());
          List<DefinedStructureInfo> matchingStructures =
              structureTypesToCheck
                  .stream()
                  .map(t -> _structureDefinitions.get(t.getDescription()))
                  .filter(Objects::nonNull)
                  .map(m -> m.get(name))
                  .filter(Objects::nonNull)
                  .collect(ImmutableList.toImmutableList());
          if (matchingStructures.isEmpty()) {
            for (int line : lines) {
              undefined(type, name, usage, line);
            }
          } else {
            matchingStructures.forEach(
                info ->
                    info.setNumReferrers(
                        info.getNumReferrers() == DefinedStructureInfo.UNKNOWN_NUM_REFERRERS
                            ? DefinedStructureInfo.UNKNOWN_NUM_REFERRERS
                            : info.getNumReferrers() + lines.size()));
          }
        });
  }

  /**
   * Updates referrers and/or warns for undefined structures based on references to then given
   * {@link StructureType}. Compared to {@link #markAbstractStructure}, this function is used when
   * the reference type and the structure type are guaranteed to match.
   */
  protected void markConcreteStructure(StructureType type, StructureUsage... usages) {
    for (StructureUsage usage : usages) {
      markAbstractStructure(type, usage, ImmutableList.of(type));
    }
  }

  public void referenceStructure(StructureType type, String name, StructureUsage usage, int line) {
    SortedMap<String, SortedMap<StructureUsage, SortedMultiset<Integer>>> byName =
        _structureReferences.computeIfAbsent(type, k -> new TreeMap<>());
    SortedMap<StructureUsage, SortedMultiset<Integer>> byUsage =
        byName.computeIfAbsent(name, k -> new TreeMap<>());
    SortedMultiset<Integer> lines = byUsage.computeIfAbsent(usage, k -> TreeMultiset.create());
    lines.add(line);
  }

  public final void setAnswerElement(ConvertConfigurationAnswerElement answerElement) {
    _answerElement = answerElement;
    _answerElement.getDefinedStructures().put(getFilename(), _structureDefinitions);
    _structureReferences.forEach(
        (structType, byType) ->
            byType.forEach(
                (name, byUsage) ->
                    byUsage.forEach(
                        (usage, lines) ->
                            lines.forEach(
                                line ->
                                    addStructureReference(
                                        _answerElement.getReferencedStructures(),
                                        structType,
                                        name,
                                        usage,
                                        line)))));
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

  public abstract List<Configuration> toVendorIndependentConfigurations()
      throws VendorConversionException;

  private void addStructureReference(
      SortedMap<String, SortedMap<String, SortedMap<String, SortedMap<String, SortedSet<Integer>>>>>
          referenceMap,
      StructureType structureType,
      String name,
      StructureUsage usage,
      int line) {
    String filename = getFilename();
    SortedMap<String, SortedMap<String, SortedMap<String, SortedSet<Integer>>>> byType =
        referenceMap.computeIfAbsent(filename, k -> new TreeMap<>());
    String type = structureType.getDescription();
    SortedMap<String, SortedMap<String, SortedSet<Integer>>> byName =
        byType.computeIfAbsent(type, k -> new TreeMap<>());
    SortedMap<String, SortedSet<Integer>> byUsage =
        byName.computeIfAbsent(name, k -> new TreeMap<>());
    String usageStr = usage.getDescription();
    SortedSet<Integer> lines = byUsage.computeIfAbsent(usageStr, k -> new TreeSet<>());
    lines.add(line);
  }

  public void undefined(StructureType structureType, String name, StructureUsage usage, int line) {
    addStructureReference(
        _answerElement.getUndefinedReferences(), structureType, name, usage, line);
  }

  /**
   * Updates structure definitions to include the specified structure {@code name} and {@code
   * structureType} and initializes the number of referrers.
   */
  public void defineStructure(StructureType structureType, String name, int line) {
    recordStructure(structureType, name, 0, line);
  }

  public void recordStructure(
      StructureType structureType, String name, int numReferrers, int line) {
    String type = structureType.getDescription();
    SortedMap<String, DefinedStructureInfo> byName =
        _structureDefinitions.computeIfAbsent(type, k -> new TreeMap<>());
    DefinedStructureInfo info =
        byName.computeIfAbsent(name, k -> new DefinedStructureInfo(new TreeSet<>(), 0));
    info.getDefinitionLines().add(line);
    if (info.getNumReferrers() == DefinedStructureInfo.UNKNOWN_NUM_REFERRERS
        || numReferrers == DefinedStructureInfo.UNKNOWN_NUM_REFERRERS) {
      info.setNumReferrers(DefinedStructureInfo.UNKNOWN_NUM_REFERRERS);
    } else {
      info.setNumReferrers(info.getNumReferrers() + numReferrers);
    }
  }
}
