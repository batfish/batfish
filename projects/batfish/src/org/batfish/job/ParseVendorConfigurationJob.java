package org.batfish.job;

import java.io.File;
import java.util.concurrent.Callable;

import org.antlr.v4.runtime.ParserRuleContext;
import org.batfish.grammar.BatfishCombinedParser;
import org.batfish.grammar.ControlPlaneExtractor;
import org.batfish.grammar.cisco.CiscoCombinedParser;
import org.batfish.grammar.cisco.CiscoControlPlaneExtractor;
import org.batfish.grammar.flatjuniper.FlatJuniperCombinedParser;
import org.batfish.grammar.flatjuniper.FlatJuniperControlPlaneExtractor;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishException;
import org.batfish.main.BatfishLogger;
import org.batfish.main.ConfigurationFormat;
import org.batfish.main.ParserBatfishException;
import org.batfish.main.Settings;
import org.batfish.main.Warnings;
import org.batfish.representation.VendorConfiguration;

public class ParseVendorConfigurationJob implements
      Callable<ParseVendorConfigurationResult> {

   private static ConfigurationFormat identifyConfigurationFormat(
         String fileText) {
      char firstChar = fileText.trim().charAt(0);
      if (firstChar == '!') {
         if (fileText.contains("set prompt")) {
            return ConfigurationFormat.VXWORKS;
         }
         else if (fileText.contains("boot system flash")) {
            return ConfigurationFormat.ARISTA;
         }
         else {
            String[] lines = fileText.split("\\n");
            for (String line : lines) {
               String trimmedLine = line.trim();
               if (!trimmedLine.startsWith("!") && line.startsWith("version")) {
                  return ConfigurationFormat.CISCO;
               }
               else {
                  break;
               }
            }
         }
      }
      else if (fileText.contains("set hostname")) {
         return ConfigurationFormat.JUNIPER_SWITCH;
      }
      else if (firstChar == '#') {
         if (fileText.contains("set version")) {
            return ConfigurationFormat.FLAT_JUNIPER;
         }
         else {
            return ConfigurationFormat.JUNIPER;
         }
      }
      return ConfigurationFormat.UNKNOWN;
   }

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
      String currentPath = _file.getAbsolutePath();
      VendorConfiguration vc = null;
      if (_fileText.length() == 0) {
         return new ParseVendorConfigurationResult(_logger.getHistory());
      }
      BatfishCombinedParser<?, ?> combinedParser = null;
      ParserRuleContext tree = null;
      ControlPlaneExtractor extractor = null;
      ConfigurationFormat format = identifyConfigurationFormat(_fileText);

      switch (format) {

      case ARISTA:
      case CISCO:
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
            return new ParseVendorConfigurationResult(
                  _logger.getHistory(),
                  new BatfishException(
                        "Juniper configurations must be flattened prior to this stage"));
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
            return new ParseVendorConfigurationResult(_logger.getHistory(),
                  new BatfishException(unsupportedError));
         }
         else {
            _logger.warn(unsupportedError);
         }
         return new ParseVendorConfigurationResult(_logger.getHistory());

      case UNKNOWN:
      default:
         String unknownError = "Unknown configuration format for file: \""
               + currentPath + "\"\n";
         return new ParseVendorConfigurationResult(_logger.getHistory(),
               new BatfishException(unknownError));
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
         return new ParseVendorConfigurationResult(_logger.getHistory(),
               new BatfishException(error, e));
      }
      catch (Exception e) {
         String error = "Error post-processing parse tree of configuration file: \""
               + currentPath + "\"";
         return new ParseVendorConfigurationResult(_logger.getHistory(),
               new BatfishException(error, e));
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
         return new ParseVendorConfigurationResult(_logger.getHistory(),
               new BatfishException(error));
      }
      return new ParseVendorConfigurationResult(_logger.getHistory(), vc);
   }

}
