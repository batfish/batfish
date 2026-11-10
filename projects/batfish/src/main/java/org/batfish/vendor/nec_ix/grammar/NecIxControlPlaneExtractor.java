package org.batfish.vendor.nec_ix.grammar;

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
import org.batfish.vendor.nec_ix.grammar.NecIxParser.Nec_ix_configurationContext;
import org.batfish.vendor.nec_ix.representation.NecIxConfiguration;

/** Extracts data from a NEC IX parse tree into an {@link NecIxConfiguration}. */
public final class NecIxControlPlaneExtractor implements ControlPlaneExtractor {

  private NecIxConfiguration _configuration;
  private final @Nonnull NecIxCombinedParser _parser;
  private final @Nonnull String _text;
  private final @Nonnull Warnings _w;
  private final @Nonnull SilentSyntaxCollection _silentSyntax;

  public NecIxControlPlaneExtractor(
      String fileText,
      NecIxCombinedParser combinedParser,
      Warnings warnings,
      SilentSyntaxCollection silentSyntax) {
    _text = fileText;
    _parser = combinedParser;
    _w = warnings;
    _silentSyntax = silentSyntax;
  }

  @Override
  public Set<String> implementedRuleNames() {
    return ImplementedRules.getImplementedRules(NecIxConfigurationBuilder.class);
  }

  @Override
  public VendorConfiguration getVendorConfiguration() {
    return _configuration;
  }

  @Override
  public void processParseTree(NetworkSnapshot snapshot, ParserRuleContext tree) {
    checkArgument(
        tree instanceof Nec_ix_configurationContext,
        "Expected %s, not %s",
        Nec_ix_configurationContext.class,
        tree.getClass());
    NecIxConfigurationBuilder cb = new NecIxConfigurationBuilder(_parser, _text, _w, _silentSyntax);
    new BatfishParseTreeWalker(_parser).walk(cb, tree);
    _configuration = cb.getConfiguration();
  }
}
