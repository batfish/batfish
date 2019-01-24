package org.batfish.job;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Multimap;
import io.opentracing.ActiveSpan;
import io.opentracing.References;
import io.opentracing.SpanContext;
import io.opentracing.util.GlobalTracer;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import org.antlr.v4.runtime.ParserRuleContext;
import org.batfish.common.BatfishException;
import org.batfish.common.ParseTreeSentences;
import org.batfish.common.Warnings;
import org.batfish.config.Settings;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.answers.ParseStatus;
import org.batfish.grammar.BatfishCombinedParser;
import org.batfish.grammar.ControlPlaneExtractor;
import org.batfish.grammar.ParseTreePrettyPrinter;
import org.batfish.grammar.VendorConfigurationFormatDetector;
import org.batfish.grammar.cisco.CiscoCombinedParser;
import org.batfish.grammar.cisco.CiscoControlPlaneExtractor;
import org.batfish.grammar.flatjuniper.FlatJuniperCombinedParser;
import org.batfish.grammar.flatjuniper.FlatJuniperControlPlaneExtractor;
import org.batfish.grammar.flattener.Flattener;
import org.batfish.grammar.flattener.FlattenerLineMap;
import org.batfish.grammar.flatvyos.FlatVyosCombinedParser;
import org.batfish.grammar.flatvyos.FlatVyosControlPlaneExtractor;
import org.batfish.grammar.iptables.IptablesCombinedParser;
import org.batfish.grammar.iptables.IptablesControlPlaneExtractor;
import org.batfish.grammar.mrv.MrvCombinedParser;
import org.batfish.grammar.mrv.MrvControlPlaneExtractor;
import org.batfish.grammar.palo_alto.PaloAltoCombinedParser;
import org.batfish.grammar.palo_alto.PaloAltoControlPlaneExtractor;
import org.batfish.main.Batfish;
import org.batfish.main.ParserBatfishException;
import org.batfish.representation.host.HostConfiguration;
import org.batfish.vendor.VendorConfiguration;

public class ParseVendorConfigurationJob extends BatfishJob<ParseVendorConfigurationResult> {

  private static final Pattern BANNER_PATTERN =
      Pattern.compile("(?m)banner[ \t][ \t]*[^ \r\n\t][^ \r\n\t]*[ \t][ \t]*([^ \r\n\t])[ \r\n]");

  private static String preprocessBanner(String fileText, ConfigurationFormat format) {
    if (format == ConfigurationFormat.CADANT) {
      return fileText;
    }
    Matcher matcher = BANNER_PATTERN.matcher(fileText);
    if (matcher.find()) {
      int delimiterIndex = matcher.start(1);
      char delimiter = fileText.charAt(delimiterIndex);
      String delimiterText = (delimiter == '^' ? "\\^" : ("[" + delimiter + "]"));
      Pattern finalDelimiterPattern = Pattern.compile("(?m)[" + delimiterText + "][\r\n]");
      Matcher finalDelimiterMatcher = finalDelimiterPattern.matcher(fileText);
      if (finalDelimiterMatcher.find(delimiterIndex + 1)) {
        int finalDelimiterIndex = finalDelimiterMatcher.start();
        String beforeDelimiter = fileText.substring(0, delimiterIndex);
        String betweenDelimiters = fileText.substring(delimiterIndex + 1, finalDelimiterIndex);
        String afterDelimiter = fileText.substring(finalDelimiterIndex + 1);
        String newFileText = beforeDelimiter + "^C" + betweenDelimiters + "^C" + afterDelimiter;
        return newFileText;
      } else {
        throw new BatfishException("Invalid banner");
      }
    } else {
      return fileText;
    }
  }

  /** Information about duplicate hostnames is collected here */
  private Multimap<String, String> _duplicateHostnames;

  /** The name of the parsed file, relative to the testrig base. */
  private String _filename;

  private String _fileText;

  private ConfigurationFormat _format;

  private ParseTreeSentences _ptSentences;

  @Nullable private SpanContext _spanContext;

  private Warnings _warnings;

  public ParseVendorConfigurationJob(
      Settings settings,
      String fileText,
      String filename,
      Warnings warnings,
      ConfigurationFormat configurationFormat,
      Multimap<String, String> duplicateHostnames,
      @Nullable SpanContext spanContext) {
    super(settings);
    _fileText = fileText;
    _filename = filename;
    _ptSentences = new ParseTreeSentences();
    _warnings = warnings;
    _format = configurationFormat;
    _duplicateHostnames = duplicateHostnames;
    _spanContext = spanContext;
  }

