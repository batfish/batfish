package org.batfish.grammar.flatjuniper;

import static com.google.common.base.Preconditions.checkArgument;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.batfish.common.Warnings;
import org.batfish.grammar.BatfishParseTreeWalker;
import org.batfish.grammar.PreprocessExtractor;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Flat_juniper_configurationContext;

/**
 * Parse tree extractor used for generating pre-processed Juniper configuration text from an initial
 * unprocessed flat Juniper parse tree.
 */
@ParametersAreNonnullByDefault
public final class PreprocessJuniperExtractor implements PreprocessExtractor {

  private static final String HEADER = "####BATFISH PRE-PROCESSED JUNIPER CONFIG####\n";

  /**
   * Pre-process a flat Juniper parse tree by generating and pruning parse tree nodes corresponding
   * to various lines in the input configuration.
   *
   * <p>Mutations are made directly to the input parse {@code tree} and serve as the output of this
   * function.
   *
   * <p>Pre-processing consists of:
   *
   * <ol>
   *   <li>Applying insertions (moves) and deletions via {@link ActivationLinePruner}
   *   <li>Pruning lines deactivated by 'deactivate' lines via {@link DeactivatedLinePruner}
   *   <li>Pruning 'deactivate' lines via {@link DeactivatedLinePruner}
   *   <li>Generating lines corresponding to 'apply-groups' lines, while respecting
   *       'apply-groups-except' lines via {@link GroupInheritor}
   *   <li>Pruning 'groups' lines via {@link GroupPruner}
   *   <li>Generating lines corresponding to 'apply-path' lines via {@link ApplyPathApplicator}
   *   <li>TODO: Pruning 'apply-path' lines
   * </ol>
   *
   * @param tree The flat-Juniper parse tree to be pre-processed in-place.
   * @param hierarchy An empty {@link Hierarchy} that will be populated with trees for regular
   *     configuration lines, groups lines, and deactivate lines
   * @param parser The parser that produced the parse {@code tree}.
   * @param w The store for warnings produced during pre-processing
   */
  static void preprocess(
      Flat_juniper_configurationContext tree,
      Hierarchy hierarchy,
      FlatJuniperCombinedParser parser,
      Warnings w) {
    ParseTreeWalker walker = new BatfishParseTreeWalker(parser);

    // Implements insert and delete respecting order of configuration lines.
    // Properly handles set, activate, and deactivate lines.
    InsertDeleteApplicator d = new InsertDeleteApplicator(parser, w);
    walker.walk(d, tree);

    // Delete all deactivated lines:
    // 1. Mark parts of the hierarchy as deactivated
    DeactivateTreeBuilder dtb = new DeactivateTreeBuilder(hierarchy);
    walker.walk(dtb, tree);
    // 2. Remove 'activate <tree>' and 'deactivate <tree>' lines
    ActivationLinePruner dp = new ActivationLinePruner();
    walker.walk(dp, tree);
    // 3. Remove 'set' lines that are deactivated
    DeactivatedLinePruner dlp = new DeactivatedLinePruner(hierarchy);
    walker.walk(dlp, tree);

    InitialTreeBuilder tb = new InitialTreeBuilder(hierarchy);
    walker.walk(tb, tree);
    GroupTreeBuilder gb = new GroupTreeBuilder(hierarchy);
    walker.walk(gb, tree);

    // Run until convergence: [set groups A apply-groups B] is valid
    ApplyGroupsMarker agm = new ApplyGroupsMarker(hierarchy, w);
    boolean changed;
    do {
      walker.walk(agm, tree);
      changed = GroupInheritor.inheritGroups(hierarchy, tree);
    } while (changed);
    GroupPruner.prune(tree);

    walker.walk(dlp, tree);

    ApplyPathApplicator ap = new ApplyPathApplicator(hierarchy, w);
    walker.walk(ap, tree);
    // TODO: pruning apply-path lines removes definition lines
  }

  private final FlatJuniperCombinedParser _parser;
  private String _preprocessedConfigurationText;
  private final Warnings _w;

  public PreprocessJuniperExtractor(FlatJuniperCombinedParser combinedParser, Warnings warnings) {
    _parser = combinedParser;
    _w = warnings;
  }

  @Override
  public @Nonnull String getPreprocessedConfigurationText() {
    return _preprocessedConfigurationText;
  }

  /**
   * Pre-process a flat Juniper parse {@code tree}, after which pre-processed configuration text
   * will be available via {@link #getPreprocessedConfigurationText}.
   */
  @Override
  public void processParseTree(ParserRuleContext tree) {
    checkArgument(
        tree instanceof Flat_juniper_configurationContext,
        "Expected %s, not %s",
        Flat_juniper_configurationContext.class,
        tree.getClass());
    preprocess((Flat_juniper_configurationContext) tree, new Hierarchy(), _parser, _w);
    Hierarchy finalHierarchy = new Hierarchy();
    InitialTreeBuilder tb = new InitialTreeBuilder(finalHierarchy);
    new BatfishParseTreeWalker(_parser).walk(tb, tree);
    _preprocessedConfigurationText = finalHierarchy.toSetLines(HEADER);
  }
}
