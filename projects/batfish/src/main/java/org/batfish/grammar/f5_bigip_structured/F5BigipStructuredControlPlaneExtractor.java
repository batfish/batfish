package org.batfish.grammar.f5_bigip_structured;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.batfish.common.BatfishException;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.ParseTreeSentences;
import org.batfish.common.Warnings;
import org.batfish.grammar.BatfishParseTreeWalker;
import org.batfish.grammar.ControlPlaneExtractor;
import org.batfish.grammar.ImplementedRules;
import org.batfish.grammar.ParseTreePrettyPrinter;
import org.batfish.grammar.f5_bigip_imish.F5BigipImishCombinedParser;
import org.batfish.grammar.f5_bigip_imish.F5BigipImishConfigurationBuilder;
import org.batfish.grammar.silent_syntax.SilentSyntaxCollection;
import org.batfish.representation.f5_bigip.F5BigipConfiguration;
import org.batfish.vendor.VendorConfiguration;

public class F5BigipStructuredControlPlaneExtractor implements ControlPlaneExtractor {

  private final F5BigipStructuredCombinedParser _parser;
  private final String _text;
  private final Warnings _w;
  private F5BigipConfiguration _configuration;
  private final String _filename;
  private final @Nullable Supplier<ParseTreeSentences> _ptSentences;
  private final @Nonnull SilentSyntaxCollection _silentSyntax;
  private final boolean _printParseTreeLineNums;

  public F5BigipStructuredControlPlaneExtractor(
      String fileText,
      F5BigipStructuredCombinedParser combinedParser,
      Warnings warnings,
      String filename,
      @Nullable Supplier<ParseTreeSentences> ptSentences,
      boolean printParseTreeLineNums,
      SilentSyntaxCollection silentSyntax) {
    _text = fileText;
    _parser = combinedParser;
    _w = warnings;
    _filename = filename;
    _ptSentences = ptSentences;
    _printParseTreeLineNums = printParseTreeLineNums;
    _silentSyntax = silentSyntax;
  }

  @Override
  public VendorConfiguration getVendorConfiguration() {
    return _configuration;
  }

  @Override
  public Set<String> implementedRuleNames() {
    return ImmutableSet.<String>builder()
        .addAll(ImplementedRules.getImplementedRules(F5BigipStructuredConfigurationBuilder.class))
        .addAll(ImplementedRules.getImplementedRules(F5BigipImishConfigurationBuilder.class))
        .build();
  }

  @Override
  public void processParseTree(NetworkSnapshot snapshot, ParserRuleContext tree) {
    F5BigipStructuredConfigurationBuilder cb =
        new F5BigipStructuredConfigurationBuilder(_parser, _text, _w, _silentSyntax);
    ParseTreeWalker walker = new BatfishParseTreeWalker(_parser);
    walker.walk(cb, tree);
    _configuration = cb.getConfiguration();

    /*
     * Everything below is a dirty stinking hack that works around current lack of support for
     * multi-file configs. All of it will be removed after that feature is added, since
     * at that point F5 BIG-IP imish configs will not appear in same file as F5 BIG-IP structured
     * configs.
     */
    Integer imishConfigurationOffset = cb.getImishConfigurationOffset();
    if (imishConfigurationOffset == null) {
      return;
    }
    F5BigipImishCombinedParser imishParser =
        new F5BigipImishCombinedParser(
            _text, _parser.getSettings(), imishConfigurationOffset, cb.getImishConfigurationLine());

    // parse imish content
    ParserRuleContext imishCtx = imishParser.parse();
    if (!imishParser.getErrors().isEmpty()) {
      throw new BatfishException(
          String.format(
              "Configuration file: '%s' contains unrecognized lines:\n%s",
              _filename, String.join("\n", imishParser.getErrors())));
    }

    ParseTreeWalker imishWalker = new BatfishParseTreeWalker(imishParser);
    F5BigipImishConfigurationBuilder icb =
        new F5BigipImishConfigurationBuilder(imishParser, _text, _w, _configuration, _silentSyntax);

    // merge in imish configuration
    imishWalker.walk(icb, imishCtx);

    // merge in imish parse tree if parse tree pretty printing is on.
    if (_ptSentences != null) {
      _ptSentences
          .get()
          .addAllSentences(
              ParseTreePrettyPrinter.getParseTreeSentences(
                  imishCtx,
                  imishParser,
                  _printParseTreeLineNums,
                  ImplementedRules.getImplementedRules(F5BigipImishConfigurationBuilder.class)));
    }
  }
}
