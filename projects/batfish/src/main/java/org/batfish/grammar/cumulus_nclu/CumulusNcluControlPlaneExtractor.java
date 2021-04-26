package org.batfish.grammar.cumulus_nclu;

import java.util.Set;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.Warnings;
import org.batfish.grammar.BatfishParseTreeWalker;
import org.batfish.grammar.ControlPlaneExtractor;
import org.batfish.grammar.ImplementedRules;
import org.batfish.grammar.silent_syntax.SilentSyntaxCollection;
import org.batfish.representation.cumulus_nclu.CumulusNcluConfiguration;
import org.batfish.vendor.VendorConfiguration;

/**
 * A {@link ControlPlaneExtractor} that produces a {@link CumulusNcluConfiguration} from a parse
 * tree returned by {@link CumulusNcluCombinedParser#parse}.
 */
public class CumulusNcluControlPlaneExtractor implements ControlPlaneExtractor {

  private CumulusNcluConfiguration _configuration;
  private final CumulusNcluCombinedParser _parser;
  private final String _text;
  private final Warnings _w;
  private final SilentSyntaxCollection _silentSyntax;

  public CumulusNcluControlPlaneExtractor(
      String fileText,
      CumulusNcluCombinedParser combinedParser,
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
  public Set<String> implementedRuleNames() {
    return ImplementedRules.getImplementedRules(CumulusNcluConfigurationBuilder.class);
  }

  @Override
  public void processParseTree(NetworkSnapshot snapshot, ParserRuleContext tree) {
    CumulusNcluConfigurationBuilder cb =
        new CumulusNcluConfigurationBuilder(_parser, _text, _w, _silentSyntax);
    ParseTreeWalker walker = new BatfishParseTreeWalker(_parser);
    walker.walk(cb, tree);
    _configuration = cb.getConfiguration();
  }
}
