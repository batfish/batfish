package org.batfish.vendor;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multiset;
import com.google.common.collect.SortedMultiset;
import com.google.common.collect.TreeMultiset;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.batfish.common.VendorConversionException;
import org.batfish.common.Warnings;
import org.batfish.common.runtime.RuntimeData;
import org.batfish.common.topology.Layer1Edge;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.DefinedStructureInfo;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.datamodel.isp_configuration.IspConfiguration;
import org.batfish.grammar.BatfishCombinedParser;

public abstract class VendorConfiguration implements Serializable {

  private transient ConvertConfigurationAnswerElement _answerElement;

  protected String _filename;

  @Nonnull protected transient RuntimeData _runtimeData;

  private VendorConfiguration _overlayConfiguration;

  protected final SortedMap<String, SortedMap<String, DefinedStructureInfo>> _structureDefinitions;

  protected final SortedMap<
          StructureType, SortedMap<String, SortedMap<StructureUsage, SortedMultiset<Integer>>>>
      _structureReferences;

  private transient boolean _unrecognized;

  protected transient Warnings _w;

  public VendorConfiguration() {
    _runtimeData = RuntimeData.EMPTY_RUNTIME_DATA;
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
   * Mark all references to a structure of the given type.
   *
   * <p>Do not use if {@code type} is used as an abstract structure type; instead use {@link
   * #markAbstractStructure(StructureType, StructureUsage, Collection)}.
   */
  protected void markConcreteStructure(StructureType type) {
    Map<String, SortedMap<StructureUsage, SortedMultiset<Integer>>> references =
        _structureReferences.getOrDefault(type, Collections.emptySortedMap());
    Map<String, DefinedStructureInfo> definitions =
        _structureDefinitions.getOrDefault(type.getDescription(), Collections.emptySortedMap());
    references.forEach(
        (name, byUsage) -> {
          DefinedStructureInfo def = definitions.get(name);
          if (def == null) {
            byUsage.forEach(
                (usage, lines) -> lines.forEach(line -> undefined(type, name, usage, line)));
          } else {
            int count = byUsage.values().stream().mapToInt(Multiset::size).sum();
            def.setNumReferrers(def.getNumReferrers() + count);
          }
        });
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
      StructureType type,
      StructureUsage usage,
      Collection<? extends StructureType> structureTypesToCheck) {
    Map<String, SortedMap<StructureUsage, SortedMultiset<Integer>>> references =
        firstNonNull(_structureReferences.get(type), Collections.emptyMap());
    references.forEach(
        (name, byUsage) -> {
          Multiset<Integer> lines = firstNonNull(byUsage.get(usage), TreeMultiset.create());
          List<DefinedStructureInfo> matchingStructures =
              structureTypesToCheck.stream()
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
                info -> info.setNumReferrers(info.getNumReferrers() + lines.size()));
          }
        });
  }

  /**
   * Updates referrers and/or warns for undefined structures based on references to then given
   * {@link StructureType}. Compared to {@link #markAbstractStructure}, this function is used when
   * the reference type and the structure type are guaranteed to match.
   *
   * <p>Prefer {@link #markConcreteStructure(StructureType)} for new uses.
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

  @JsonIgnore
  public void setRuntimeData(@Nonnull RuntimeData runtimeData) {
    _runtimeData = runtimeData;
  }

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
  public void defineSingleLineStructure(StructureType structureType, String name, int line) {
    String type = structureType.getDescription();
    SortedMap<String, DefinedStructureInfo> byName =
        _structureDefinitions.computeIfAbsent(type, k -> new TreeMap<>());
    DefinedStructureInfo info =
        byName.computeIfAbsent(name, k -> new DefinedStructureInfo(new TreeSet<>(), 0));
    info.getDefinitionLines().add(line);
  }

  /**
   * Mark the specified structure as defined on each line in the supplied context. This method
   * proceeds by examining every token in the given {@code context}, rather than just the start and
   * stop intervals. It uses the given {@code parser} to find the original (pre-flattening) line
   * number of each token.
   *
   * <p>For structures in non-flattened files, see {@link #defineStructure(StructureType, String,
   * ParserRuleContext)}.
   */
  public void defineFlattenedStructure(
      StructureType type, String name, RuleContext ctx, BatfishCombinedParser<?, ?> parser) {
    /* Recursively process children to find all relevant definition lines for the specified context */
    for (int i = 0; i < ctx.getChildCount(); i++) {
      ParseTree child = ctx.getChild(i);
      if (child instanceof TerminalNode) {
        defineSingleLineStructure(type, name, parser.getLine(((TerminalNode) child).getSymbol()));
      } else if (child instanceof RuleContext) {
        defineFlattenedStructure(type, name, (RuleContext) child, parser);
      }
    }
  }

  /**
   * Mark the specified structure as defined on each line in the supplied context. This method marks
   * every line between the start and stop intervals of the given {@code context}.
   *
   * <p>For flattened structures, see {@link #defineFlattenedStructure(StructureType, String,
   * RuleContext, BatfishCombinedParser)}.
   */
  public void defineStructure(StructureType type, String name, ParserRuleContext ctx) {
    for (int i = ctx.getStart().getLine(); i <= ctx.getStop().getLine(); ++i) {
      defineSingleLineStructure(type, name, i);
    }
  }

  /**
   * Returns the ISP configuration for this config object. A return value of null implies that the
   * subclass does not provide meaningful information.
   *
   * <p>Subclasses whose border interfaces are not expected to be covered by the user-supplied ISP
   * config file (e.g., AWS) should override this method.
   */
  @Nullable
  public IspConfiguration getIspConfiguration() {
    return null;
  }

  /**
   * Returns the layer 1 topology based on the config files.
   *
   * <p>The returned topology has the following invariant: all interfaces in the topology must be
   * present in configurations returned by {@link #toVendorIndependentConfigurations()}. Not all
   * interfaces that are present in configurations need to be present in the topology.
   *
   * <p>This function should be overridden by classes like AwsConfiguration that synthesize their
   * connectivity. For classes that represent router configs, the default implementation should be
   * used.
   *
   * <p>It is the responsibility of the implementation to enforce the invariant above.
   */
  @Nonnull
  public Set<Layer1Edge> getLayer1Edges() {
    return ImmutableSet.of();
  }
}
