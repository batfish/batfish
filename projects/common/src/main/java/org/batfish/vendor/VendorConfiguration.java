package org.batfish.vendor;

import com.carrotsearch.hppc.IntHashSet;
import com.carrotsearch.hppc.IntSet;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.IntConsumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.batfish.common.VendorConversionException;
import org.batfish.common.Warnings;
import org.batfish.common.runtime.SnapshotRuntimeData;
import org.batfish.common.topology.Layer1Edge;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.isp_configuration.IspConfiguration;
import org.batfish.datamodel.references.StructureManager;
import org.batfish.grammar.BatfishCombinedParser;

public abstract class VendorConfiguration implements Serializable {

  private transient @Nullable Map<Integer, Set<Integer>> _extraLines;
  private transient @Nullable ConversionContext _conversionContext;
  protected String _filename;
  protected @Nonnull List<String> _secondaryFilenames;
  protected @Nonnull transient SnapshotRuntimeData _runtimeData;
  private VendorConfiguration _overlayConfiguration;
  protected final @Nonnull StructureManager _structureManager;

  private transient boolean _unrecognized;
  protected transient Warnings _w;

  public VendorConfiguration() {
    _runtimeData = SnapshotRuntimeData.EMPTY_SNAPSHOT_RUNTIME_DATA;
    _structureManager = StructureManager.create();
    _secondaryFilenames = ImmutableList.of();
  }

  public String canonicalizeInterfaceName(String name) {
    return name;
  }

  public @Nullable ConversionContext getConversionContext() {
    return _conversionContext;
  }

  /** Returns the primary file from which this vendor configuration was extracted */
  public String getFilename() {
    return _filename;
  }

  /**
   * Returns any additional files, beyond the primary one, from which this vendor configuration was
   * extracted
   */
  public @Nonnull List<String> getSecondaryFilenames() {
    return _secondaryFilenames;
  }

  public abstract String getHostname();

  public VendorConfiguration getOverlayConfiguration() {
    return _overlayConfiguration;
  }

  public @Nonnull StructureManager getStructureManager() {
    return _structureManager;
  }

  /**
   * Return a mapping from filename -> structure manager
   *
   * <p>Default implementation returns singleton map of {@link #getFilename()} to {@link
   * #getStructureManager()}.
   *
   * <p>Multi-file primary {@link VendorConfiguration} implementations should override and return a
   * map including a structure manager for each applicable file.
   */
  public @Nonnull Map<String, StructureManager> getStructureManagerByFilename() {
    return ImmutableMap.of(getFilename(), getStructureManager());
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
    _structureManager.markConcreteStructure(type);
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
    _structureManager.markAbstractStructure(type, usage, structureTypesToCheck);
  }

