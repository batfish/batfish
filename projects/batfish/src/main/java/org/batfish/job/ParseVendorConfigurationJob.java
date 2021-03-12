package org.batfish.job;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import io.opentracing.References;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.util.GlobalTracer;
import java.nio.file.Paths;
import java.util.Set;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.antlr.v4.runtime.ParserRuleContext;
import org.batfish.common.BatfishException;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.ParseTreeSentences;
import org.batfish.common.Warnings;
import org.batfish.common.WillNotCommitException;
import org.batfish.config.Settings;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.answers.ParseStatus;
import org.batfish.grammar.BatfishCombinedParser;
import org.batfish.grammar.BatfishParseException;
import org.batfish.grammar.ControlPlaneExtractor;
import org.batfish.grammar.ParseTreePrettyPrinter;
import org.batfish.grammar.VendorConfigurationFormatDetector;
import org.batfish.grammar.arista.AristaCombinedParser;
import org.batfish.grammar.arista.AristaControlPlaneExtractor;
import org.batfish.grammar.cisco.CiscoCombinedParser;
import org.batfish.grammar.cisco.CiscoControlPlaneExtractor;
import org.batfish.grammar.cisco_asa.AsaCombinedParser;
import org.batfish.grammar.cisco_asa.AsaControlPlaneExtractor;
import org.batfish.grammar.cisco_nxos.CiscoNxosCombinedParser;
import org.batfish.grammar.cisco_nxos.NxosControlPlaneExtractor;
import org.batfish.grammar.cisco_xr.CiscoXrCombinedParser;
import org.batfish.grammar.cisco_xr.CiscoXrControlPlaneExtractor;
import org.batfish.grammar.cumulus_concatenated.CumulusConcatenatedCombinedParser;
import org.batfish.grammar.cumulus_concatenated.CumulusConcatenatedControlPlaneExtractor;
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
import org.batfish.grammar.fortios.FortiosCombinedParser;
import org.batfish.grammar.fortios.FortiosControlPlaneExtractor;
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

  private static final Set<ConfigurationFormat> UNIMPLEMENTED_FORMATS =
      ImmutableSet.of(
          ConfigurationFormat.ALCATEL_AOS,
          ConfigurationFormat.BLADENETWORK,
          ConfigurationFormat.F5,
          ConfigurationFormat.IBM_BNT,
          ConfigurationFormat.JUNIPER_SWITCH,
          ConfigurationFormat.METAMAKO,
          ConfigurationFormat.MRV_COMMANDS,
          ConfigurationFormat.MSS,
          ConfigurationFormat.RUCKUS_ICX,
          ConfigurationFormat.VXWORKS);

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
  final NetworkSnapshot _snapshot;
  @Nullable private SpanContext _spanContext;
  private Warnings _warnings;

  public ParseVendorConfigurationJob(
      Settings settings,
      NetworkSnapshot snapshot,
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
    _snapshot = snapshot;
  }

  private static final Pattern WHITESPACE_ONLY = Pattern.compile("^\\s*$");

  /**
   * Detects and returns the {@link ConfigurationFormat format} of the given text using the given
   * default format.
   */
  @VisibleForTesting
  static ConfigurationFormat detectFormat(
      String fileText, Settings settings, ConfigurationFormat format) {
    Span span = GlobalTracer.get().buildSpan("Detecting file format").start();
    try (Scope scope = GlobalTracer.get().scopeManager().activate(span)) {
      assert scope != null; // avoid unused warning

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
    } finally {
      span.finish();
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
    Span parseSpan = GlobalTracer.get().buildSpan("Creating parser").start();
    try (Scope scope = GlobalTracer.get().scopeManager().activate(parseSpan)) {
      assert scope != null; // avoid unused warning

      switch (format) {
        case ARISTA:
          {
            AristaCombinedParser aristaParser = new AristaCombinedParser(_fileText, _settings);
            combinedParser = aristaParser;
            extractor = new AristaControlPlaneExtractor(_fileText, aristaParser, format, _warnings);
            break;
          }

        case ARUBAOS:
        case CADANT:
        case CISCO_IOS:
        case FORCE10:
        case FOUNDRY:
          {
            CiscoCombinedParser ciscoParser = new CiscoCombinedParser(_fileText, _settings, format);
            combinedParser = ciscoParser;
            extractor = new CiscoControlPlaneExtractor(_fileText, ciscoParser, format, _warnings);
            break;
          }
        case CISCO_ASA:
          {
            AsaCombinedParser asaParser = new AsaCombinedParser(_fileText, _settings);
            combinedParser = asaParser;
            extractor = new AsaControlPlaneExtractor(_fileText, asaParser, _warnings);
            break;
          }

        case CISCO_NX:
          {
            CiscoNxosCombinedParser ciscoNxosParser =
                new CiscoNxosCombinedParser(_fileText, _settings);
            combinedParser = ciscoNxosParser;
            extractor = new NxosControlPlaneExtractor(_fileText, ciscoNxosParser, _warnings);
            break;
          }

        case CISCO_IOS_XR:
          {
            CiscoXrCombinedParser ciscoXrParser = new CiscoXrCombinedParser(_fileText, _settings);
            combinedParser = ciscoXrParser;
            extractor =
                new CiscoXrControlPlaneExtractor(_fileText, ciscoXrParser, format, _warnings);
            break;
          }
        case CUMULUS_CONCATENATED:
          {
            CumulusConcatenatedCombinedParser parser =
                new CumulusConcatenatedCombinedParser(_fileText, _settings);
            combinedParser = parser;
            extractor =
                new CumulusConcatenatedControlPlaneExtractor(
                    _fileText,
                    _warnings,
                    _filename,
                    parser.getSettings(),
                    _settings.getPrintParseTree() ? () -> _ptSentences : null,
                    _settings.getPrintParseTreeLineNums());
            break;
          }

        case CUMULUS_NCLU:
          {
            CumulusNcluCombinedParser parser = new CumulusNcluCombinedParser(_fileText, _settings);
            combinedParser = parser;
            extractor = new CumulusNcluControlPlaneExtractor(_fileText, parser, _warnings);
            break;
          }

        case F5_BIGIP_STRUCTURED:
          {
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
          }

        case FORTIOS:
          {
            FortiosCombinedParser parser = new FortiosCombinedParser(_fileText, _settings);
            combinedParser = parser;
            extractor = new FortiosControlPlaneExtractor(_fileText, parser, _warnings);
            break;
          }

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
                  "Flattening: '%s' on-the-fly; line-numbers reported for this file will be"
                      + " spurious\n",
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
    } finally {
      parseSpan.finish();
    }

    ParserRuleContext tree;
    Span parsingSpan = GlobalTracer.get().buildSpan("Parsing").start();
    try (Scope scope = GlobalTracer.get().scopeManager().activate(parsingSpan)) {
      assert scope != null; // avoid unused warning
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
    } finally {
      parsingSpan.finish();
    }

    Span postProcessSpan = GlobalTracer.get().buildSpan("Post-processing").start();
    try (Scope scope = GlobalTracer.get().scopeManager().activate(postProcessSpan)) {
      assert scope != null; // avoid unused warning
      _logger.info("\tPost-processing...");

      try {
        extractor.processParseTree(_snapshot, tree);
      } catch (BatfishParseException e) {
        _warnings.setErrorDetails(e.getErrorDetails());
        throw new BatfishException("Error processing parse tree", e);
      }

      _logger.info("OK\n");
    } finally {
      Batfish.logWarnings(_logger, _warnings);
      postProcessSpan.finish();
    }

    VendorConfiguration vc = extractor.getVendorConfiguration();
    vc.setVendor(format);
    vc.setFilename(_filename);
    if (Strings.isNullOrEmpty(vc.getHostname())) {
      _warnings.redFlag("No hostname set in file: '" + _filename.replace("\\", "/") + "'\n");
      String guessedHostname =
          Paths.get(_filename)
              .getFileName()
              .toString()
              .toLowerCase()
              .replaceAll("\\.(cfg|conf)$", "");
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
      return new ParseResult(
          null, null, _filename, format, _ptSentences, ParseStatus.EMPTY, _warnings);
    } else if (format == ConfigurationFormat.IGNORED) {
      _warnings.redFlag("Ignored file: " + _filename + "\n");
      return new ParseResult(
          null, null, _filename, format, _ptSentences, ParseStatus.IGNORED, _warnings);
    } else if (format == ConfigurationFormat.UNKNOWN) {
      _warnings.redFlag("Unable to detect format for file: '" + _filename + "'\n");
      return new ParseResult(
          null, null, _filename, format, _ptSentences, ParseStatus.UNKNOWN, _warnings);
    } else if (UNIMPLEMENTED_FORMATS.contains(format)) {
      String unsupportedError =
          "Unsupported configuration format: '" + format + "' for file: '" + _filename + "'\n";
      if (!_settings.ignoreUnsupported()) {
        return new ParseResult(
            null,
            new BatfishException(unsupportedError),
            _filename,
            format,
            _ptSentences,
            ParseStatus.FAILED,
            _warnings);
      }
      _warnings.redFlag(unsupportedError);
      return new ParseResult(
          null, null, _filename, format, _ptSentences, ParseStatus.UNSUPPORTED, _warnings);
    }

    try {
      // Actually parse the file.
      VendorConfiguration vc = parseFile(format);
      ParseStatus status =
          vc.getUnrecognized() ? ParseStatus.PARTIALLY_UNRECOGNIZED : ParseStatus.PASSED;
      return new ParseResult(vc, null, _filename, format, _ptSentences, status, _warnings);
    } catch (WillNotCommitException e) {
      if (_settings.getHaltOnParseError()) {
        // Fail the job if we need to
        return new ParseResult(
            null, e, _filename, format, _ptSentences, ParseStatus.WILL_NOT_COMMIT, _warnings);
      }
      // Otherwise just generate a warning
      _warnings.redFlag(e.getMessage());
      return new ParseResult(
          null, null, _filename, format, _ptSentences, ParseStatus.WILL_NOT_COMMIT, _warnings);
    } catch (Exception e) {
      return new ParseResult(
          null,
          new BatfishException("Error parsing configuration file: '" + _filename + "'", e),
          _filename,
          format,
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
          result.getFormat(),
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
          result.getFormat(),
          result.getWarnings(),
          result.getParseTreeSentences(),
          result.getFailureCause());
    } else {
      return new ParseVendorConfigurationResult(
          elapsed,
          _logger.getHistory(),
          _filename,
          result.getFormat(),
          result.getWarnings(),
          result.getStatus());
    }
  }

  @Override
  public ParseVendorConfigurationResult call() {
    Span span =
        GlobalTracer.get()
            .buildSpan("ParseVendorConfigurationJob for " + _filename)
            .addReference(References.FOLLOWS_FROM, _spanContext)
            .start();
    try (Scope scope = GlobalTracer.get().scopeManager().activate(span)) {
      assert scope != null; // avoid unused warning

      _logger.infof("Processing: '%s'\n", _filename);
      long startTime = System.currentTimeMillis();
      ParseResult result = parse();
      return fromResult(result, System.currentTimeMillis() - startTime);
    } finally {
      span.finish();
    }
  }

  public String getFilename() {
    return _filename;
  }

  public String getFileText() {
    return _fileText;
  }
}
