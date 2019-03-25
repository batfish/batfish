package org.batfish.job;

import java.nio.file.Path;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.antlr.v4.runtime.ParserRuleContext;
import org.batfish.common.BatfishException;
import org.batfish.common.Warnings;
import org.batfish.config.Settings;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.grammar.BatfishParseException;
import org.batfish.grammar.VendorConfigurationFormatDetector;
import org.batfish.grammar.flatjuniper.FlatJuniperCombinedParser;
import org.batfish.grammar.flatjuniper.PreprocessJuniperExtractor;
import org.batfish.grammar.flattener.Flattener;
import org.batfish.grammar.flattener.FlattenerLineMap;
import org.batfish.main.Batfish;
import org.batfish.main.ParserBatfishException;

/**
 * {@link BatfishJob} that performs Juniper pre-processing on a configuration file and returns the
 * output in a {@link PreprocessJuniperResult}. If the input text is not recognized as a Juniper
 * configuration, it is returned unmodified.
 */
@ParametersAreNonnullByDefault
public final class PreprocessJuniperJob extends BatfishJob<PreprocessJuniperResult> {

  private @Nonnull String _fileText;
  private @Nonnull Path _inputFile;
  private @Nonnull Path _outputFile;
  private @Nonnull Warnings _warnings;

  public PreprocessJuniperJob(
      Settings settings, String fileText, Path inputFile, Path outputFile, Warnings warnings) {
    super(settings);
    _fileText = fileText;
    _inputFile = inputFile;
    _outputFile = outputFile;
    _warnings = warnings;
  }

  @Override
  public @Nonnull PreprocessJuniperResult call() {
    long startTime = System.currentTimeMillis();
    long elapsedTime;
    String inputFileAsString = _inputFile.toAbsolutePath().toString();
    String flatConfigText;
    FlattenerLineMap lineMap;
    // If flat Juniper, proceed with original input text. If hierarchical Juniper, flatten first. If
    // non-Juniper, just return the original text.
    switch (VendorConfigurationFormatDetector.identifyConfigurationFormat(_fileText)) {
      case FLAT_JUNIPER:
        flatConfigText = _fileText;
        lineMap = null;
        break;

      case JUNIPER:
        {
          try {
            Flattener flattener =
                Batfish.flatten(
                    _fileText,
                    _logger,
                    _settings,
                    _warnings,
                    ConfigurationFormat.JUNIPER,
                    VendorConfigurationFormatDetector.BATFISH_FLATTENED_JUNIPER_HEADER);
            flatConfigText = flattener.getFlattenedConfigurationText();
            lineMap = flattener.getOriginalLineMap();
          } catch (ParserBatfishException e) {
            String error = "Error parsing configuration file: \"" + inputFileAsString + "\"";
            elapsedTime = System.currentTimeMillis() - startTime;
            return new PreprocessJuniperResult(
                elapsedTime, _logger.getHistory(), _outputFile, new BatfishException(error, e));
          } catch (Exception e) {
            String error =
                "Error post-processing parse tree of configuration file: \""
                    + inputFileAsString
                    + "\"";
            elapsedTime = System.currentTimeMillis() - startTime;
            return new PreprocessJuniperResult(
                elapsedTime, _logger.getHistory(), _outputFile, new BatfishException(error, e));
          } finally {
            Batfish.logWarnings(_logger, _warnings);
          }
          break;
        }

      default:
        _logger.debugf("Skipping: \"%s\"\n", _inputFile);
        elapsedTime = System.currentTimeMillis() - startTime;
        return new PreprocessJuniperResult(
            elapsedTime, _logger.getHistory(), _outputFile, _fileText);
    }

    _logger.debugf("Preprocessing Juniper config: \"%s\"...", _inputFile);
    FlatJuniperCombinedParser parser =
        new FlatJuniperCombinedParser(flatConfigText, _settings, lineMap);
    PreprocessJuniperExtractor extractor =
        new PreprocessJuniperExtractor(flatConfigText, parser, _warnings);
    _logger.info("\tParsing...");
    // Parse the flat Juniper text
    ParserRuleContext tree = Batfish.parse(parser, _logger, _settings);

    if (!parser.getErrors().isEmpty()) {
      throw new BatfishException(
          String.format(
              "Configuration file: '%s' contains unrecognized lines:\n%s",
              inputFileAsString, String.join("\n", parser.getErrors())));
    }

    try {
      _logger.info("\tPost-processing...");

      try {
        // Pre-process the initial flat Juniper parse tree
        extractor.processParseTree(tree);
      } catch (BatfishParseException e) {
        _warnings.setErrorDetails(e.getErrorDetails());
        throw new BatfishException("Error processing parse tree", e);
      }

      _logger.info("OK\n");
    } finally {
      Batfish.logWarnings(_logger, _warnings);
    }

    elapsedTime = System.currentTimeMillis() - startTime;
    // Return configuration text corresponding to the pre-processed parse tree.
    return new PreprocessJuniperResult(
        elapsedTime,
        _logger.getHistory(),
        _outputFile,
        extractor.getPreprocessedConfigurationText());
  }
}
