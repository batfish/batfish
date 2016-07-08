package org.batfish.job;

import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.antlr.v4.runtime.ParserRuleContext;
import org.batfish.grammar.BatfishCombinedParser;
import org.batfish.grammar.ControlPlaneExtractor;
import org.batfish.grammar.cisco.CiscoCombinedParser;
import org.batfish.grammar.cisco.CiscoControlPlaneExtractor;
import org.batfish.grammar.flatjuniper.FlatJuniperCombinedParser;
import org.batfish.grammar.flatjuniper.FlatJuniperControlPlaneExtractor;
import org.batfish.grammar.flatvyos.FlatVyosCombinedParser;
import org.batfish.grammar.flatvyos.FlatVyosControlPlaneExtractor;
import org.batfish.grammar.mrv.MrvCombinedParser;
import org.batfish.grammar.mrv.MrvControlPlaneExtractor;
import org.batfish.main.Batfish;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.main.ParserBatfishException;
import org.batfish.main.Settings;
import org.batfish.main.Warnings;
import org.batfish.representation.VendorConfiguration;

public class ParseVendorConfigurationJob extends
      BatfishJob<ParseVendorConfigurationResult> {

   private Path _file;

   private String _fileText;

   private Warnings _warnings;

   public ParseVendorConfigurationJob(Settings settings, String fileText,
         Path file, Warnings warnings) {
      super(settings);
      _fileText = fileText;
      _file = file;
      _warnings = warnings;
   }

   @Override
   public ParseVendorConfigurationResult call() throws Exception {
      long startTime = System.currentTimeMillis();
      long elapsedTime;
      String currentPath = _file.toAbsolutePath().toString();
      VendorConfiguration vc = null;
      BatfishCombinedParser<?, ?> combinedParser = null;
      ParserRuleContext tree = null;
      ControlPlaneExtractor extractor = null;
      ConfigurationFormat format = Format
            .identifyConfigurationFormat(_fileText);

      if (format == ConfigurationFormat.EMPTY) {
         elapsedTime = System.currentTimeMillis() - startTime;
         return new ParseVendorConfigurationResult(elapsedTime,
               _logger.getHistory(), _file, _warnings);
      }
      switch (format) {

      case ARISTA:
      case CISCO:
      case CISCO_IOS_XR:
         boolean nonNexus = checkNonNexus(_fileText);
         CiscoCombinedParser ciscoParser = new CiscoCombinedParser(_fileText,
               _settings, nonNexus);
         combinedParser = ciscoParser;
         extractor = new CiscoControlPlaneExtractor(_fileText, ciscoParser,
               _warnings, _settings.getUnrecognizedAsRedFlag());
         break;

      case VYOS:
         if (_settings.flattenOnTheFly()) {
            String msg = "Flattening: \""
                  + currentPath
                  + "\" on-the-fly; line-numbers reported for this file will be spurious\n";
            _warnings.pedantic(msg);
            // _logger
            // .warn("Flattening: \""
            // + currentPath
            // +
            // "\" on-the-fly; line-numbers reported for this file will be spurious\n");
            _fileText = Batfish.flatten(_fileText, _logger, _settings,
                  ConfigurationFormat.VYOS,
                  Format.BATFISH_FLATTENED_VYOS_HEADER);
         }
         else {
            elapsedTime = System.currentTimeMillis() - startTime;
            return new ParseVendorConfigurationResult(elapsedTime,
                  _logger.getHistory(), _file, new BatfishException(
                        "Vyos configurations must be flattened prior to this stage: \""
                              + _file.toString() + "\""));
         }
         // MISSING BREAK IS INTENTIONAL
      case FLAT_VYOS:
         FlatVyosCombinedParser flatVyosParser = new FlatVyosCombinedParser(
               _fileText, _settings);
         combinedParser = flatVyosParser;
         extractor = new FlatVyosControlPlaneExtractor(_fileText,
               flatVyosParser, _warnings);
         break;

      case JUNIPER:
         if (_settings.flattenOnTheFly()) {
            String msg = "Flattening: \""
                  + currentPath
                  + "\" on-the-fly; line-numbers reported for this file will be spurious\n";
            _warnings.pedantic(msg);
            // _logger
            // .warn("Flattening: \""
            // + currentPath
            // +
            // "\" on-the-fly; line-numbers reported for this file will be spurious\n");
            _fileText = Batfish.flatten(_fileText, _logger, _settings,
                  ConfigurationFormat.JUNIPER,
                  Format.BATFISH_FLATTENED_JUNIPER_HEADER);
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
               _fileText, _settings);
         combinedParser = flatJuniperParser;
         extractor = new FlatJuniperControlPlaneExtractor(_fileText,
               flatJuniperParser, _warnings);
         break;

      case MRV:
         MrvCombinedParser mrvParser = new MrvCombinedParser(_fileText,
               _settings);
         combinedParser = mrvParser;
         extractor = new MrvControlPlaneExtractor(_fileText, mrvParser,
               _warnings);
         break;

      case AWS_VPC:
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
            // _logger.warn(unsupportedError);
            _warnings.unimplemented(unsupportedError);
         }
         elapsedTime = System.currentTimeMillis() - startTime;
         return new ParseVendorConfigurationResult(elapsedTime,
               _logger.getHistory(), _file, _warnings);

      case EMPTY:
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
         String error = "Error parsing configuration file: '" + currentPath
               + "'";
         elapsedTime = System.currentTimeMillis() - startTime;
         return new ParseVendorConfigurationResult(elapsedTime,
               _logger.getHistory(), _file, new BatfishException(error, e));
      }
      catch (Exception e) {
         String error = "Error post-processing parse tree of configuration file: '"
               + currentPath + "'";
         elapsedTime = System.currentTimeMillis() - startTime;
         return new ParseVendorConfigurationResult(elapsedTime,
               _logger.getHistory(), _file, new BatfishException(error, e));
      }
      finally {
         Batfish.logWarnings(_logger, _warnings);
      }
      vc = extractor.getVendorConfiguration();
      vc.setVendor(format);
      // at this point we should have a VendorConfiguration vc
      String hostname = vc.getHostname();
      if (hostname == null) {
         String error = "No hostname set in file: '" + _file + "'\n";
         try {
            _warnings.redFlag(error);
         }
         catch (BatfishException e) {
            elapsedTime = System.currentTimeMillis() - startTime;
            return new ParseVendorConfigurationResult(elapsedTime,
                  _logger.getHistory(), _file, e);
         }
         String filename = _file.getFileName().toString();
         String guessedHostname = filename.replaceAll("\\.(cfg|conf)$", "");
         _logger
               .redflag("\tNo hostname set! Guessing hostname from filename: \""
                     + filename + "\" ==> \"" + guessedHostname + "\"\n");
         vc.setHostname(guessedHostname);
      }
      elapsedTime = System.currentTimeMillis() - startTime;
      return new ParseVendorConfigurationResult(elapsedTime,
            _logger.getHistory(), _file, vc, _warnings);
   }

   private boolean checkNonNexus(String fileText) {
      Matcher neighborActivateMatcher = Pattern.compile(
            "(?m)^neighbor.*activate$")

      .matcher(fileText);
      return fileText.contains("exit-address-family")
            || neighborActivateMatcher.find();
   }

}
