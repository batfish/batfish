package org.batfish.job;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import io.opentracing.ActiveSpan;
import io.opentracing.References;
import io.opentracing.SpanContext;
import io.opentracing.util.GlobalTracer;
import java.nio.file.Paths;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.antlr.v4.runtime.ParserRuleContext;
import org.batfish.common.BatfishException;
import org.batfish.common.ParseTreeSentences;
import org.batfish.common.Warnings;
import org.batfish.config.Settings;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.answers.ParseStatus;
import org.batfish.grammar.BatfishCombinedParser;
import org.batfish.grammar.BatfishParseException;
import org.batfish.grammar.ControlPlaneExtractor;
import org.batfish.grammar.ParseTreePrettyPrinter;
import org.batfish.grammar.VendorConfigurationFormatDetector;
import org.batfish.grammar.cisco.CiscoCombinedParser;
import org.batfish.grammar.cisco.CiscoControlPlaneExtractor;
import org.batfish.grammar.cumulus_nclu.CumulusNcluCombinedParser;
import org.batfish.grammar.cumulus_nclu.CumulusNcluControlPlaneExtractor;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredCombinedParser;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredControlPlaneExtractor;
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
import org.batfish.representation.host.HostConfiguration;
import org.batfish.vendor.VendorConfiguration;

public class ParseVendorConfigurationJob extends BatfishJob<ParseVendorConfigurationResult> {

  private static final Pattern BANNER_PATTERN =
      Pattern.compile("(?m)banner[ \t][ \t]*[^ \r\n\t][^ \r\n\t]*[ \t][ \t]*([^ \r\n\t])[ \r\n]");

  private static final Set<ConfigurationFormat> UNIMPLEMENTED_FORMATS =
      ImmutableSet.of(
          ConfigurationFormat.ALCATEL_AOS,
          ConfigurationFormat.BLADENETWORK,
          ConfigurationFormat.F5,
          ConfigurationFormat.JUNIPER_SWITCH,
          ConfigurationFormat.METAMAKO,
          ConfigurationFormat.MRV_COMMANDS,
          ConfigurationFormat.MSS,
          ConfigurationFormat.VXWORKS);

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

  /**
   * What type of files are expected, or {@link ConfigurationFormat#UNKNOWN} to detect dynamically.
   */
  private ConfigurationFormat _expectedFormat;

  private ParseTreeSentences _ptSentences;

  @Nullable private SpanContext _spanContext;

  private Warnings _warnings;

