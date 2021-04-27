package org.batfish.main.preprocess;

import java.io.IOException;
import java.nio.file.Path;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.antlr.v4.runtime.ParserRuleContext;
import org.batfish.common.BatfishException;
import org.batfish.common.BatfishLogger;
import org.batfish.common.Warnings;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.grammar.BatfishCombinedParser;
import org.batfish.grammar.BatfishParseException;
import org.batfish.grammar.GrammarSettings;
import org.batfish.grammar.PreprocessExtractor;
import org.batfish.grammar.VendorConfigurationFormatDetector;
import org.batfish.grammar.flatjuniper.FlatJuniperCombinedParser;
import org.batfish.grammar.flatjuniper.PreprocessJuniperExtractor;
import org.batfish.grammar.flattener.Flattener;
import org.batfish.grammar.flattener.FlattenerLineMap;
import org.batfish.grammar.palo_alto.PaloAltoCombinedParser;
import org.batfish.grammar.palo_alto.PreprocessPaloAltoExtractor;
import org.batfish.main.Batfish;

@ParametersAreNonnullByDefault
public final class Preprocessor {

  /**
   * Performs pre-processing on a configuration file and returns the output. If the input text is
   * not recognized as a preprocessible configuration, it is returned unmodified.
   *
   * @throws IOException if there is an error parsing or pre-processing the input
   */
  public static @Nonnull String preprocess(
      GrammarSettings settings, String fileText, Path inputFile, Warnings warnings)
      throws IOException {
    BatfishLogger logger = new BatfishLogger(BatfishLogger.LEVELSTR_INFO, true);
    String flatConfigText;
    FlattenerLineMap lineMap;
    // If flat, proceed with original input text. If hierarchical, flatten first. If
    // not non-preprocessible, just return the original text.
    String header = null;
    ConfigurationFormat format =
        VendorConfigurationFormatDetector.identifyConfigurationFormat(fileText);
    try {
      Flattener flattener = Batfish.flatten(fileText, logger, settings, warnings, format, header);
      flatConfigText = flattener.getFlattenedConfigurationText();
      lineMap = flattener.getOriginalLineMap();
    } catch (Exception e) {
      throw new IOException(String.format("Error flattening %s", inputFile), e);
    } finally {
      Batfish.logWarnings(logger, warnings);
    }

    logger.debugf("Preprocessing config: \"%s\"...", inputFile);
    BatfishCombinedParser<?, ?> parser = null;
    PreprocessExtractor extractor = null;
    switch (format) {
      case JUNIPER:
      case FLAT_JUNIPER:
        FlatJuniperCombinedParser juniperParser =
            new FlatJuniperCombinedParser(flatConfigText, settings, lineMap);
        extractor = new PreprocessJuniperExtractor(juniperParser, warnings);
        parser = juniperParser;
        break;
      case PALO_ALTO:
      case PALO_ALTO_NESTED:
        PaloAltoCombinedParser paloAltoParser =
            new PaloAltoCombinedParser(flatConfigText, settings, lineMap);
        extractor = new PreprocessPaloAltoExtractor(paloAltoParser, warnings);
        parser = paloAltoParser;
        break;
      default:
        logger.debugf("Skipping: \"%s\"\n", inputFile);
        return fileText;
    }
    logger.info("\tParsing...");
    // Parse the flat text
    ParserRuleContext tree = Batfish.parse(parser, logger, settings);

    if (!parser.getErrors().isEmpty()) {
      throw new BatfishException(
          String.format(
              "Configuration file: '%s' contains unrecognized lines:\n%s",
              inputFile.toAbsolutePath(), String.join("\n", parser.getErrors())));
    }

    try {
      logger.info("\tPost-processing...");

      try {
        // Pre-process the initial flat parse tree
        extractor.processParseTree(tree);
      } catch (BatfishParseException e) {
        warnings.setErrorDetails(e.getErrorDetails());
        throw new IOException("Error processing parse tree", e);
      }

      logger.info("OK\n");
    } finally {
      Batfish.logWarnings(logger, warnings);
    }

    // Return configuration text corresponding to the pre-processed parse tree.
    return extractor.getPreprocessedConfigurationText();
  }

  private Preprocessor() {} // prevent instantiation
}
