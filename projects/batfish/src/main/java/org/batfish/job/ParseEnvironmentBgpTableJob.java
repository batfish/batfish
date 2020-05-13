package org.batfish.job;

import java.io.IOException;
import java.util.SortedMap;
import org.antlr.v4.runtime.ParserRuleContext;
import org.batfish.common.BatfishException;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.ParseTreeSentences;
import org.batfish.common.Warnings;
import org.batfish.common.plugin.BgpTablePlugin;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.config.Settings;
import org.batfish.datamodel.answers.ParseStatus;
import org.batfish.datamodel.collections.BgpAdvertisementsByVrf;
import org.batfish.grammar.BatfishCombinedParser;
import org.batfish.grammar.BgpTableExtractor;
import org.batfish.grammar.BgpTableFormat;
import org.batfish.grammar.BgpTableFormatDetector;
import org.batfish.grammar.ParseTreePrettyPrinter;
import org.batfish.main.Batfish;
import org.batfish.main.ParserBatfishException;

public class ParseEnvironmentBgpTableJob extends BatfishJob<ParseEnvironmentBgpTableResult> {

  private SortedMap<BgpTableFormat, BgpTablePlugin> _bgpTablePlugins;
  private String _key;
  private String _objectText;
  private String _hostname;
  private ParseTreeSentences _ptSentences;
  private final NetworkSnapshot _snapshot;
  private Warnings _warnings;

  public ParseEnvironmentBgpTableJob(
      Settings settings,
      NetworkSnapshot snapshot,
      String objectText,
      String hostname,
      String key,
      Warnings warnings,
      SortedMap<BgpTableFormat, BgpTablePlugin> bgpTablePlugins) {
    super(settings);
    _bgpTablePlugins = bgpTablePlugins;
    _objectText = objectText;
    _key = key;
    _hostname = hostname;
    _ptSentences = new ParseTreeSentences();
    _snapshot = snapshot;
    _warnings = warnings;
  }

  @Override
  public ParseEnvironmentBgpTableResult call() {
    long startTime = System.currentTimeMillis();
    long elapsedTime;
    ParserRuleContext tree = null;
    _logger.infof("Processing: '%s'\n", _key);
    BgpTablePlugin plugin = null;
    BgpTableFormat format = BgpTableFormatDetector.identifyBgpTableFormat(_objectText);
    switch (format) {
      case EMPTY:
        _warnings.redFlag("Empty object: '" + _key + "'\n");
        elapsedTime = System.currentTimeMillis() - startTime;
        return new ParseEnvironmentBgpTableResult(
            elapsedTime, _logger.getHistory(), _key, _warnings, ParseStatus.EMPTY);

      case JSON:
        elapsedTime = System.currentTimeMillis() - startTime;
        try {
          return new ParseEnvironmentBgpTableResult(
              elapsedTime,
              _logger.getHistory(),
              _key,
              _hostname,
              BatfishObjectMapper.mapper().readValue(_objectText, BgpAdvertisementsByVrf.class),
              _warnings,
              _ptSentences);
        } catch (IOException e) {
          elapsedTime = System.currentTimeMillis() - startTime;
          return new ParseEnvironmentBgpTableResult(elapsedTime, _logger.getHistory(), _key, e);
        } finally {
          Batfish.logWarnings(_logger, _warnings);
        }

      case UNKNOWN:
        String unknownError = "Unknown bgp-table format for object: '" + _key + "'\n";
        if (!_settings.ignoreUnknown()) {
          elapsedTime = System.currentTimeMillis() - startTime;
          return new ParseEnvironmentBgpTableResult(
              elapsedTime, _logger.getHistory(), _key, new BatfishException(unknownError));
        } else {
          _warnings.unimplemented(unknownError);
          elapsedTime = System.currentTimeMillis() - startTime;
          return new ParseEnvironmentBgpTableResult(
              elapsedTime, _logger.getHistory(), _key, _warnings, ParseStatus.UNKNOWN);
        }

        // $CASES-OMITTED$
      default:
        break;
    }
    plugin = _bgpTablePlugins.get(format);
    if (plugin == null) {
      String unsupportedError =
          "Unsupported bgp-table format: '"
              + format.bgpTableFormatName()
              + "' for file: '"
              + _key
              + "'\n";
      if (!_settings.ignoreUnsupported()) {
        elapsedTime = System.currentTimeMillis() - startTime;
        return new ParseEnvironmentBgpTableResult(
            elapsedTime, _logger.getHistory(), _key, new BatfishException(unsupportedError));
      } else {
        _warnings.unimplemented(unsupportedError);
        elapsedTime = System.currentTimeMillis() - startTime;
        return new ParseEnvironmentBgpTableResult(
            elapsedTime, _logger.getHistory(), _key, _warnings, ParseStatus.UNSUPPORTED);
      }
    }
    BgpTableExtractor extractor;
    try {
      _logger.info("\tParsing...");
      BatfishCombinedParser<?, ?> combinedParser = plugin.parser(_objectText, _settings);
      extractor = plugin.extractor(_hostname, _objectText, combinedParser, _warnings);
      tree = Batfish.parse(combinedParser, _logger, _settings);
      if (_settings.getPrintParseTree()) {
        _ptSentences =
            ParseTreePrettyPrinter.getParseTreeSentences(
                tree, combinedParser, _settings.getPrintParseTreeLineNums());
      }
      _logger.info("\tPost-processing...");
      extractor.processParseTree(_snapshot, tree);
      _logger.info("OK\n");
    } catch (ParserBatfishException e) {
      String error = "Error parsing configuration file: '" + _key + "'";
      elapsedTime = System.currentTimeMillis() - startTime;
      return new ParseEnvironmentBgpTableResult(
          elapsedTime, _logger.getHistory(), _key, new BatfishException(error, e));
    } catch (Exception e) {
      String error = "Error post-processing parse tree of configuration file: '" + _key + "'";
      elapsedTime = System.currentTimeMillis() - startTime;
      return new ParseEnvironmentBgpTableResult(
          elapsedTime, _logger.getHistory(), _key, new BatfishException(error, e));
    } finally {
      Batfish.logWarnings(_logger, _warnings);
    }
    BgpAdvertisementsByVrf bgpAdvertisementsByVrf = extractor.getBgpAdvertisementsByVrf();
    elapsedTime = System.currentTimeMillis() - startTime;
    return new ParseEnvironmentBgpTableResult(
        elapsedTime,
        _logger.getHistory(),
        _key,
        _hostname,
        bgpAdvertisementsByVrf,
        _warnings,
        _ptSentences);
  }
}
