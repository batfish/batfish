package org.batfish.grammar.flatjuniper;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Set;
import javax.annotation.Nonnull;
import org.antlr.v4.runtime.ParserRuleContext;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.Warnings;
import org.batfish.grammar.BatfishParseTreeWalker;
import org.batfish.grammar.ControlPlaneExtractor;
import org.batfish.grammar.ImplementedRules;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Flat_juniper_configurationContext;
import org.batfish.grammar.silent_syntax.SilentSyntaxCollection;
import org.batfish.representation.juniper.JuniperConfiguration;
import org.batfish.vendor.VendorConfiguration;

public class FlatJuniperControlPlaneExtractor implements ControlPlaneExtractor {

  private JuniperConfiguration _configuration;
  private final FlatJuniperCombinedParser _parser;
  private final String _text;
  private final Warnings _w;
  private final @Nonnull SilentSyntaxCollection _silentSyntax;

  @Override
  public Set<String> implementedRuleNames() {
    // TODO: do we need to add things like InsertDeleteApplicator? I think not, since those don't
    // make it to the final output.
    return ImplementedRules.getImplementedRules(ConfigurationBuilder.class);
  }

  public FlatJuniperControlPlaneExtractor(
      String fileText,
      FlatJuniperCombinedParser combinedParser,
      Warnings warnings,
      SilentSyntaxCollection silentSyntax) {
    _text = fileText;
    _parser = combinedParser;
    _w = warnings;
    _silentSyntax = silentSyntax;
  }

  @Override
  public VendorConfiguration getVendorConfiguration() {
    return _configuration;
  }

  @Override
  public void processParseTree(NetworkSnapshot snapshot, ParserRuleContext tree) {
    checkArgument(
        tree instanceof Flat_juniper_configurationContext,
        "Expected %s, not %s",
        Flat_juniper_configurationContext.class,
        tree.getClass());
    Hierarchy hierarchy = new Hierarchy();
    // Pre-process parse tree
    PreprocessJuniperExtractor.preprocess(
        (Flat_juniper_configurationContext) tree, hierarchy, _parser, _w);
    // Build configuration from pre-processed parse tree
    ConfigurationBuilder cb =
        new ConfigurationBuilder(_parser, _text, _w, hierarchy.getTokenInputs(), _silentSyntax);
    new BatfishParseTreeWalker(_parser).walk(cb, tree);
    _configuration = cb.getConfiguration();
  }
}