  public ParseVendorConfigurationJob(
      Settings settings,
      String fileText,
      String filename,
      Warnings warnings,
      ConfigurationFormat expectedFormat,
      Multimap<String, String> duplicateHostnames,
      @Nullable SpanContext spanContext) {
    super(settings);
    _fileText = fileText;
    _filename = filename;
    _ptSentences = new ParseTreeSentences();
    _warnings = warnings;
    _expectedFormat = expectedFormat;
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

  /**
   * Returns the {@link VendorConfiguration vendor configuration} corresponding to the current file
   * with the given format.
   *
   * <p>Expects that empty, ignored, unsupported, or format-unknown files have been removed ahead of
   * time.
   *
   * @throws BatfishException if the given file is for an unsupported or unhandled format
   */
  @SuppressWarnings("fallthrough")
  private VendorConfiguration parseFile(ConfigurationFormat format) {
    BatfishCombinedParser<?, ?> combinedParser = null;
    ControlPlaneExtractor extractor = null;
    FlattenerLineMap lineMap = null;
    try (ActiveSpan parseSpan = GlobalTracer.get().buildSpan("Creating parser").startActive()) {
      assert parseSpan != null; // avoid unused warning

      switch (format) {
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
              throw new BatfishException("Error preprocessing banner", e);
            }
          } while (!newFileText.equals(fileText));
          _logger.info("OK\n");
          CiscoCombinedParser ciscoParser = new CiscoCombinedParser(newFileText, _settings, format);
          combinedParser = ciscoParser;
          extractor = new CiscoControlPlaneExtractor(newFileText, ciscoParser, format, _warnings);
          break;

        case CUMULUS_NCLU:
          {
            CumulusNcluCombinedParser parser = new CumulusNcluCombinedParser(_fileText, _settings);
            combinedParser = parser;
            extractor = new CumulusNcluControlPlaneExtractor(_fileText, parser, _warnings);
            break;
          }

        case F5_BIGIP_STRUCTURED:
          F5BigipStructuredCombinedParser parser =
              new F5BigipStructuredCombinedParser(_fileText, _settings);
          combinedParser = parser;
          extractor =
              new F5BigipStructuredControlPlaneExtractor(
                  _fileText,
                  parser,
                  _warnings,
                  _filename,
                  _settings.getPrintParseTree() ? () -> _ptSentences : null,
                  _settings.getPrintParseTreeLineNums());
          break;

        case HOST:
          try {
            return HostConfiguration.fromJson(_filename, _fileText, _warnings);
          } catch (Exception e) {
            throw new BatfishException(
                String.format(
                    "Failed to create host config from file: '%s', with error: %s",
                    _filename, e.getMessage()),
                e);
          }

        case VYOS:
          _warnings.pedantic(
              String.format(
                  "Flattening: '%s' on-the-fly; line-numbers reported for this file will be spurious\n",
                  _filename));
          _fileText =
              Batfish.flatten(
                      _fileText,
                      _logger,
                      _settings,
                      _warnings,
                      ConfigurationFormat.VYOS,
                      VendorConfigurationFormatDetector.BATFISH_FLATTENED_VYOS_HEADER)
                  .getFlattenedConfigurationText();
          // fall through
        case FLAT_VYOS:
          FlatVyosCombinedParser flatVyosParser = new FlatVyosCombinedParser(_fileText, _settings);
          combinedParser = flatVyosParser;
          extractor = new FlatVyosControlPlaneExtractor(_fileText, flatVyosParser, _warnings);
          break;

        case JUNIPER:
          try {
            Flattener flattener =
                Batfish.flatten(
                    _fileText,
                    _logger,
                    _settings,
                    _warnings,
                    ConfigurationFormat.JUNIPER,
                    VendorConfigurationFormatDetector.BATFISH_FLATTENED_JUNIPER_HEADER);
            _fileText = flattener.getFlattenedConfigurationText();
            lineMap = flattener.getOriginalLineMap();
          } catch (BatfishException e) {
            throw new BatfishException(
                String.format("Error flattening configuration file: '%s'", _filename), e);
          }
          // fall through
        case FLAT_JUNIPER:
          FlatJuniperCombinedParser flatJuniperParser =
              new FlatJuniperCombinedParser(_fileText, _settings, lineMap);
          combinedParser = flatJuniperParser;
          extractor = new FlatJuniperControlPlaneExtractor(_fileText, flatJuniperParser, _warnings);
          break;

        case IPTABLES:
          IptablesCombinedParser iptablesParser = new IptablesCombinedParser(_fileText, _settings);
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
          try {
            Flattener flattener =
                Batfish.flatten(
                    _fileText,
                    _logger,
                    _settings,
                    _warnings,
                    ConfigurationFormat.PALO_ALTO_NESTED,
                    VendorConfigurationFormatDetector.BATFISH_FLATTENED_PALO_ALTO_HEADER);
            _fileText = flattener.getFlattenedConfigurationText();
            lineMap = flattener.getOriginalLineMap();
          } catch (BatfishException e) {
            throw new BatfishException(
                String.format("Error flattening configuration file: '%s'", _filename), e);
          }
          // fall through
        case PALO_ALTO:
          PaloAltoCombinedParser paParser =
              new PaloAltoCombinedParser(_fileText, _settings, lineMap);
          combinedParser = paParser;
          extractor = new PaloAltoControlPlaneExtractor(_fileText, paParser, _warnings);
          break;

        default:
          throw new BatfishException(
              String.format("File format %s is neither unsupported nor handled", format));
      }
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
      if (!combinedParser.getErrors().isEmpty()) {
        throw new BatfishException(
            String.format(
                "Configuration file: '%s' contains unrecognized lines:\n%s",
                _filename, String.join("\n", combinedParser.getErrors())));
      }
    }

