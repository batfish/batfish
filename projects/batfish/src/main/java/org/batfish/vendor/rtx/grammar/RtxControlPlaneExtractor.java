package org.batfish.vendor.rtx.grammar;

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
import org.batfish.vendor.rtx.grammar.RtxParser.Rtx_configurationContext;
import org.batfish.vendor.rtx.representation.RtxConfiguration;

/** Extracts data from a Yamaha RTX parse tree into an {@link RtxConfiguration}. */
public final class RtxControlPlaneExtractor implements ControlPlaneExtractor {

  private RtxConfiguration _configuration;
  private final @Nonnull RtxCombinedParser _parser;
  private final @Nonnull String _text;
  private final @Nonnull Warnings _w;
  private final @Nonnull SilentSyntaxCollection _silentSyntax;

  public RtxControlPlaneExtractor(
      String fileText,
      RtxCombinedParser combinedParser,
      Warnings warnings,
      SilentSyntaxCollection silentSyntax) {
    _text = fileText;
    _parser = combinedParser;
    _w = warnings;
    _silentSyntax = silentSyntax;
  }

  @Override
  public Set<String> implementedRuleNames() {
    return ImplementedRules.getImplementedRules(RtxConfigurationBuilder.class);
  }

  @Override
  public VendorConfiguration getVendorConfiguration() {
    return _configuration;
  }

  @Override
  public void processParseTree(NetworkSnapshot snapshot, ParserRuleContext tree) {
    checkArgument(
        tree instanceof Rtx_configurationContext,
        "Expected %s, not %s",
        Rtx_configurationContext.class,
        tree.getClass());
    RtxConfigurationBuilder cb = new RtxConfigurationBuilder(_parser, _text, _w, _silentSyntax);
    new BatfishParseTreeWalker(_parser).walk(cb, tree);
    _configuration = cb.getConfiguration();
  }
}
