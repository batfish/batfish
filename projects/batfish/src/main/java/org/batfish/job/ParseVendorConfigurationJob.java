package org.batfish.job;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
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
import org.batfish.grammar.FileParseResult;
import org.batfish.grammar.ParseTreePrettyPrinter;
import org.batfish.grammar.VendorConfigurationFormatDetector;
import org.batfish.grammar.cisco.CiscoCombinedParser;
import org.batfish.grammar.cisco.CiscoControlPlaneExtractor;
import org.batfish.grammar.cisco_asa.AsaCombinedParser;
import org.batfish.grammar.cisco_asa.AsaControlPlaneExtractor;
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
import org.batfish.grammar.frr.FrrCombinedParser;
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
import org.batfish.vendor.arista.grammar.AristaCombinedParser;
import org.batfish.vendor.arista.grammar.AristaControlPlaneExtractor;
import org.batfish.vendor.check_point_gateway.grammar.CheckPointGatewayCombinedParser;
import org.batfish.vendor.check_point_gateway.grammar.CheckPointGatewayControlPlaneExtractor;
import org.batfish.vendor.cisco_aci.representation.AciConfiguration;
import org.batfish.vendor.cisco_aci.representation.AciParser;
import org.batfish.vendor.cisco_aci.representation.FabricLink;
import org.batfish.vendor.cisco_nxos.grammar.CiscoNxosCombinedParser;
import org.batfish.vendor.cisco_nxos.grammar.NxosControlPlaneExtractor;
import org.batfish.vendor.sonic.grammar.SonicControlPlaneExtractor;
import org.batfish.vendor.sonic.grammar.SonicControlPlaneExtractor.SonicFileType;

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
          ConfigurationFormat.UNSUPPORTED,
          ConfigurationFormat.VXWORKS);

  /** Information about duplicate hostnames is collected here */
  private final Multimap<String, String> _duplicateHostnames;

  private final @Nonnull Map<String, String> _fileTexts;

  /**
   * What type of files are expected, or {@link ConfigurationFormat#UNKNOWN} to detect dynamically.
   */
  private final ConfigurationFormat _expectedFormat;

  /**
   * Map from fileName (relative to the snapshot base) to its {@link FileParseResult} object, which
   * is populated as part of parsing.
   */
  private final @Nonnull Map<String, FileParseResult> _fileResults;

  final NetworkSnapshot _snapshot;
  private final @Nonnull Warnings _warnings;

  public ParseVendorConfigurationJob(
      Settings settings,
      NetworkSnapshot snapshot,
      Map<String, String> fileTexts,
      Warnings.Settings logSettings,
      ConfigurationFormat expectedFormat,
      Multimap<String, String> duplicateHostnames) {
    super(settings);
    checkArgument(!fileTexts.isEmpty(), "Set of file texts cannot be empty");
    _fileTexts = ImmutableMap.copyOf(fileTexts);
    _fileResults =
        _fileTexts.keySet().stream()
            .collect(
                ImmutableMap.toImmutableMap(
                    Function.identity(),
                    f ->
                        new FileParseResult(
                            new ParseTreeSentences(),
                            new SilentSyntaxCollection(),
                            new Warnings(logSettings))));
    _expectedFormat = expectedFormat;
    _duplicateHostnames = duplicateHostnames;
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
  }

  /**
   * Returns the {@link VendorConfiguration vendor configuration} based on the files in this job.
   *
   * <p>Expects that empty, ignored, unsupported, or format-unknown files have been removed ahead of
   * time.
   *
   * @throws BatfishException if the given file is for an unsupported or unhandled format
   */
  @SuppressWarnings("fallthrough")
  private VendorConfiguration parseFiles(ConfigurationFormat format) {
    VendorConfiguration vc;
    FlattenerLineMap lineMap = null;

    // fileText obtained after flattening some formats, used in fallthrough cases below
    String flattenedFileText = null;

    switch (format) {
      case A10_ACOS:
        {
          Entry<String, String> fileEntry = Iterables.getOnlyElement(_fileTexts.entrySet());
          String filename = fileEntry.getKey();
          String fileText = fileEntry.getValue();
          A10CombinedParser a10Parser = new A10CombinedParser(fileText, _settings);
          ControlPlaneExtractor extractor =
              new A10ControlPlaneExtractor(
                  fileText,
                  a10Parser,
                  _fileResults.get(filename).getWarnings(),
                  _fileResults.get(filename).getSilentSyntax());
          parseFile(filename, a10Parser, extractor);
          vc = extractor.getVendorConfiguration();
          vc.setFilename(filename);
          break;
        }

      case ARISTA:
        {
          Entry<String, String> fileEntry = Iterables.getOnlyElement(_fileTexts.entrySet());
          String filename = fileEntry.getKey();
          String fileText = fileEntry.getValue();
          AristaCombinedParser aristaParser = new AristaCombinedParser(fileText, _settings);
          ControlPlaneExtractor extractor =
              new AristaControlPlaneExtractor(
                  fileText,
                  aristaParser,
                  format,
                  _fileResults.get(filename).getWarnings(),
                  _fileResults.get(filename).getSilentSyntax());
          parseFile(filename, aristaParser, extractor);
          vc = extractor.getVendorConfiguration();
          vc.setFilename(filename);
          break;
        }

      case ARUBAOS:
      case CADANT:
      case CISCO_IOS:
      case FORCE10:
      case FOUNDRY:
        {
          Entry<String, String> fileEntry = Iterables.getOnlyElement(_fileTexts.entrySet());
          String filename = fileEntry.getKey();
          String fileText = fileEntry.getValue();
          CiscoCombinedParser ciscoParser = new CiscoCombinedParser(fileText, _settings);
          ControlPlaneExtractor extractor =
              new CiscoControlPlaneExtractor(
                  fileText,
                  ciscoParser,
                  format,
                  _fileResults.get(filename).getWarnings(),
                  _fileResults.get(filename).getSilentSyntax());
          parseFile(filename, ciscoParser, extractor);
          vc = extractor.getVendorConfiguration();
          vc.setFilename(filename);
          break;
        }
      case CISCO_ASA:
        {
          Entry<String, String> fileEntry = Iterables.getOnlyElement(_fileTexts.entrySet());
          String filename = fileEntry.getKey();
          String fileText = fileEntry.getValue();
          AsaCombinedParser asaParser = new AsaCombinedParser(fileText, _settings);
          ControlPlaneExtractor extractor =
              new AsaControlPlaneExtractor(
                  fileText,
                  asaParser,
                  _fileResults.get(filename).getWarnings(),
                  _fileResults.get(filename).getSilentSyntax());
          parseFile(filename, asaParser, extractor);
          vc = extractor.getVendorConfiguration();
          vc.setFilename(filename);
          break;
        }

      case CISCO_NX:
        {
          Entry<String, String> fileEntry = Iterables.getOnlyElement(_fileTexts.entrySet());
          String filename = fileEntry.getKey();
          String fileText = fileEntry.getValue();
          CiscoNxosCombinedParser ciscoNxosParser =
              new CiscoNxosCombinedParser(fileText, _settings);
          ControlPlaneExtractor extractor =
              new NxosControlPlaneExtractor(
                  fileText,
                  ciscoNxosParser,
                  _fileResults.get(filename).getWarnings(),
                  _fileResults.get(filename).getSilentSyntax());
          parseFile(filename, ciscoNxosParser, extractor);
          vc = extractor.getVendorConfiguration();
          vc.setFilename(filename);
          break;
        }

      case CISCO_IOS_XR:
        {
          Entry<String, String> fileEntry = Iterables.getOnlyElement(_fileTexts.entrySet());
          String filename = fileEntry.getKey();
          String fileText = fileEntry.getValue();
          CiscoXrCombinedParser ciscoXrParser = new CiscoXrCombinedParser(fileText, _settings);
          ControlPlaneExtractor extractor =
              new CiscoXrControlPlaneExtractor(
                  fileText,
                  ciscoXrParser,
                  format,
                  _fileResults.get(filename).getWarnings(),
                  _fileResults.get(filename).getSilentSyntax());
          parseFile(filename, ciscoXrParser, extractor);
          vc = extractor.getVendorConfiguration();
          vc.setFilename(filename);
          break;
        }

      case CHECK_POINT_GATEWAY:
        {
          Entry<String, String> fileEntry = Iterables.getOnlyElement(_fileTexts.entrySet());
          String filename = fileEntry.getKey();
          String fileText = fileEntry.getValue();
          CheckPointGatewayCombinedParser checkPointParser =
              new CheckPointGatewayCombinedParser(fileText, _settings);
          ControlPlaneExtractor extractor =
              new CheckPointGatewayControlPlaneExtractor(
                  fileText,
                  checkPointParser,
                  _fileResults.get(filename).getWarnings(),
                  _fileResults.get(filename).getSilentSyntax());
          parseFile(filename, checkPointParser, extractor);
          vc = extractor.getVendorConfiguration();
          vc.setFilename(filename);
          break;
        }

      case CISCO_ACI:
        {
          AciConfiguration mergedConfig = null;
          List<FabricLink> fabricLinks = new ArrayList<>();
          String primaryFilename = null;
          for (Entry<String, String> fileEntry : _fileTexts.entrySet()) {
            String filename = fileEntry.getKey();
            String fileText = fileEntry.getValue();
            try {
              if (AciConfiguration.isFabricLinksJson(fileText)) {
                fabricLinks.addAll(AciParser.parseFabricLinksJson(filename, fileText));
                continue;
              }
              AciConfiguration config =
                  AciParser.fromFile(filename, fileText, _fileResults.get(filename).getWarnings());
              if (mergedConfig == null) {
                mergedConfig = config;
                primaryFilename = filename;
              } else {
                _fileResults
                    .get(filename)
                    .getWarnings()
                    .redFlagf(
                        "Multiple APIC model files found in cisco_aci_configs; ignoring %s in favor"
                            + " of %s",
                        filename, primaryFilename);
              }
            } catch (Exception e) {
              throw new BatfishException(
                  String.format(
                      "Failed to create ACI config from file: '%s', with error: %s",
                      filename, e.getMessage()),
                  e);
            }
          }
          if (mergedConfig == null) {
            throw new BatfishException(
                "No APIC model file found in cisco_aci_configs; expected at least one polUni"
                    + " JSON/XML file");
          }
          if (!fabricLinks.isEmpty()) {
            mergedConfig.setFabricLinks(fabricLinks);
          }
          return mergedConfig;
        }

      case CUMULUS_CONCATENATED:
        {
          Entry<String, String> fileEntry = Iterables.getOnlyElement(_fileTexts.entrySet());
          String filename = fileEntry.getKey();
          String fileText = fileEntry.getValue();
          CumulusConcatenatedCombinedParser parser =
              new CumulusConcatenatedCombinedParser(fileText, _settings);
          ControlPlaneExtractor extractor =
              new CumulusConcatenatedControlPlaneExtractor(
                  fileText,
                  _fileResults.get(filename).getWarnings(),
                  filename,
                  parser.getSettings(),
                  _settings.getPrintParseTree()
                      ? () -> _fileResults.get(filename).getParseTreeSentences()
                      : null,
                  _settings.getPrintParseTreeLineNums(),
                  _fileResults.get(filename).getSilentSyntax());
          parseFile(filename, parser, extractor);
          vc = extractor.getVendorConfiguration();
          vc.setFilename(filename);
          break;
        }

      case CUMULUS_NCLU:
        {
          Entry<String, String> fileEntry = Iterables.getOnlyElement(_fileTexts.entrySet());
          String filename = fileEntry.getKey();
          String fileText = fileEntry.getValue();
          CumulusNcluCombinedParser parser = new CumulusNcluCombinedParser(fileText, _settings);
          ControlPlaneExtractor extractor =
              new CumulusNcluControlPlaneExtractor(
                  fileText,
                  parser,
                  _fileResults.get(filename).getWarnings(),
                  _fileResults.get(filename).getSilentSyntax());
          parseFile(filename, parser, extractor);
          vc = extractor.getVendorConfiguration();
          vc.setFilename(filename);
          break;
        }

      case F5_BIGIP_STRUCTURED:
        {
          Entry<String, String> fileEntry = Iterables.getOnlyElement(_fileTexts.entrySet());
          String filename = fileEntry.getKey();
          String fileText = fileEntry.getValue();
          F5BigipStructuredCombinedParser parser =
              new F5BigipStructuredCombinedParser(fileText, _settings);
          ControlPlaneExtractor extractor =
              new F5BigipStructuredControlPlaneExtractor(
                  fileText,
                  parser,
                  _fileResults.get(filename).getWarnings(),
                  filename,
                  _settings.getPrintParseTree()
                      ? () -> _fileResults.get(filename).getParseTreeSentences()
                      : null,
                  _settings.getPrintParseTreeLineNums(),
                  _fileResults.get(filename).getSilentSyntax());
          parseFile(filename, parser, extractor);
          vc = extractor.getVendorConfiguration();
          vc.setFilename(filename);
          break;
        }

      case FORTIOS:
        {
          Entry<String, String> fileEntry = Iterables.getOnlyElement(_fileTexts.entrySet());
          String filename = fileEntry.getKey();
          String fileText = fileEntry.getValue();
          FortiosCombinedParser parser = new FortiosCombinedParser(fileText, _settings);
          ControlPlaneExtractor extractor =
              new FortiosControlPlaneExtractor(
                  fileText,
                  parser,
                  _fileResults.get(filename).getWarnings(),
                  _fileResults.get(filename).getSilentSyntax());
          parseFile(filename, parser, extractor);
          vc = extractor.getVendorConfiguration();
          vc.setFilename(filename);
          break;
        }

      case HOST:
        {
          Entry<String, String> fileEntry = Iterables.getOnlyElement(_fileTexts.entrySet());
          String filename = fileEntry.getKey();
          String fileText = fileEntry.getValue();
          try {
            return HostConfiguration.fromJson(
                filename, fileText, _fileResults.get(filename).getWarnings());
          } catch (Exception e) {
            throw new BatfishException(
                String.format(
                    "Failed to create host config from file: '%s', with error: %s",
                    filename, e.getMessage()),
                e);
          }
        }

      case SONIC:
        {
          Map<SonicFileType, String> sonicFileTypes =
              SonicControlPlaneExtractor.getSonicFileMap(_fileTexts);

          String frrFilename = sonicFileTypes.get(SonicFileType.FRR_CONF); // frr file must exist
          FrrCombinedParser frrParser =
              new FrrCombinedParser(_fileTexts.get(frrFilename), _settings, 1, 0);

          SonicControlPlaneExtractor extractor =
              new SonicControlPlaneExtractor(sonicFileTypes, _fileTexts, _fileResults, frrParser);

          try {
            extractor.processNonFrrFiles();
          } catch (JsonProcessingException exception) {
            throw new BatfishException("Error deserializing non-FRR file(s)", exception);
          }
          parseFile(frrFilename, frrParser, extractor);
          vc = extractor.getVendorConfiguration();
          vc.setFilename(frrFilename);
          vc.setSecondaryFilenames(
              _fileTexts.keySet().stream()
                  .filter(filename -> !filename.equals(frrFilename))
                  .collect(ImmutableList.toImmutableList()));
          break;
        }

      case VYOS:
        {
          Entry<String, String> fileEntry = Iterables.getOnlyElement(_fileTexts.entrySet());
          String filename = fileEntry.getKey();
          String fileText = fileEntry.getValue();
          _fileResults
              .get(filename)
              .getWarnings()
              .pedantic(
                  String.format(
                      "Flattening: '%s' on-the-fly; line-numbers reported for this file will be"
                          + " spurious\n",
                      filename));
          flattenedFileText =
              Batfish.flatten(
                      fileText,
                      _logger,
                      _settings,
                      _fileResults.get(filename).getWarnings(),
                      ConfigurationFormat.VYOS,
                      VendorConfigurationFormatDetector.BATFISH_FLATTENED_VYOS_HEADER)
                  .getFlattenedConfigurationText();
          // fall through
        }
      case FLAT_VYOS:
        {
          Entry<String, String> fileEntry = Iterables.getOnlyElement(_fileTexts.entrySet());
          String filename = fileEntry.getKey();
          String fileText = firstNonNull(flattenedFileText, fileEntry.getValue());
          FlatVyosCombinedParser flatVyosParser = new FlatVyosCombinedParser(fileText, _settings);
          ControlPlaneExtractor extractor =
              new FlatVyosControlPlaneExtractor(
                  fileText,
                  flatVyosParser,
                  _fileResults.get(filename).getWarnings(),
                  _fileResults.get(filename).getSilentSyntax());
          parseFile(filename, flatVyosParser, extractor);
          vc = extractor.getVendorConfiguration();
          vc.setFilename(filename);
          break;
        }

      case JUNIPER:
        {
          Entry<String, String> fileEntry = Iterables.getOnlyElement(_fileTexts.entrySet());
          String filename = fileEntry.getKey();
          String fileText = fileEntry.getValue();
          try {
            Flattener flattener =
                Batfish.flatten(
                    fileText,
                    _logger,
                    _settings,
                    _fileResults.get(filename).getWarnings(),
                    ConfigurationFormat.JUNIPER,
                    VendorConfigurationFormatDetector.BATFISH_FLATTENED_JUNIPER_HEADER);
            flattenedFileText = flattener.getFlattenedConfigurationText();
            lineMap = flattener.getOriginalLineMap();
          } catch (BatfishException e) {
            throw new BatfishException(
                String.format("Error flattening configuration file: '%s'", filename), e);
          }
          // fall through
        }
      case FLAT_JUNIPER:
        {
          Entry<String, String> fileEntry = Iterables.getOnlyElement(_fileTexts.entrySet());
          String filename = fileEntry.getKey();
          String fileText = firstNonNull(flattenedFileText, fileEntry.getValue());
          FlatJuniperCombinedParser flatJuniperParser =
              new FlatJuniperCombinedParser(fileText, _settings, lineMap);
          ControlPlaneExtractor extractor =
              new FlatJuniperControlPlaneExtractor(
                  fileText,
                  flatJuniperParser,
                  _fileResults.get(filename).getWarnings(),
                  _fileResults.get(filename).getSilentSyntax());
          parseFile(filename, flatJuniperParser, extractor);
          vc = extractor.getVendorConfiguration();
          vc.setFilename(filename);
          break;
        }

      case IPTABLES:
        {
          Entry<String, String> fileEntry = Iterables.getOnlyElement(_fileTexts.entrySet());
          String filename = fileEntry.getKey();
          String fileText = fileEntry.getValue();
          IptablesCombinedParser iptablesParser = new IptablesCombinedParser(fileText, _settings);
          ControlPlaneExtractor extractor =
              new IptablesControlPlaneExtractor(
                  fileText,
                  iptablesParser,
                  _fileResults.get(filename).getWarnings(),
                  filename,
                  _fileResults.get(filename).getSilentSyntax());
          parseFile(filename, iptablesParser, extractor);
          vc = extractor.getVendorConfiguration();
          vc.setFilename(filename);
          break;
        }

      case MRV:
        {
          Entry<String, String> fileEntry = Iterables.getOnlyElement(_fileTexts.entrySet());
          String filename = fileEntry.getKey();
          String fileText = fileEntry.getValue();
          MrvCombinedParser mrvParser = new MrvCombinedParser(fileText, _settings);
          ControlPlaneExtractor extractor =
              new MrvControlPlaneExtractor(
                  fileText,
                  mrvParser,
                  _fileResults.get(filename).getWarnings(),
                  _fileResults.get(filename).getSilentSyntax());
          parseFile(filename, mrvParser, extractor);
          vc = extractor.getVendorConfiguration();
          vc.setFilename(filename);
          break;
        }

      case PALO_ALTO_NESTED:
        {
          Entry<String, String> fileEntry = Iterables.getOnlyElement(_fileTexts.entrySet());
          String filename = fileEntry.getKey();
          String fileText = fileEntry.getValue();
          try {
            Flattener flattener =
                Batfish.flatten(
                    fileText,
                    _logger,
                    _settings,
                    _fileResults.get(filename).getWarnings(),
                    ConfigurationFormat.PALO_ALTO_NESTED,
                    VendorConfigurationFormatDetector.BATFISH_FLATTENED_PALO_ALTO_HEADER);
            flattenedFileText = flattener.getFlattenedConfigurationText();
            lineMap = flattener.getOriginalLineMap();
          } catch (BatfishException e) {
            throw new BatfishException(
                String.format("Error flattening configuration file: '%s'", filename), e);
          }
          // fall through
        }
      case PALO_ALTO:
        {
          Entry<String, String> fileEntry = Iterables.getOnlyElement(_fileTexts.entrySet());
          String filename = fileEntry.getKey();
          String fileText = firstNonNull(flattenedFileText, fileEntry.getValue());
          PaloAltoCombinedParser paParser =
              new PaloAltoCombinedParser(fileText, _settings, lineMap);
          ControlPlaneExtractor extractor =
              new PaloAltoControlPlaneExtractor(
                  fileText,
                  paParser,
                  _fileResults.get(filename).getWarnings(),
                  _fileResults.get(filename).getSilentSyntax());
          parseFile(filename, paParser, extractor);
          vc = extractor.getVendorConfiguration();
          vc.setFilename(filename);
          break;
        }

      default:
        throw new BatfishException(
            String.format("File format %s is neither unsupported nor handled", format));
    }

    vc.setVendor(format);
    if (Strings.isNullOrEmpty(vc.getHostname())) {
      _warnings.redFlagf("No hostname set in %s\n", jobFilenamesToString(_fileTexts.keySet()));
      String guessedHostname =
          Paths.get(vc.getFilename()) // use the primary file for guessing filename
              .getFileName()
              .toString()
              .toLowerCase()
              .replaceAll("\\.(cfg|conf)$", "");
      _logger.redflag(
          "\tNo hostname set! Guessing hostname from filename: '"
              + vc.getFilename()
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
    _logger.info("\tParsing...");
    tree = Batfish.parse(combinedParser, _logger, _settings);

    if (_settings.getPrintParseTree()) {
      _fileResults
          .get(filename)
          .setParseTreeSentences(
              ParseTreePrettyPrinter.getParseTreeSentences(
                  tree,
                  combinedParser,
                  _settings.getPrintParseTreeLineNums(),
                  extractor.implementedRuleNames()));
    }
    if (!combinedParser.getErrors().isEmpty()) {
      throw new BatfishException(
          String.format(
              "Configuration file(s): %s contains unrecognized lines:\n%s",
              _fileTexts.keySet(), String.join("\n", combinedParser.getErrors())));
    }

    try {
      _logger.info("\tPost-processing...");

      try {
        extractor.processParseTree(_snapshot, tree);
      } catch (BatfishParseException e) {
        _fileResults.get(filename).getWarnings().setErrorDetails(e.getErrorDetails());
        throw new BatfishException("Error processing parse tree", e);
      }

      _logger.info("OK\n");
    } finally {
      Batfish.logWarnings(_logger, _fileResults.get(filename).getWarnings());
    }
  }

  /**
   * Parses the given file and returns a {@link ParseResult} for this job.
   *
   * <p>The returned {@link ParseResult} will always have a valid {@link ParseStatus} for each file.
   * It may also contain a {@link ParseResult#getConfig() parsed vendor-specific configuration} or a
   * {@link ParseResult#getFailureCause() failure cause}.
   */
  public @Nonnull ParseResult parse() {
    ConfigurationFormat format = detectFormat(_fileTexts, _settings, _expectedFormat);

    String jobFiles = jobFilenamesToString(_fileTexts.keySet());
    // Handle specially some cases that will not produce a vendor configuration file.
    if (format == ConfigurationFormat.EMPTY) {
      _warnings.redFlagf("Empty file(s): %s\n", jobFiles);
      setParseStatus(ParseStatus.EMPTY);
      return new ParseResult(null, null, _fileResults, format, _warnings);
    } else if (format == ConfigurationFormat.IGNORED) {
      _warnings.redFlagf("Ignored file(s): %s\n", jobFiles);
      setParseStatus(ParseStatus.IGNORED);
      return new ParseResult(null, null, _fileResults, format, _warnings);
    } else if (format == ConfigurationFormat.UNKNOWN) {
      _warnings.redFlagf("Unable to detect format for file(s): %s\n", jobFiles);
      setParseStatus(ParseStatus.UNKNOWN);
      return new ParseResult(null, null, _fileResults, format, _warnings);
    } else if (UNIMPLEMENTED_FORMATS.contains(format)) {
      String unsupportedError =
          String.format(
              "Unsupported configuration format '%s' for file(s): %s\n", format, jobFiles);
      if (!_settings.ignoreUnsupported()) {
        setParseStatus(ParseStatus.FAILED);
        return new ParseResult(
            null, new BatfishException(unsupportedError), _fileResults, format, _warnings);
      }
      _warnings.redFlag(unsupportedError);
      setParseStatus(ParseStatus.UNSUPPORTED);
      return new ParseResult(null, null, _fileResults, format, _warnings);
    }

    try {
      // Actually parse the files.
      VendorConfiguration vc = parseFiles(format);
      setParseStatus(
          vc.getUnrecognized() ? ParseStatus.PARTIALLY_UNRECOGNIZED : ParseStatus.PASSED);
      return new ParseResult(vc, null, _fileResults, format, _warnings);
    } catch (WillNotCommitException e) {
      setParseStatus(ParseStatus.WILL_NOT_COMMIT);
      if (_settings.getHaltOnParseError()) {
        // Fail the job if we need to
        return new ParseResult(null, e, _fileResults, format, _warnings);
      }
      // Otherwise just generate a warning
      _warnings.redFlag(e.getMessage());
      return new ParseResult(null, null, _fileResults, format, _warnings);
    } catch (Exception e) {
      setParseStatus(ParseStatus.FAILED);
      return new ParseResult(
          null,
          new BatfishException(
              String.format("Error parsing configuration file(s): %s", _fileTexts.keySet()), e),
          _fileResults,
          format,
          _warnings);
    }
  }

  /** Sets the ParseStatus for files for which it is not already set. */
  private void setParseStatus(ParseStatus parseStatus) {
    _fileResults.forEach(
        (filename, fileResult) -> {
          if (fileResult.getParseStatus() == null) {
            fileResult.setParseStatus(parseStatus);
          }
        });
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
          result.getWarnings());
    }
  }

  @Override
  public ParseVendorConfigurationResult call() {
    _logger.infof("Processing: %s\n", _fileTexts.keySet());
    long startTime = System.currentTimeMillis();
    ParseResult result = parse();
    return fromResult(result, System.currentTimeMillis() - startTime);
  }

  /**
   * Returns a map from file name to its text content. The map has one entry for each file that is
   * part of this parsing job.
   */
  public Map<String, String> getFileTexts() {
    return _fileTexts;
  }

  /** Returns a string, made up of filenames, used in warnings */
  static @Nonnull String jobFilenamesToString(Collection<String> filenames) {
    return filenames.size() == 1
        ? filenames.iterator().next() // backward-compatible, common case of one file
        : filenames.stream()
            .map(Names::escapeNameIfNeeded)
            .collect(ImmutableSortedSet.toImmutableSortedSet(Comparator.naturalOrder()))
            .toString();
  }
}
