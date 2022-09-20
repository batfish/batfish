package org.batfish.grammar.cool_nos;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Set;
import javax.annotation.Nonnull;
import org.antlr.v4.runtime.ParserRuleContext;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.Warnings;
import org.batfish.grammar.BatfishParseTreeWalker;
import org.batfish.grammar.ControlPlaneExtractor;
import org.batfish.grammar.ImplementedRules;
import org.batfish.grammar.cool_nos.CoolNosParser.Cool_nos_configurationContext;
import org.batfish.grammar.silent_syntax.SilentSyntaxCollection;
import org.batfish.vendor.VendorConfiguration;
import org.batfish.vendor.cool_nos.CoolNosConfiguration;

/** Extracts data from Cool NOS parse tree into a {@link CoolNosConfiguration}. */
public final class CoolNosControlPlaneExtractor implements ControlPlaneExtractor {

  public CoolNosControlPlaneExtractor(
      String fileText,
      CoolNosCombinedParser combinedParser,
      Warnings warnings,
      SilentSyntaxCollection silentSyntax) {
    _text = fileText;
    _parser = combinedParser;
    _w = warnings;
    _silentSyntax = silentSyntax;
  }

  @Override
  public Set<String> implementedRuleNames() {
    return ImplementedRules.getImplementedRules(CoolNosConfigurationBuilder.class);
  }

  @Override
  public VendorConfiguration getVendorConfiguration() {
    return _configuration;
  }

  @Override
  public void processParseTree(NetworkSnapshot snapshot, ParserRuleContext tree) {
    checkArgument(
        tree instanceof Cool_nos_configurationContext,
        "Expected %s, not %s",
        Cool_nos_configurationContext.class,
        tree.getClass());
    // TOOD: insert any pre-processing of the parse tree here

    // Build configuration from pre-processed parse tree
    CoolNosConfigurationBuilder cb =
        new CoolNosConfigurationBuilder(_parser, _text, _w, _silentSyntax);
    new BatfishParseTreeWalker(_parser).walk(cb, tree);
    _configuration = cb.getConfiguration();
  }

  private CoolNosConfiguration _configuration;
  private final CoolNosCombinedParser _parser;
  private final String _text;
  private final Warnings _w;
  private final @Nonnull SilentSyntaxCollection _silentSyntax;
}
