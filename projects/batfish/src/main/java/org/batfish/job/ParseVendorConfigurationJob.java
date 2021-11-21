package org.batfish.job;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import io.opentracing.References;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.util.GlobalTracer;
import java.io.Serializable;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.antlr.v4.runtime.ParserRuleContext;
import org.batfish.common.BatfishException;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.ParseTreeSentences;
import org.batfish.common.Warnings;
import org.batfish.common.WillNotCommitException;
import org.batfish.config.Settings;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Names;
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
import org.batfish.grammar.silent_syntax.SilentSyntaxCollection;
import org.batfish.main.Batfish;
import org.batfish.representation.host.HostConfiguration;
import org.batfish.vendor.VendorConfiguration;
import org.batfish.vendor.a10.grammar.A10CombinedParser;
import org.batfish.vendor.a10.grammar.A10ControlPlaneExtractor;
import org.batfish.vendor.check_point_gateway.grammar.CheckPointGatewayCombinedParser;
import org.batfish.vendor.check_point_gateway.grammar.CheckPointGatewayControlPlaneExtractor;

@ParametersAreNonnullByDefault
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

  /**
   * Represents results of parsing one (of possibly multiple) file that is part of this Job. The
   * warnings object here will have file-specific warnings. Job-level warnings, not specific to a
   * file, go in {@link ParseVendorConfigurationJob#_warnings}.
   */
  @ParametersAreNonnullByDefault
  public static class FileResult implements Serializable {
    @Nonnull private ParseTreeSentences _parseTreeSentences;
    @Nonnull private final SilentSyntaxCollection _silentSyntax;
    @Nonnull private final Warnings _warnings;

    public FileResult(
        ParseTreeSentences parseTreeSentences,
        SilentSyntaxCollection silentSyntax,
        Warnings warnings) {
      _parseTreeSentences = parseTreeSentences;
      _silentSyntax = silentSyntax;
      _warnings = warnings;
    }

    @Nonnull
    public ParseTreeSentences getParseTreeSentences() {
      return _parseTreeSentences;
    }

    @Nonnull
    public SilentSyntaxCollection getSilentSyntax() {
      return _silentSyntax;
    }

    @Nonnull
    public Warnings getWarnings() {
      return _warnings;
    }
  }

  /** Information about duplicate hostnames is collected here */
  private final Multimap<String, String> _duplicateHostnames;

  private final @Nonnull Map<String, String> _fileTexts;

  /**
   * What type of files are expected, or {@link ConfigurationFormat#UNKNOWN} to detect dynamically.
   */
  private final ConfigurationFormat _expectedFormat;

  /**
   * Map from fileName (relative to the snapshot base) to its {@link FileResult} object, which is
   * populated as part of parsing.
   */
  private @Nonnull final Map<String, FileResult> _fileResults;

  final NetworkSnapshot _snapshot;
  @Nullable private final SpanContext _spanContext;

  /** Job-level (non-file-specific) warnings */
  private @Nonnull final Warnings _warnings;

  public ParseVendorConfigurationJob(
      Settings settings,
      NetworkSnapshot snapshot,
      Map<String, String> fileTexts,
      Warnings.Settings logSettings,
      ConfigurationFormat expectedFormat,
      Multimap<String, String> duplicateHostnames,
      @Nullable SpanContext spanContext) {
    super(settings);
    checkArgument(!fileTexts.isEmpty(), "Set of file texts cannot be empty");
    _fileTexts = ImmutableMap.copyOf(fileTexts);
    _fileResults =
        _fileTexts.keySet().stream()
            .collect(
                ImmutableMap.toImmutableMap(
                    Function.identity(),
                    f ->
                        new FileResult(
                            new ParseTreeSentences(),
                            new SilentSyntaxCollection(),
                            new Warnings(logSettings))));
    _expectedFormat = expectedFormat;
    _duplicateHostnames = duplicateHostnames;
    _spanContext = spanContext;
    _snapshot = snapshot;
    _warnings = new Warnings(logSettings);
  }

  private static final Pattern WHITESPACE_ONLY = Pattern.compile("^\\s*$");

  /**
   * Detects and returns the {@link ConfigurationFormat format} of the given text using the given
   * default format.
   */
  @VisibleForTesting
  static ConfigurationFormat detectFormat(
      Map<String, String> fileTexts, Settings settings, ConfigurationFormat format) {
    Span span = GlobalTracer.get().buildSpan("Detecting file format").start();
    try (Scope scope = GlobalTracer.get().scopeManager().activate(span)) {
      assert scope != null; // avoid unused warning

      String fileText = String.join("\n", fileTexts.values());

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
  private VendorConfiguration parseFiles(ConfigurationFormat format) {
    ControlPlaneExtractor extractor = null;
    FlattenerLineMap lineMap = null;

    Span parseSpan = GlobalTracer.get().buildSpan("Creating parser").start();
    try (Scope scope = GlobalTracer.get().scopeManager().activate(parseSpan)) {
      assert scope != null; // avoid unused warning

      // pull out the common case
      String filename = getRepresentativeFilename(_fileTexts, format);
      String fileText = _fileTexts.get(filename);

      switch (format) {
        case A10_ACOS:
          {
            A10CombinedParser a10Parser = new A10CombinedParser(fileText, _settings);
            extractor =
                new A10ControlPlaneExtractor(
                    fileText,
                    a10Parser,
                    _fileResults.get(filename)._warnings,
                    _fileResults.get(filename)._silentSyntax);
            parseFile(filename, a10Parser, extractor);
            break;
          }

        case ARISTA:
          {
            AristaCombinedParser aristaParser = new AristaCombinedParser(fileText, _settings);
            extractor =
                new AristaControlPlaneExtractor(
                    fileText,
                    aristaParser,
                    format,
                    _fileResults.get(filename)._warnings,
                    _fileResults.get(filename)._silentSyntax);
            parseFile(filename, aristaParser, extractor);
            break;
          }

        case ARUBAOS:
        case CADANT:
        case CISCO_IOS:
        case FORCE10:
        case FOUNDRY:
          {
            CiscoCombinedParser ciscoParser = new CiscoCombinedParser(fileText, _settings);
            extractor =
                new CiscoControlPlaneExtractor(
                    fileText,
                    ciscoParser,
                    format,
                    _fileResults.get(filename)._warnings,
                    _fileResults.get(filename)._silentSyntax);
            parseFile(filename, ciscoParser, extractor);
            break;
          }
        case CISCO_ASA:
          {
            AsaCombinedParser asaParser = new AsaCombinedParser(fileText, _settings);
            extractor =
                new AsaControlPlaneExtractor(
                    fileText,
                    asaParser,
                    _fileResults.get(filename)._warnings,
                    _fileResults.get(filename)._silentSyntax);
            parseFile(filename, asaParser, extractor);
            break;
          }

        case CISCO_NX:
          {
            CiscoNxosCombinedParser ciscoNxosParser =
                new CiscoNxosCombinedParser(fileText, _settings);
            extractor =
                new NxosControlPlaneExtractor(
                    fileText,
                    ciscoNxosParser,
                    _fileResults.get(filename)._warnings,
                    _fileResults.get(filename)._silentSyntax);
            parseFile(filename, ciscoNxosParser, extractor);
            break;
          }

        case CISCO_IOS_XR:
          {
            CiscoXrCombinedParser ciscoXrParser = new CiscoXrCombinedParser(fileText, _settings);
            extractor =
                new CiscoXrControlPlaneExtractor(
                    fileText,
                    ciscoXrParser,
                    format,
                    _fileResults.get(filename)._warnings,
                    _fileResults.get(filename)._silentSyntax);
            parseFile(filename, ciscoXrParser, extractor);
            break;
          }

        case CHECK_POINT_GATEWAY:
          {
            CheckPointGatewayCombinedParser checkPointParser =
                new CheckPointGatewayCombinedParser(fileText, _settings);
            extractor =
                new CheckPointGatewayControlPlaneExtractor(
                    fileText,
                    checkPointParser,
                    _fileResults.get(filename)._warnings,
                    _fileResults.get(filename)._silentSyntax);
            parseFile(filename, checkPointParser, extractor);
            break;
          }

        case CUMULUS_CONCATENATED:
          {
            CumulusConcatenatedCombinedParser parser =
                new CumulusConcatenatedCombinedParser(fileText, _settings);
            extractor =
                new CumulusConcatenatedControlPlaneExtractor(
                    fileText,
                    _fileResults.get(filename)._warnings,
                    filename,
                    parser.getSettings(),
                    _settings.getPrintParseTree()
                        ? () -> _fileResults.get(filename)._parseTreeSentences
                        : null,
                    _settings.getPrintParseTreeLineNums(),
                    _fileResults.get(filename)._silentSyntax);
            parseFile(filename, parser, extractor);
            break;
          }

        case CUMULUS_NCLU:
          {
            CumulusNcluCombinedParser parser = new CumulusNcluCombinedParser(fileText, _settings);
            extractor =
                new CumulusNcluControlPlaneExtractor(
                    fileText,
                    parser,
                    _fileResults.get(filename)._warnings,
                    _fileResults.get(filename)._silentSyntax);
            parseFile(filename, parser, extractor);
            break;
          }

        case F5_BIGIP_STRUCTURED:
          {
            F5BigipStructuredCombinedParser parser =
                new F5BigipStructuredCombinedParser(fileText, _settings);
            extractor =
                new F5BigipStructuredControlPlaneExtractor(
                    fileText,
                    parser,
                    _fileResults.get(filename)._warnings,
                    filename,
                    _settings.getPrintParseTree()
                        ? () -> _fileResults.get(filename)._parseTreeSentences
                        : null,
                    _settings.getPrintParseTreeLineNums(),
                    _fileResults.get(filename)._silentSyntax);
            parseFile(filename, parser, extractor);
            break;
          }

        case FORTIOS:
          {
            FortiosCombinedParser parser = new FortiosCombinedParser(fileText, _settings);
            extractor =
                new FortiosControlPlaneExtractor(
                    fileText,
                    parser,
                    _fileResults.get(filename)._warnings,
                    _fileResults.get(filename)._silentSyntax);
            parseFile(filename, parser, extractor);
            break;
          }

        case HOST:
          try {
            return HostConfiguration.fromJson(
                filename, fileText, _fileResults.get(filename)._warnings);
          } catch (Exception e) {
            throw new BatfishException(
                String.format(
                    "Failed to create host config from file: '%s', with error: %s",
                    filename, e.getMessage()),
                e);
          }

//        case SONIC:
//          {
//            SonicCombinedParser parser = new SonicCombinedParser(_fileText, _settings);
//            combinedParser = parser;
//            extractor =
//                new SonicControlPlaneExtractor(
//                    _fileText,
//                    _warnings,
//                    _filename,
//                    _parsingContext,
//                    parser.getSettings(),
//                    _settings.getPrintParseTree() ? () -> _ptSentences : null,
//                    _settings.getPrintParseTreeLineNums(),
//                    _silentSyntax);
//            break;
//          }

        case VYOS:
          _fileResults
              .get(filename)
              ._warnings
              .pedantic(
                  String.format(
                      "Flattening: '%s' on-the-fly; line-numbers reported for this file will be"
                          + " spurious\n",
                      filename));
          fileText =
              Batfish.flatten(
                      fileText,
                      _logger,
                      _settings,
                      _fileResults.get(filename)._warnings,
                      ConfigurationFormat.VYOS,
                      VendorConfigurationFormatDetector.BATFISH_FLATTENED_VYOS_HEADER)
                  .getFlattenedConfigurationText();
          // fall through
        case FLAT_VYOS:
          FlatVyosCombinedParser flatVyosParser = new FlatVyosCombinedParser(fileText, _settings);
          extractor =
              new FlatVyosControlPlaneExtractor(
                  fileText,
                  flatVyosParser,
                  _fileResults.get(filename)._warnings,
                  _fileResults.get(filename)._silentSyntax);
          parseFile(filename, flatVyosParser, extractor);
          break;

        case JUNIPER:
          try {
            Flattener flattener =
                Batfish.flatten(
                    fileText,
                    _logger,
                    _settings,
                    _fileResults.get(filename)._warnings,
                    ConfigurationFormat.JUNIPER,
                    VendorConfigurationFormatDetector.BATFISH_FLATTENED_JUNIPER_HEADER);
            fileText = flattener.getFlattenedConfigurationText();
            lineMap = flattener.getOriginalLineMap();
          } catch (BatfishException e) {
            throw new BatfishException(
                String.format("Error flattening configuration file: '%s'", filename), e);
          }
          // fall through
        case FLAT_JUNIPER:
          FlatJuniperCombinedParser flatJuniperParser =
              new FlatJuniperCombinedParser(fileText, _settings, lineMap);
          extractor =
              new FlatJuniperControlPlaneExtractor(
                  fileText,
                  flatJuniperParser,
                  _fileResults.get(filename)._warnings,
                  _fileResults.get(filename)._silentSyntax);
          parseFile(filename, flatJuniperParser, extractor);
          break;

        case IPTABLES:
          IptablesCombinedParser iptablesParser = new IptablesCombinedParser(fileText, _settings);
          extractor =
              new IptablesControlPlaneExtractor(
                  fileText,
                  iptablesParser,
                  _fileResults.get(filename)._warnings,
                  filename,
                  _fileResults.get(filename)._silentSyntax);
          parseFile(filename, iptablesParser, extractor);
          break;

        case MRV:
          MrvCombinedParser mrvParser = new MrvCombinedParser(fileText, _settings);
          extractor =
              new MrvControlPlaneExtractor(
                  fileText,
                  mrvParser,
                  _fileResults.get(filename)._warnings,
                  _fileResults.get(filename)._silentSyntax);
          parseFile(filename, mrvParser, extractor);
          break;

        case PALO_ALTO_NESTED:
          try {
            Flattener flattener =
                Batfish.flatten(
                    fileText,
                    _logger,
                    _settings,
                    _fileResults.get(filename)._warnings,
                    ConfigurationFormat.PALO_ALTO_NESTED,
                    VendorConfigurationFormatDetector.BATFISH_FLATTENED_PALO_ALTO_HEADER);
            fileText = flattener.getFlattenedConfigurationText();
            lineMap = flattener.getOriginalLineMap();
          } catch (BatfishException e) {
            throw new BatfishException(
                String.format("Error flattening configuration file: '%s'", filename), e);
          }
          // fall through
        case PALO_ALTO:
          PaloAltoCombinedParser paParser =
              new PaloAltoCombinedParser(fileText, _settings, lineMap);
          extractor =
              new PaloAltoControlPlaneExtractor(
                  fileText,
                  paParser,
                  _fileResults.get(filename)._warnings,
                  _fileResults.get(filename)._silentSyntax);
          parseFile(filename, paParser, extractor);
          break;

        default:
          throw new BatfishException(
              String.format("File format %s is neither unsupported nor handled", format));
      }
    } finally {
      parseSpan.finish();
    }

    String representativeFilename = getRepresentativeFilename(_fileTexts, format);
    VendorConfiguration vc = extractor.getVendorConfiguration();
    vc.setVendor(format);
    vc.setFilename(representativeFilename);
    if (Strings.isNullOrEmpty(vc.getHostname())) {
      _fileResults
          .get(representativeFilename)
          ._warnings
          .redFlag(
              String.format(
                  "No hostname set in %s\n",
                  _fileTexts.keySet().stream()
                      .map(f -> f.replace("\\", "/"))
                      .collect(ImmutableList.toImmutableList())));
      String guessedHostname =
          Paths.get(representativeFilename)
              .getFileName()
              .toString()
              .toLowerCase()
              .replaceAll("\\.(cfg|conf)$", "");
      _logger.redflag(
          "\tNo hostname set! Guessing hostname from filename: '"
              + representativeFilename
              + "' ==> '"
              + guessedHostname
              + "'\n");
      vc.setHostname(guessedHostname);
    }
    return vc;
  }

  private void parseFile(
      String filename,
      BatfishCombinedParser<?, ?> combinedParser,
      ControlPlaneExtractor extractor) {
    ParserRuleContext tree;
    Span parsingSpan = GlobalTracer.get().buildSpan("Parsing").start();
    try (Scope scope = GlobalTracer.get().scopeManager().activate(parsingSpan)) {
      assert scope != null; // avoid unused warning
      _logger.info("\tParsing...");
      tree = Batfish.parse(combinedParser, _logger, _settings);

      if (_settings.getPrintParseTree()) {
        _fileResults.get(filename)._parseTreeSentences =
            ParseTreePrettyPrinter.getParseTreeSentences(
                tree,
                combinedParser,
                _settings.getPrintParseTreeLineNums(),
                extractor.implementedRuleNames());
      }
      if (!combinedParser.getErrors().isEmpty()) {
        throw new BatfishException(
            String.format(
                "Configuration file(s): %s contains unrecognized lines:\n%s",
                _fileTexts.keySet(), String.join("\n", combinedParser.getErrors())));
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
        _fileResults.get(filename)._warnings.setErrorDetails(e.getErrorDetails());
        throw new BatfishException("Error processing parse tree", e);
      }

      _logger.info("OK\n");
    } finally {
      Batfish.logWarnings(_logger, _fileResults.get(filename)._warnings);
      postProcessSpan.finish();
    }
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
    ConfigurationFormat format = detectFormat(_fileTexts, _settings, _expectedFormat);

    String jobFiles = jobFilenamesToString(_fileTexts.keySet());
    // Handle specially some cases that will not produce a vendor configuration file.
    if (format == ConfigurationFormat.EMPTY) {
      _warnings.redFlag(String.format("Empty file(s): %s\n", jobFiles));
      return new ParseResult(null, null, _fileResults, format, ParseStatus.EMPTY, _warnings);
    } else if (format == ConfigurationFormat.IGNORED) {
      _warnings.redFlag(String.format("Ignored file(s): %s\n", jobFiles));
      return new ParseResult(null, null, _fileResults, format, ParseStatus.IGNORED, _warnings);
    } else if (format == ConfigurationFormat.UNKNOWN) {
      _warnings.redFlag(String.format("Unable to detect format for file(s): %s\n", jobFiles));
      return new ParseResult(null, null, _fileResults, format, ParseStatus.UNKNOWN, _warnings);
    } else if (UNIMPLEMENTED_FORMATS.contains(format)) {
      String unsupportedError =
          String.format(
              "Unsupported configuration format '%s' for file(s): %s\n", format, jobFiles);
      if (!_settings.ignoreUnsupported()) {
        return new ParseResult(
            null,
            new BatfishException(unsupportedError),
            _fileResults,
            format,
            ParseStatus.FAILED,
            _warnings);
      }
      _warnings.redFlag(unsupportedError);
      return new ParseResult(null, null, _fileResults, format, ParseStatus.UNSUPPORTED, _warnings);
    }

    try {
      // Actually parse the files.
      VendorConfiguration vc = parseFiles(format);
      ParseStatus status =
          vc.getUnrecognized() ? ParseStatus.PARTIALLY_UNRECOGNIZED : ParseStatus.PASSED;
      return new ParseResult(vc, null, _fileResults, format, status, _warnings);
    } catch (WillNotCommitException e) {
      if (_settings.getHaltOnParseError()) {
        // Fail the job if we need to
        return new ParseResult(
            null, e, _fileResults, format, ParseStatus.WILL_NOT_COMMIT, _warnings);
      }
      // Otherwise just generate a warning
      _warnings.redFlag(e.getMessage());
      return new ParseResult(
          null, null, _fileResults, format, ParseStatus.WILL_NOT_COMMIT, _warnings);
    } catch (Exception e) {
      return new ParseResult(
          null,
          new BatfishException(
              String.format("Error parsing configuration file(s): %s", _fileTexts.keySet()), e),
          _fileResults,
          format,
          ParseStatus.FAILED,
          _warnings);
    }
  }

  public ParseVendorConfigurationResult fromResult(ParseResult result, long elapsed) {
    if (result.getConfig() != null) {
      return new ParseVendorConfigurationResult(
          elapsed,
          _logger.getHistory(),
          result.getFileResults(),
          result.getFormat(),
          result.getConfig(),
          result.getWarnings(),
          result.getStatus(),
          _duplicateHostnames);
    } else if (result.getFailureCause() != null) {
      return new ParseVendorConfigurationResult(
          elapsed,
          _logger.getHistory(),
          result.getFileResults(),
          result.getFormat(),
          result.getWarnings(),
          result.getFailureCause());
    } else {
      return new ParseVendorConfigurationResult(
          elapsed,
          _logger.getHistory(),
          result.getFileResults(),
          result.getFormat(),
          result.getWarnings(),
          result.getStatus());
    }
  }

  @Override
  public ParseVendorConfigurationResult call() {
    Span span =
        GlobalTracer.get()
            .buildSpan("ParseVendorConfigurationJob for " + _fileTexts.keySet())
            .addReference(References.FOLLOWS_FROM, _spanContext)
            .start();
    try (Scope scope = GlobalTracer.get().scopeManager().activate(span)) {
      assert scope != null; // avoid unused warning

      _logger.infof("Processing: %s\n", _fileTexts.keySet());
      long startTime = System.currentTimeMillis();
      ParseResult result = parse();
      return fromResult(result, System.currentTimeMillis() - startTime);
    } finally {
      span.finish();
    }
  }

  /**
   * Returns a map from file name to its text content. The map has one entry for each file that is
   * part of this parsing job.
   */
  public Map<String, String> getFileTexts() {
    return _fileTexts;
  }

  /**
   * For some existing APIs, e.g., {@link
   * org.batfish.vendor.VendorConfiguration#setFilename(java.lang.String)}, a single filename is
   * expected. This representative filename, corresponding to the main file, will be used until we
   * can upgrade those APIs.
   */
  @SuppressWarnings("unused")
  private String getRepresentativeFilename(
      Map<String, String> fileTexts, ConfigurationFormat format) {
    if (fileTexts.size() == 1) {
      return Iterables.getOnlyElement(fileTexts.keySet());
    }
    // TODO: for multi-file cases, implement format-based representative
    throw new UnsupportedOperationException();
  }

  /** Returns a string, made up of filenames, used in warnings */
  static String jobFilenamesToString(Collection<String> filenames) {
    return filenames.size() == 1
        ? filenames.iterator().next() // backward-compatible, common case of one file
        : filenames.stream()
            .map(Names::escapeNameIfNeeded)
            .collect(ImmutableSortedSet.toImmutableSortedSet(Comparator.naturalOrder()))
            .toString();
  }
}
