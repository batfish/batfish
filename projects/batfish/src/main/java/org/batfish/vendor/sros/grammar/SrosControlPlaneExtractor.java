package org.batfish.vendor.sros.grammar;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Set;
import javax.annotation.Nonnull;
import org.antlr.v4.runtime.ParserRuleContext;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.Warnings;
import org.batfish.grammar.BatfishParseTreeWalker;
import org.batfish.grammar.ControlPlaneExtractor;
import org.batfish.grammar.ImplementedRules;
import org.batfish.grammar.silent_syntax.SilentSyntaxCollection;
import org.batfish.vendor.VendorConfiguration;
import org.batfish.vendor.sros.grammar.SrosParser.Sros_configurationContext;

/** Extracts data from an SR-OS parse tree into a {@link SrosConfiguration}. */
public final class SrosControlPlaneExtractor implements ControlPlaneExtractor {

  public SrosControlPlaneExtractor(
      String fileText,
      SrosCombinedParser combinedParser,
      Warnings warnings,
      SilentSyntaxCollection silentSyntax) {
    _text = fileText;
    _parser = combinedParser;
    _w = warnings;
    _silentSyntax = silentSyntax;
  }

  @Override
  public Set<String> implementedRuleNames() {
    return ImplementedRules.getImplementedRules(SrosConfigurationBuilder.class);
  }

  @Override
  public VendorConfiguration getVendorConfiguration() {
    return _configuration;
  }

  @Override
  public void processParseTree(NetworkSnapshot snapshot, ParserRuleContext tree) {
    checkArgument(
        tree instanceof Sros_configurationContext,
        "Expected %s, not %s",
        Sros_configurationContext.class,
        tree.getClass());
    SrosConfigurationBuilder cb = new SrosConfigurationBuilder(_parser, _text, _w, _silentSyntax);
    new BatfishParseTreeWalker(_parser).walk(cb, tree);
    _configuration = cb.getConfiguration();
  }

  private org.batfish.vendor.sros.representation.SrosConfiguration _configuration;
  private final @Nonnull SrosCombinedParser _parser;
  private final @Nonnull String _text;
  private final @Nonnull Warnings _w;
  private final @Nonnull SilentSyntaxCollection _silentSyntax;
}
