package org.batfish.vendor.aruba_aoscx.grammar;

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
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.Aoscx_configurationContext;
import org.batfish.vendor.aruba_aoscx.representation.AosCxConfiguration;

/** Extracts data from an Aruba AOS-CX parse tree. */
public final class AosCxControlPlaneExtractor implements ControlPlaneExtractor {

  public AosCxControlPlaneExtractor(
      String fileText,
      AosCxCombinedParser combinedParser,
      Warnings warnings,
      SilentSyntaxCollection silentSyntax) {
    _text = fileText;
    _parser = combinedParser;
    _w = warnings;
    _silentSyntax = silentSyntax;
  }

  @Override
  public Set<String> implementedRuleNames() {
    return ImplementedRules.getImplementedRules(AosCxConfigurationBuilder.class);
  }

  @Override
  public VendorConfiguration getVendorConfiguration() {
    return _configuration;
  }

  @Override
  public void processParseTree(NetworkSnapshot snapshot, ParserRuleContext tree) {
    checkArgument(
        tree instanceof Aoscx_configurationContext,
        "Expected %s, not %s",
        Aoscx_configurationContext.class,
        tree.getClass());

    AosCxConfigurationBuilder cb =
        new AosCxConfigurationBuilder(_parser, _text, _w, _silentSyntax);
    new BatfishParseTreeWalker(_parser).walk(cb, tree);
    _configuration = cb.getConfiguration();
  }

  private AosCxConfiguration _configuration;
  private final AosCxCombinedParser _parser;
  private final String _text;
  private final Warnings _w;
  private final @Nonnull SilentSyntaxCollection _silentSyntax;
}