  private static final Pattern WHITESPACE_ONLY = Pattern.compile("^\\s*$");

  /**
   * Detects and returns the {@link ConfigurationFormat format} of the given text using the given
   * default format.
   */
  @VisibleForTesting
  static ConfigurationFormat detectFormat(
      String fileText, Settings settings, ConfigurationFormat format) {
    try (ActiveSpan span = GlobalTracer.get().buildSpan("Detecting file format").startActive()) {
      assert span != null; // avoid unused warning

      if (WHITESPACE_ONLY.matcher(fileText).matches()) {
        return ConfigurationFormat.EMPTY;
      }

      if (settings.ignoreFilesWithStrings().stream().anyMatch(fileText::contains)) {
        return ConfigurationFormat.IGNORED;
      }

      if (format == ConfigurationFormat.UNKNOWN) {
        return VendorConfigurationFormatDetector.identifyConfigurationFormat(fileText);
      }

      return format;
    }
  }

  @SuppressWarnings("fallthrough")
  @Override
  public ParseVendorConfigurationResult call() throws Exception {
    long startTime = System.currentTimeMillis();
    _logger.infof("Processing: '%s'\n", _filename);

    try (ActiveSpan span =
        GlobalTracer.get()
            .buildSpan("ParseVendorConfigurationJob for " + _filename)
            .addReference(References.FOLLOWS_FROM, _spanContext)
            .startActive()) {
      assert span != null; // avoid unused warning

      ConfigurationFormat format = detectFormat(_fileText, _settings, _format);

      BatfishCombinedParser<?, ?> combinedParser = null;
      ControlPlaneExtractor extractor = null;
      FlattenerLineMap lineMap = null;
      try (ActiveSpan parseSpan = GlobalTracer.get().buildSpan("Creating parser").startActive()) {
        assert parseSpan != null; // avoid unused warning

        switch (format) {
          case EMPTY:
            _warnings.redFlag("Empty file: '" + _filename + "'\n");
            return new ParseVendorConfigurationResult(
                System.currentTimeMillis() - startTime,
                _logger.getHistory(),
                _filename,
                _warnings,
                ParseStatus.EMPTY);

          case IGNORED:
            _warnings.pedantic("Ignored file: '" + _filename + "'\n");
            return new ParseVendorConfigurationResult(
                System.currentTimeMillis() - startTime,
                _logger.getHistory(),
                _filename,
                _warnings,
                ParseStatus.IGNORED);

          case ARISTA:
          case ARUBAOS:
          case CADANT:
          case CISCO_ASA:
          case CISCO_IOS:
          case CISCO_IOS_XR:
          case CISCO_NX:
          case FORCE10:
          case FOUNDRY:
            String newFileText = _fileText;
            String fileText;
            _logger.info("\tPreprocessing...");
            do {
              fileText = newFileText;
              try {
                newFileText = preprocessBanner(fileText, format);
              } catch (BatfishException e) {
                return new ParseVendorConfigurationResult(
                    System.currentTimeMillis() - startTime,
                    _logger.getHistory(),
                    _filename,
                    new BatfishException("Error preprocessing banner", e));
              }
            } while (!newFileText.equals(fileText));
            _logger.info("OK\n");
            CiscoCombinedParser ciscoParser =
                new CiscoCombinedParser(newFileText, _settings, format);
            combinedParser = ciscoParser;
            extractor = new CiscoControlPlaneExtractor(newFileText, ciscoParser, format, _warnings);
            break;

          case HOST:
            VendorConfiguration vc;
            try {
              vc = HostConfiguration.fromJson(_filename, _fileText, _warnings);
            } catch (Exception e) {
              return new ParseVendorConfigurationResult(
                  System.currentTimeMillis() - startTime,
                  _logger.getHistory(),
                  _filename,
                  new BatfishException(
                      String.format(
                          "Failed to create host config from file: '%s', with error: %s",
                          _filename, e.getMessage()),
                      e));
            }

            return new ParseVendorConfigurationResult(
                System.currentTimeMillis() - startTime,
                _logger.getHistory(),
                _filename,
                vc,
                _warnings,
                _ptSentences,
                _duplicateHostnames);

          case VYOS:
            if (_settings.flattenOnTheFly()) {
              _warnings.pedantic(
                  String.format(
                      "Flattening: '%s' on-the-fly; line-numbers reported for this file will be spurious\n",
                      _filename));
              _fileText =
                  Batfish.flatten(
                          _fileText,
                          _logger,
                          _settings,
                          ConfigurationFormat.VYOS,
                          VendorConfigurationFormatDetector.BATFISH_FLATTENED_VYOS_HEADER)
                      .getFlattenedConfigurationText();
            } else {
              return new ParseVendorConfigurationResult(
                  System.currentTimeMillis() - startTime,
                  _logger.getHistory(),
                  _filename,
                  new BatfishException(
                      String.format(
                          "Vyos configurations must be flattened prior to this stage: '%s'",
                          _filename)));
            }
            // fall through
          case FLAT_VYOS:
            FlatVyosCombinedParser flatVyosParser =
                new FlatVyosCombinedParser(_fileText, _settings);
            combinedParser = flatVyosParser;
            extractor = new FlatVyosControlPlaneExtractor(_fileText, flatVyosParser, _warnings);
            break;

          case JUNIPER:
            if (_settings.flattenOnTheFly()) {
              _warnings.pedantic(
                  String.format(
                      "Flattening: '%s' on-the-fly; line-numbers reported for this file will be spurious\n",
                      _filename));
              try {
                Flattener flattener =
                    Batfish.flatten(
                        _fileText,
                        _logger,
                        _settings,
                        ConfigurationFormat.JUNIPER,
                        VendorConfigurationFormatDetector.BATFISH_FLATTENED_JUNIPER_HEADER);
                _fileText = flattener.getFlattenedConfigurationText();
                lineMap = flattener.getOriginalLineMap();
              } catch (BatfishException e) {
                return new ParseVendorConfigurationResult(
                    System.currentTimeMillis() - startTime,
                    _logger.getHistory(),
                    _filename,
                    new BatfishException(
                        String.format("Error flattening configuration file: '%s'", _filename), e));
              }
            } else {
              return new ParseVendorConfigurationResult(
                  System.currentTimeMillis() - startTime,
                  _logger.getHistory(),
                  _filename,
                  new BatfishException(
                      String.format(
                          "Juniper configurations must be flattened prior to this stage: '%s'",
                          _filename)));
            }
            // fall through
          case FLAT_JUNIPER:
            FlatJuniperCombinedParser flatJuniperParser =
                new FlatJuniperCombinedParser(_fileText, _settings, lineMap);
            combinedParser = flatJuniperParser;
            extractor =
                new FlatJuniperControlPlaneExtractor(_fileText, flatJuniperParser, _warnings);
            break;

          case IPTABLES:
            IptablesCombinedParser iptablesParser =
                new IptablesCombinedParser(_fileText, _settings);
            combinedParser = iptablesParser;
            extractor =
                new IptablesControlPlaneExtractor(_fileText, iptablesParser, _warnings, _filename);
            break;

          case MRV:
            MrvCombinedParser mrvParser = new MrvCombinedParser(_fileText, _settings);
            combinedParser = mrvParser;
            extractor = new MrvControlPlaneExtractor(_fileText, mrvParser, _warnings);
            break;

          case PALO_ALTO_NESTED:
            if (_settings.flattenOnTheFly()) {
              _warnings.pedantic(
                  String.format(
                      "Flattening: '%s' on-the-fly; line-numbers reported for this file will be spurious\n",
                      _filename));
              try {
                Flattener flattener =
                    Batfish.flatten(
                        _fileText,
                        _logger,
                        _settings,
                        ConfigurationFormat.PALO_ALTO_NESTED,
                        VendorConfigurationFormatDetector.BATFISH_FLATTENED_PALO_ALTO_HEADER);
                _fileText = flattener.getFlattenedConfigurationText();
                lineMap = flattener.getOriginalLineMap();
              } catch (BatfishException e) {
                return new ParseVendorConfigurationResult(
                    System.currentTimeMillis() - startTime,
                    _logger.getHistory(),
                    _filename,
                    new BatfishException(
                        String.format("Error flattening configuration file: '%s'", _filename), e));
              }
            } else {
              return new ParseVendorConfigurationResult(
                  System.currentTimeMillis() - startTime,
                  _logger.getHistory(),
                  _filename,
                  new BatfishException(
                      String.format(
                          "Palo Alto nested configurations must be flattened prior to this stage: '%s'",
                          _filename)));
            }
            // fall through
          case PALO_ALTO:
            PaloAltoCombinedParser paParser =
                new PaloAltoCombinedParser(_fileText, _settings, lineMap);
            combinedParser = paParser;
            extractor = new PaloAltoControlPlaneExtractor(_fileText, paParser, _warnings);
            break;

          case ALCATEL_AOS:
          case AWS:
          case BLADENETWORK:
          case F5:
          case JUNIPER_SWITCH:
          case METAMAKO:
          case MRV_COMMANDS:
          case MSS:
          case VXWORKS:
            String unsupportedError =
                "Unsupported configuration format: '"
                    + format
                    + "' for file: '"
                    + _filename
                    + "'\n";
            if (!_settings.ignoreUnsupported()) {
              return new ParseVendorConfigurationResult(
                  System.currentTimeMillis() - startTime,
                  _logger.getHistory(),
                  _filename,
                  new BatfishException(unsupportedError));
            } else {
              _warnings.unimplemented(unsupportedError);
              return new ParseVendorConfigurationResult(
                  System.currentTimeMillis() - startTime,
                  _logger.getHistory(),
                  _filename,
                  _warnings,
                  ParseStatus.UNSUPPORTED);
            }

          case UNKNOWN:
          default:
            String unknownError = "Unknown configuration format for file: '" + _filename + "'\n";
            if (!_settings.ignoreUnknown()) {
              return new ParseVendorConfigurationResult(
                  System.currentTimeMillis() - startTime,
                  _logger.getHistory(),
                  _filename,
                  new BatfishException(unknownError));
            } else {
              _warnings.unimplemented(unknownError);
              return new ParseVendorConfigurationResult(
                  System.currentTimeMillis() - startTime,
                  _logger.getHistory(),
                  _filename,
                  _warnings,
                  ParseStatus.UNKNOWN);
            }
        }
      } catch (Exception e) {
        return new ParseVendorConfigurationResult(
            System.currentTimeMillis() - startTime,
            _logger.getHistory(),
            _filename,
            new BatfishException(
                String.format(
                    "Pre-parsing failure for file '%s', with error: %s", _filename, e.getMessage()),
                e));
      }

      ParserRuleContext tree;
      try (ActiveSpan parseSpan = GlobalTracer.get().buildSpan("Parsing").startActive()) {
        assert parseSpan != null; // avoid unused warning
        _logger.info("\tParsing...");
        tree = Batfish.parse(combinedParser, _logger, _settings);
        if (_settings.getPrintParseTree()) {
          _ptSentences =
              ParseTreePrettyPrinter.getParseTreeSentences(
                  tree, combinedParser, _settings.getPrintParseTreeLineNums());
        }
      } catch (ParserBatfishException e) {
        String error = "Error parsing configuration file: '" + _filename + "'";
        return new ParseVendorConfigurationResult(
            System.currentTimeMillis() - startTime,
            _logger.getHistory(),
            _filename,
            new BatfishException(error, e));
      }

      try (ActiveSpan postProcessSpan =
          GlobalTracer.get().buildSpan("Post-processing").startActive()) {
        assert postProcessSpan != null; // avoid unused warning
        _logger.info("\tPost-processing...");
        extractor.processParseTree(tree);
        if (!combinedParser.getErrors().isEmpty()) {
          return new ParseVendorConfigurationResult(
              System.currentTimeMillis() - startTime,
              _logger.getHistory(),
              _filename,
              new BatfishException(
                  String.format(
                      "Configuration file: '%s' contains unrecognized lines:\n%s",
                      _filename, String.join("\n", combinedParser.getErrors()))));
        }
        _logger.info("OK\n");
      } catch (Exception e) {
        String error =
            "Error post-processing parse tree of configuration file: '" + _filename + "'";
        return new ParseVendorConfigurationResult(
            System.currentTimeMillis() - startTime,
            _logger.getHistory(),
            _filename,
            new BatfishException(error, e));
      } finally {
        Batfish.logWarnings(_logger, _warnings);
      }

      VendorConfiguration vc = extractor.getVendorConfiguration();
      vc.setVendor(format);
      vc.setFilename(_filename);
      if (vc.getHostname() == null) {
        _warnings.redFlag("No hostname set in file: '" + _filename.replace("\\", "/") + "'\n");
        String guessedHostname =
            Paths.get(_filename).getFileName().toString().replaceAll("\\.(cfg|conf)$", "");
        _logger.redflag(
            "\tNo hostname set! Guessing hostname from filename: '"
                + _filename
                + "' ==> '"
                + guessedHostname
                + "'\n");
        vc.setHostname(guessedHostname);
      }
      return new ParseVendorConfigurationResult(
          System.currentTimeMillis() - startTime,
          _logger.getHistory(),
          _filename,
          vc,
          _warnings,
          _ptSentences,
          _duplicateHostnames);
    }
  }
}
