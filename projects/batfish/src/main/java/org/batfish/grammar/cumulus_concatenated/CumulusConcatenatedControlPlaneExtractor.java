package org.batfish.grammar.cumulus_concatenated;

import java.util.function.Supplier;
import javax.annotation.Nullable;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.batfish.common.BatfishException;
import org.batfish.common.ParseTreeSentences;
import org.batfish.common.Warnings;
import org.batfish.grammar.BatfishCombinedParser;
import org.batfish.grammar.BatfishLexer;
import org.batfish.grammar.BatfishParseTreeWalker;
import org.batfish.grammar.BatfishParser;
import org.batfish.grammar.ControlPlaneExtractor;
import org.batfish.grammar.GrammarSettings;
import org.batfish.grammar.ParseTreePrettyPrinter;
import org.batfish.grammar.cumulus_ports.CumulusPortsCombinedParser;
import org.batfish.grammar.cumulus_ports.CumulusPortsConfigurationBuilder;
import org.batfish.grammar.cumulus_ports.CumulusPortsParser.Cumulus_ports_configurationContext;
import org.batfish.representation.cumulus.CumulusNcluConfiguration;
import org.batfish.vendor.VendorConfiguration;

public class CumulusConcatenatedControlPlaneExtractor implements ControlPlaneExtractor {
  private static final String START_OF_CUMULUS_INTERFACES_FILE =
      "# This file describes the network interfaces";

  private final String _text;
  private final String _filename;
  private final GrammarSettings _grammarSettings;
  private final Supplier<ParseTreeSentences> _ptSentences;
  private final boolean _printParseTreeLineNums;
  private final Warnings _w;

  private CumulusNcluConfiguration _configuration;
  private int _line = -1;
  private int _offset = -1;

  public CumulusConcatenatedControlPlaneExtractor(
      String fileText,
      Warnings warnings,
      String filename,
      GrammarSettings grammarSettings,
      @Nullable Supplier<ParseTreeSentences> ptSentences,
      boolean printParseTreeLineNums) {
    _text = fileText;
    _w = warnings;
    _filename = filename;
    _grammarSettings = grammarSettings;
    _ptSentences = ptSentences;
    _printParseTreeLineNums = printParseTreeLineNums;
  }

  @Override
  public VendorConfiguration getVendorConfiguration() {
    return _configuration;
  }

  @Override
  public void processParseTree(ParserRuleContext tree) {
    _configuration = new CumulusNcluConfiguration();

    parseHostname();
    parsePortsFile();
    parseInterfacesFile();
    parseFrrFile();
  }

  private void parseFrrFile() {}

  private void parseInterfacesFile() {}

  private void parsePortsFile() {
    int len = _text.indexOf(START_OF_CUMULUS_INTERFACES_FILE);
    String text = len > 0 ? _text.substring(0, len) : _text;

    CumulusPortsCombinedParser parser =
        new CumulusPortsCombinedParser(text, _grammarSettings, _line, _offset);
    Cumulus_ports_configurationContext ctxt = parser.parse();
    checkErrors(parser);
    ParseTreeWalker walker = new BatfishParseTreeWalker(parser);
    CumulusPortsConfigurationBuilder cb = new CumulusPortsConfigurationBuilder(_configuration);
    walker.walk(cb, ctxt);
    mergeParseTree(ctxt, parser);

    Token startOfCumulusInterfacesFile = ctxt.getStop();
    _line = startOfCumulusInterfacesFile.getLine();
    _offset = startOfCumulusInterfacesFile.getStartIndex();
  }

  private void parseHostname() {
    int endOfHostname = _text.indexOf("\n");
    String hostname = _text.substring(0, endOfHostname).trim();
    _configuration.setHostname(hostname);
    _line = 2;
    _offset = endOfHostname + 1;
  }

  /** merge in parse tree if desired */
  private <P extends BatfishParser, L extends BatfishLexer> void mergeParseTree(
      ParserRuleContext ctxt, BatfishCombinedParser<P, L> parser) {
    if (_ptSentences != null) {
      _ptSentences
          .get()
          .getSentences()
          .addAll(
              ParseTreePrettyPrinter.getParseTreeSentences(ctxt, parser, _printParseTreeLineNums)
                  .getSentences());
    }
  }

  private <P extends BatfishParser, L extends BatfishLexer> void checkErrors(
      BatfishCombinedParser<P, L> parser) {
    if (!parser.getErrors().isEmpty()) {
      throw new BatfishException(
          String.format(
              "Configuration file: '%s' contains unrecognized lines:\n%s",
              _filename, String.join("\n", parser.getErrors())));
    }
  }
}
