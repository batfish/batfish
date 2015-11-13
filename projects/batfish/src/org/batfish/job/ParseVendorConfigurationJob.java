package org.batfish.job;

import java.io.File;

import org.antlr.v4.runtime.ParserRuleContext;
import org.batfish.grammar.BatfishCombinedParser;
import org.batfish.grammar.ControlPlaneExtractor;
import org.batfish.grammar.cisco.CiscoCombinedParser;
import org.batfish.grammar.cisco.CiscoControlPlaneExtractor;
import org.batfish.grammar.flatjuniper.FlatJuniperCombinedParser;
import org.batfish.grammar.flatjuniper.FlatJuniperControlPlaneExtractor;
import org.batfish.main.Batfish;
import org.batfish.common.BatfishException;
import org.batfish.common.BatfishLogger;
import org.batfish.main.ConfigurationFormat;
import org.batfish.main.ParserBatfishException;
import org.batfish.main.Settings;
import org.batfish.main.Warnings;
import org.batfish.representation.VendorConfiguration;

public class ParseVendorConfigurationJob extends
      BatfishJob<ParseVendorConfigurationResult> {

   private File _file;

   private String _fileText;

   private final BatfishLogger _logger;

   private Settings _settings;

   private Warnings _warnings;

   public ParseVendorConfigurationJob(Settings settings, String fileText,
         File file, Warnings warnings) {
      _settings = settings;
      _fileText = fileText;
      _file = file;
      _warnings = warnings;
      _logger = new BatfishLogger(_settings.getLogLevel(),
            _settings.getTimestamp());
   }

   @Override
   public ParseVendorConfigurationResult call() throws Exception {
      long startTime = System.currentTimeMillis();
      long elapsedTime;
      String currentPath = _file.getAbsolutePath();
      VendorConfiguration vc = null;
      if (_fileText.length() == 0) {
         elapsedTime = System.currentTimeMillis() - startTime;
         return new ParseVendorConfigurationResult(elapsedTime,
               _logger.getHistory(), _file);
      }
      BatfishCombinedParser<?, ?> combinedParser = null;
      ParserRuleContext tree = null;
      ControlPlaneExtractor extractor = null;
      ConfigurationFormat format = Format
            .identifyConfigurationFormat(_fileText);

      switch (format) {

      case ARISTA:
      case CISCO:
      case CISCO_IOS_XR:
         CiscoCombinedParser ciscoParser = new CiscoCombinedParser(_fileText,
               _settings.getThrowOnParserError(),
               _settings.getThrowOnLexerError());
         combinedParser = ciscoParser;
         extractor = new CiscoControlPlaneExtractor(_fileText, ciscoParser,
               _warnings);
         break;

      case JUNIPER:
         if (_settings.flattenOnTheFly()) {
            _logger
                  .warn("Flattening: \""
                        + currentPath
                        + "\" on-the-fly; line-numbers reported for this file will be spurious\n");
            _fileText = Batfish.flatten(_fileText, _logger, _settings);
         }
         else {
            elapsedTime = System.currentTimeMillis() - startTime;
            return new ParseVendorConfigurationResult(elapsedTime,
                  _logger.getHistory(), _file, new BatfishException(
                        "Juniper configurations must be flattened prior to this stage: \""
                              + _file.toString() + "\""));
         }
         // MISSING BREAK IS INTENTIONAL
      case FLAT_JUNIPER:
         FlatJuniperCombinedParser flatJuniperParser = new FlatJuniperCombinedParser(
               _fileText, _settings.getThrowOnParserError(),
               _settings.getThrowOnLexerError());
         combinedParser = flatJuniperParser;
         extractor = new FlatJuniperControlPlaneExtractor(_fileText,
               flatJuniperParser, _warnings);
         break;

      case JUNIPER_SWITCH:
      case VXWORKS:
         String unsupportedError = "Unsupported configuration format: \""
               + format.toString() + "\" for file: \"" + currentPath + "\"\n";
         if (!_settings.ignoreUnsupported()) {
            elapsedTime = System.currentTimeMillis() - startTime;
            return new ParseVendorConfigurationResult(elapsedTime,
                  _logger.getHistory(), _file, new BatfishException(
                        unsupportedError));
         }
         else {
            _logger.warn(unsupportedError);
         }
         elapsedTime = System.currentTimeMillis() - startTime;
         return new ParseVendorConfigurationResult(elapsedTime,
               _logger.getHistory(), _file);

      case UNKNOWN:
      default:
         String unknownError = "Unknown configuration format for file: \""
               + currentPath + "\"\n";
         elapsedTime = System.currentTimeMillis() - startTime;
         return new ParseVendorConfigurationResult(elapsedTime,
               _logger.getHistory(), _file, new BatfishException(unknownError));
      }

      try {
         tree = Batfish.parse(combinedParser, currentPath, _logger, _settings);
         _logger.info("\tPost-processing...");
         extractor.processParseTree(tree);
         _logger.info("OK\n");
      }
      catch (ParserBatfishException e) {
         String error = "Error parsing configuration file: \"" + currentPath
               + "\"";
         elapsedTime = System.currentTimeMillis() - startTime;
         return new ParseVendorConfigurationResult(elapsedTime,
               _logger.getHistory(), _file, new BatfishException(error, e));
      }
      catch (Exception e) {
         String error = "Error post-processing parse tree of configuration file: \""
               + currentPath + "\"";
         elapsedTime = System.currentTimeMillis() - startTime;
         return new ParseVendorConfigurationResult(elapsedTime,
               _logger.getHistory(), _file, new BatfishException(error, e));
      }
      finally {
         for (String warning : _warnings.getRedFlagWarnings()) {
            _logger.redflag(warning);
         }
         for (String warning : _warnings.getUnimplementedWarnings()) {
            _logger.unimplemented(warning);
         }
         for (String warning : _warnings.getPedanticWarnings()) {
            _logger.pedantic(warning);
         }
      }
      vc = extractor.getVendorConfiguration();
      vc.setVendor(format);
      // at this point we should have a VendorConfiguration vc
      String hostname = vc.getHostname();
      if (hostname == null) {
         String error = "No hostname set in file: \"" + _file + "\"\n";
         try {
            _warnings.redFlag(error);
         }
         catch (BatfishException e) {
            elapsedTime = System.currentTimeMillis() - startTime;
            return new ParseVendorConfigurationResult(elapsedTime,
                  _logger.getHistory(), _file, e);
         }
         String filename = _file.getName();
         String guessedHostname = filename.replaceAll("\\.(cfg|conf)$", "");
         _logger
               .redflag("\tNo hostname set! Guessing hostname from filename: \""
                     + filename + "\" ==> \"" + guessedHostname + "\"\n");
         vc.setHostname(guessedHostname);
      }
      elapsedTime = System.currentTimeMillis() - startTime;
      return new ParseVendorConfigurationResult(elapsedTime,
            _logger.getHistory(), _file, vc);
   }

}
