package org.batfish.vendor.sonic.grammar;

import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.batfish.common.BatfishException;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.ParseTreeSentences;
import org.batfish.common.Warnings;
import org.batfish.grammar.BatfishCombinedParser;
import org.batfish.grammar.BatfishParseTreeWalker;
import org.batfish.grammar.ControlPlaneExtractor;
import org.batfish.grammar.GrammarSettings;
import org.batfish.grammar.ImplementedRules;
import org.batfish.grammar.ParseTreePrettyPrinter;
import org.batfish.grammar.frr.FrrCombinedParser;
import org.batfish.grammar.frr.FrrConfigurationBuilder;
import org.batfish.grammar.frr.FrrParser.Frr_configurationContext;
import org.batfish.grammar.silent_syntax.SilentSyntaxCollection;
import org.batfish.vendor.VendorConfiguration;
import org.batfish.vendor.sonic.representation.SonicConfiguration;

public class SonicControlPlaneExtractor implements ControlPlaneExtractor {
  private static final Pattern LINE_PATTERN = Pattern.compile("(?m)^(.*)$");

  private final String _text;
  private final String _filename;
  private final GrammarSettings _grammarSettings;
  private final Supplier<ParseTreeSentences> _ptSentences;
  private final boolean _printParseTreeLineNums;
  private final Warnings _w;

  private SonicConfiguration _configuration;
  private int _line = -1;
  private int _offset = -1;
  private final List<String> _errors = new ArrayList<>();
  private final SilentSyntaxCollection _silentSyntax;

  public SonicControlPlaneExtractor(
      String fileText,
      Warnings warnings,
      String filename,
      GrammarSettings grammarSettings,
      @Nullable Supplier<ParseTreeSentences> ptSentences,
      boolean printParseTreeLineNums,
      SilentSyntaxCollection silentSyntax) {
    _text = fileText;
    _w = warnings;
    _filename = filename;
    _grammarSettings = grammarSettings;
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
        .addAll(ImplementedRules.getImplementedRules(FrrConfigurationBuilder.class))
        .build();
  }

  @Override
  public void processParseTree(NetworkSnapshot snapshot, ParserRuleContext tree) {
    _configuration = new SonicConfiguration();

    //    String hostname = _configuration.getHostname();
    //    if (hostname != null) {
    //      ConfigDb configDb =
    //          Optional.ofNullable((SonicConfigDbs) _parsingContext.getSonicConfigDbs())
    //              .flatMap(s -> s.getHostConfigDb(hostname))
    //              .orElse(null);
    //      if (configDb == null) {
    //        _w.redFlag("configdb file not found for " + hostname);
    //      }
    //      _configuration.setConfigDb(configDb);
    //    }

    parseFrrFile();
    checkErrors();
  }

  private void parseFrrFile() {
    FrrCombinedParser parser = new FrrCombinedParser(_text, _grammarSettings, _line, _offset);
    Frr_configurationContext ctxt = parser.parse();
    checkErrors(parser);
    ParseTreeWalker walker = new BatfishParseTreeWalker(parser);
    FrrConfigurationBuilder cb =
        new FrrConfigurationBuilder(_configuration, parser, _w, _text, _silentSyntax);
    walker.walk(cb, ctxt);
    mergeParseTree(ctxt, parser);
  }

  /** merge in parse tree if desired */
  private void mergeParseTree(ParserRuleContext ctxt, BatfishCombinedParser<?, ?> parser) {
    if (_ptSentences != null) {
      _ptSentences
          .get()
          .getSentences()
          .addAll(
              ParseTreePrettyPrinter.getParseTreeSentences(ctxt, parser, _printParseTreeLineNums)
                  .getSentences());
    }
  }

  private void checkErrors(BatfishCombinedParser<?, ?> parser) {
    if (!parser.getErrors().isEmpty()) {
      _errors.addAll(parser.getErrors());
    }
  }

  private void checkErrors() {
    if (!_errors.isEmpty()) {
      throw new BatfishException(
          String.format(
              "Configuration file: '%s' contains unrecognized lines:\n%s",
              _filename, String.join("\n", _errors)));
    }
  }
}
