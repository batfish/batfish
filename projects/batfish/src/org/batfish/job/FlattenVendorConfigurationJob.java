package org.batfish.job;

import java.io.File;

import org.batfish.main.Batfish;
import org.batfish.common.BatfishException;
import org.batfish.common.BatfishLogger;
import org.batfish.main.ConfigurationFormat;
import org.batfish.main.ParserBatfishException;
import org.batfish.main.Settings;
import org.batfish.main.Warnings;

public class FlattenVendorConfigurationJob extends
      BatfishJob<FlattenVendorConfigurationResult> {

   private String _fileText;

   private File _inputFile;

   private final BatfishLogger _logger;

   private File _outputFile;

   private Settings _settings;

   private Warnings _warnings;

   public FlattenVendorConfigurationJob(Settings settings, String fileText,
         File inputFile, File outputFile, Warnings warnings) {
      _settings = settings;
      _fileText = fileText;
      _inputFile = inputFile;
      _outputFile = outputFile;
      _warnings = warnings;
      _logger = new BatfishLogger(_settings.getLogLevel(),
            _settings.getTimestamp());
   }

   @Override
   public FlattenVendorConfigurationResult call() throws Exception {
      long startTime = System.currentTimeMillis();
      long elapsedTime;
      String inputFileAsString = _inputFile.getAbsolutePath();
      ConfigurationFormat format = Format
            .identifyConfigurationFormat(_fileText);

      if (format == ConfigurationFormat.JUNIPER) {
         _logger.debug("Flattening config: \"" + _inputFile.toString()
               + "\"...");
         String flatConfigText = null;
         try {
            flatConfigText = Batfish.flatten(_fileText, _logger, _settings);
         }
         catch (ParserBatfishException e) {
            String error = "Error parsing configuration file: \""
                  + inputFileAsString + "\"";
            elapsedTime = System.currentTimeMillis() - startTime;
            return new FlattenVendorConfigurationResult(elapsedTime,
                  _logger.getHistory(), new BatfishException(error, e));
         }
         catch (Exception e) {
            String error = "Error post-processing parse tree of configuration file: \""
                  + inputFileAsString + "\"";
            elapsedTime = System.currentTimeMillis() - startTime;
            return new FlattenVendorConfigurationResult(elapsedTime,
                  _logger.getHistory(), new BatfishException(error, e));
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
         elapsedTime = System.currentTimeMillis() - startTime;
         return new FlattenVendorConfigurationResult(elapsedTime,
               _logger.getHistory(), _outputFile, flatConfigText);
      }
      else if (!_settings.ignoreUnsupported()
            && format == ConfigurationFormat.UNKNOWN) {
         elapsedTime = System.currentTimeMillis() - startTime;
         return new FlattenVendorConfigurationResult(elapsedTime,
               _logger.getHistory(), new BatfishException(
                     "Unknown configuration format for: \""
                           + _inputFile.toString() + "\""));
      }
      else {
         _logger.debug("Skipping: \"" + _inputFile.toString() + "\"\n");
         String flatConfigText = _fileText;
         elapsedTime = System.currentTimeMillis() - startTime;
         return new FlattenVendorConfigurationResult(elapsedTime,
               _logger.getHistory(), _outputFile, flatConfigText);
      }
   }

}
