package org.batfish.job;

import java.io.File;
import java.util.concurrent.Callable;

import org.batfish.main.Batfish;
import org.batfish.main.BatfishException;
import org.batfish.main.BatfishLogger;
import org.batfish.main.ConfigurationFormat;
import org.batfish.main.ParserBatfishException;
import org.batfish.main.Settings;
import org.batfish.main.Warnings;

public class FlattenVendorConfigurationJob implements
      Callable<FlattenVendorConfigurationResult> {

   private String _fileText;

   private File _inputFile;

   private final BatfishLogger _logger;

   private File _outputFile;

   private Settings _settings;

   private Warnings _warnings;

   public FlattenVendorConfigurationJob(Settings settings, String fileText,
         File file, File outputFile, Warnings warnings) {
      _settings = settings;
      _fileText = fileText;
      _inputFile = file;
      _outputFile = outputFile;
      _warnings = warnings;
      _logger = new BatfishLogger(_settings.getLogLevel(),
            _settings.getTimestamp());
   }

   @Override
   public FlattenVendorConfigurationResult call() throws Exception {
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
            return new FlattenVendorConfigurationResult(_logger.getHistory(),
                  new BatfishException(error, e));
         }
         catch (Exception e) {
            String error = "Error post-processing parse tree of configuration file: \""
                  + inputFileAsString + "\"";
            return new FlattenVendorConfigurationResult(_logger.getHistory(),
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
         return new FlattenVendorConfigurationResult(_logger.getHistory(),
               _outputFile, flatConfigText);
      }
      else {
         _logger.debug("Skipping: \"" + _inputFile.toString() + "\"\n");
         String flatConfigText = _fileText;
         return new FlattenVendorConfigurationResult(_logger.getHistory(),
               _outputFile, flatConfigText);
      }
   }

}