  /**
   * Updates referrers and/or warns for undefined structures based on references to an abstract
   * {@link StructureType}: a reference type that may refer to one of a number of defined structure
   * types passed in {@code structureTypesToCheck}.
   */
  protected void markAbstractStructureAllUsages(
      StructureType type, Collection<? extends StructureType> structureTypesToCheck) {
    _structureManager.markAbstractStructureAllUsages(type, structureTypesToCheck);
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

  public void referenceStructure(
      @Nonnull StructureType type, @Nonnull String name, @Nonnull StructureUsage usage, int line) {
    _structureManager.referenceStructure(type, name, usage, line);
  }

  /**
   * Rename the specified structure in its definition as well as references. Returns {@code false}
   * and warns if the rename request is unsuccessful (e.g. no corresponding structure or name
   * conflict), otherwise returns {@code true}.
   *
   * <p>The specified {@link Collection} of {@link StructureType} should contain all structure types
   * that share the same namespace (including the specified structure {@code type}); i.e. a rename
   * will only succeed if the new name is not already in use by any structures of the specified
   * types.
   */
  public boolean renameStructure(
      String origName,
      String newName,
      StructureType type,
      Collection<StructureType> sameNamespaceTypes) {
    return _structureManager.renameStructure(origName, newName, type, sameNamespaceTypes, _w);
  }

  /**
   * Delete the definition of and any references to the specified structure. Returns {@code false}
   * and warns if the delete request is unsuccessful (e.g. no corresponding structure), otherwise
   * returns {@code true}.
   */
  public boolean deleteStructure(String name, StructureType type) {
    return _structureManager.deleteStructure(name, type, _w);
  }

  public void setConversionContext(@Nullable ConversionContext conversionContext) {
    _conversionContext = conversionContext;
  }

  public void setFilename(String filename) {
    _filename = filename;
  }

  public void setSecondaryFilenames(List<String> filenames) {
    _secondaryFilenames = ImmutableList.copyOf(filenames);
  }

  public abstract void setHostname(String hostname);

  @JsonIgnore
  public void setRuntimeData(@Nonnull SnapshotRuntimeData runtimeData) {
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

  public void undefined(StructureType structureType, String name, StructureUsage usage, int line) {
    _structureManager.undefined(structureType, name, usage, line);
  }

  /* Recursively process children to find all relevant definition lines for the specified context */
  private static void collectLines(
      RuleContext ctx,
      BatfishCombinedParser<?, ?> parser,
      Map<Integer, Set<Integer>> extraLines,
      IntConsumer collect) {
    for (int i = 0; i < ctx.getChildCount(); ++i) {
      ParseTree child = ctx.getChild(i);
      if (child instanceof TerminalNode) {
        int originalLine = parser.getLine(((TerminalNode) child).getSymbol());
        collect.accept(originalLine);
        if (extraLines != null) {
          extraLines.getOrDefault(originalLine, ImmutableSet.of()).forEach(collect::accept);
        }
      } else if (child instanceof RuleContext) {
        collectLines((RuleContext) child, parser, extraLines, collect);
      }
    }
  }

  /**
   * Updates structure definitions to include the specified structure {@code name} and {@code
   * structureType} and initializes the number of referrers.
   */
  public void defineSingleLineStructure(StructureType structureType, String name, int line) {
    _structureManager.getOrDefine(structureType, name).addDefinitionLines(line);
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
    IntSet lines = new IntHashSet();
    collectLines(ctx, parser, _extraLines, lines::add);
    ImmutableRangeSet.Builder<Integer> ranges = ImmutableRangeSet.builder();
    lines.iterator().forEachRemaining(c -> ranges.add(Range.closedOpen(c.value, c.value + 1)));
    _structureManager.getOrDefine(type, name).addDefinitionLines(ranges.build());
  }

  /**
   * Mark the specified structure as defined on each line in the supplied context. This method marks
   * every line between the start and stop intervals of the given {@code context}.
   *
   * <p>For flattened structures, see {@link #defineFlattenedStructure(StructureType, String,
   * RuleContext, BatfishCombinedParser)}.
   */
  public void defineStructure(StructureType type, String name, ParserRuleContext ctx) {
    _structureManager
        .getOrDefine(type, name)
        .addDefinitionLines(Range.closed(ctx.getStart().getLine(), ctx.getStop().getLine()));
  }

  /**
   * Returns the ISP configuration for this config object. A return value of null implies that the
   * subclass does not provide meaningful information.
   *
   * <p>Subclasses whose border interfaces are not expected to be covered by the user-supplied ISP
   * config file (e.g., AWS) should override this method.
   */
  public @Nullable IspConfiguration getIspConfiguration() {
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
   * connectivity. For classes that represent router configs without access to information about
   * presence of other nodes and interfafces (e.g. via {@link ConversionContext}), the default
   * implementation should be used.
   *
   * <p>It is the responsibility of the implementation to enforce the invariant above.
   */
  public @Nonnull Set<Layer1Edge> getLayer1Edges() {
    return ImmutableSet.of();
  }

  public void setExtraLines(@Nullable Map<Integer, Set<Integer>> extraLines) {
    _extraLines = extraLines;
  }
}