    try (ActiveSpan postProcessSpan =
        GlobalTracer.get().buildSpan("Post-processing").startActive()) {
      assert postProcessSpan != null; // avoid unused warning
      _logger.info("\tPost-processing...");

      try {
        extractor.processParseTree(tree);
      } catch (BatfishParseException e) {
        _warnings.setErrorDetails(e.getErrorDetails());
        throw new BatfishException("Error processing parse tree", e);
      }

      _logger.info("OK\n");
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
    return vc;
  }

  /**
   * Parses the given file and returns a {@link ParseResult} for this job.
   *
   * <p>The returned {@link ParseResult} will always have a valid {@link ParseResult#getStatus()}.
   * It may also contain a {@link ParseResult#getConfig() parsed vendor-specific configuration} or a
   * {@link ParseResult#getFailureCause() failure cause}.
   */
  @Nonnull
  public ParseResult parse() {
    ConfigurationFormat format = detectFormat(_fileText, _settings, _expectedFormat);

    // Handle specially some cases that will not produce a vendor configuration file.
    if (format == ConfigurationFormat.EMPTY) {
      _warnings.redFlag("Empty file: '" + _filename + "'\n");
      return new ParseResult(null, null, _filename, _ptSentences, ParseStatus.EMPTY, _warnings);
    } else if (format == ConfigurationFormat.IGNORED) {
      _warnings.redFlag("Ignored file: " + _filename + "\n");
      return new ParseResult(null, null, _filename, _ptSentences, ParseStatus.IGNORED, _warnings);
    } else if (format == ConfigurationFormat.UNKNOWN) {
      _warnings.redFlag("Unable to detect format for file: '" + _filename + "'\n");
      return new ParseResult(null, null, _filename, _ptSentences, ParseStatus.UNKNOWN, _warnings);
    } else if (UNIMPLEMENTED_FORMATS.contains(format)) {
      String unsupportedError =
          "Unsupported configuration format: '" + format + "' for file: '" + _filename + "'\n";
      if (!_settings.ignoreUnsupported()) {
        return new ParseResult(
            null,
            new BatfishException(unsupportedError),
            _filename,
            _ptSentences,
            ParseStatus.FAILED,
            _warnings);
      }
      _warnings.redFlag(unsupportedError);
      return new ParseResult(
          null, null, _filename, _ptSentences, ParseStatus.UNSUPPORTED, _warnings);
    }

    try {
      // Actually parse the file.
      VendorConfiguration vc = parseFile(format);
      ParseStatus status =
          vc.getUnrecognized() ? ParseStatus.PARTIALLY_UNRECOGNIZED : ParseStatus.PASSED;
      return new ParseResult(vc, null, _filename, _ptSentences, status, _warnings);
    } catch (Exception e) {
      return new ParseResult(
          null,
          new BatfishException("Error parsing configuration file: '" + _filename + "'", e),
          _filename,
          _ptSentences,
          ParseStatus.FAILED,
          _warnings);
    }
  }

  public ParseVendorConfigurationResult fromResult(ParseResult result, long elapsed) {
    if (result.getConfig() != null) {
      return new ParseVendorConfigurationResult(
          elapsed,
          _logger.getHistory(),
          _filename,
          result.getConfig(),
          result.getWarnings(),
          result.getParseTreeSentences(),
          result.getStatus(),
          _duplicateHostnames);
    } else if (result.getFailureCause() != null) {
      return new ParseVendorConfigurationResult(
          elapsed,
          _logger.getHistory(),
          _filename,
          result.getWarnings(),
          result.getParseTreeSentences(),
          result.getFailureCause());
    } else {
      return new ParseVendorConfigurationResult(
          elapsed, _logger.getHistory(), _filename, result.getWarnings(), result.getStatus());
    }
  }

  @Override
  public ParseVendorConfigurationResult call() {
    try (ActiveSpan span =
        GlobalTracer.get()
            .buildSpan("ParseVendorConfigurationJob for " + _filename)
            .addReference(References.FOLLOWS_FROM, _spanContext)
            .startActive()) {
      assert span != null; // avoid unused warning

      _logger.infof("Processing: '%s'\n", _filename);
      long startTime = System.currentTimeMillis();
      ParseResult result = parse();
      return fromResult(result, System.currentTimeMillis() - startTime);
    }
  }

  public String getFilename() {
    return _filename;
  }

  public String getFileText() {
    return _fileText;
  }
}
