package org.batfish.vendor.sonic.grammar;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.antlr.v4.runtime.ParserRuleContext;
import org.batfish.common.ErrorDetails;
import org.batfish.common.NetworkSnapshot;
import org.batfish.grammar.BatfishParseTreeWalker;
import org.batfish.grammar.ControlPlaneExtractor;
import org.batfish.grammar.FileParseResult;
import org.batfish.grammar.ImplementedRules;
import org.batfish.grammar.frr.FrrCombinedParser;
import org.batfish.grammar.frr.FrrConfigurationBuilder;
import org.batfish.vendor.VendorConfiguration;
import org.batfish.vendor.sonic.representation.ConfigDb;
import org.batfish.vendor.sonic.representation.ResolvConf;
import org.batfish.vendor.sonic.representation.SnmpYml;
import org.batfish.vendor.sonic.representation.SonicConfiguration;

public class SonicControlPlaneExtractor implements ControlPlaneExtractor {

  public enum SonicFileType {
    CONFIG_DB_JSON,
    FRR_CONF,
    RESOLV_CONF,
    SNMP_YML
  }

  private final @Nonnull Map<String, String> _fileTexts;
  private final @Nonnull Map<SonicFileType, String> _fileTypes;
  private final @Nonnull Map<String, FileParseResult> _fileResults;
  private final @Nonnull FrrCombinedParser _frrParser;
  private final @Nonnull SonicConfiguration _configuration;

  public SonicControlPlaneExtractor(
      Map<SonicFileType, String> fileTypes,
      Map<String, String> fileTexts,
      Map<String, FileParseResult> fileResults,
      FrrCombinedParser frrParser) {
    _fileTypes = fileTypes;
    _fileTexts = fileTexts;
    _fileResults = fileResults;
    _frrParser = frrParser;
    _configuration = new SonicConfiguration();
  }

  @Override
  public VendorConfiguration getVendorConfiguration() {
    return _configuration;
  }

  @Override
  public Set<String> implementedRuleNames() {
    return ImplementedRules.getImplementedRules(FrrConfigurationBuilder.class);
  }

  public void processNonFrrFiles() throws JsonProcessingException {
    String configDbFilename = _fileTypes.get(SonicFileType.CONFIG_DB_JSON); // must exist
    ConfigDb configDb =
        ConfigDb.deserialize(
            _fileTexts.get(configDbFilename), _fileResults.get(configDbFilename).getWarnings());
    _configuration.setConfigDb(configDb);
    configDb.getHostname().ifPresent(_configuration::setHostname);

    String resolveConfFilename = _fileTypes.get(SonicFileType.RESOLV_CONF);
    if (resolveConfFilename != null) {
      ResolvConf resolveConf =
          ResolvConf.deserialize(
              _fileTexts.get(resolveConfFilename),
              _fileResults.get(resolveConfFilename).getWarnings());
      _configuration.setResolveConf(resolveConf);
    }

    String snmpYmlFilename = _fileTypes.get(SonicFileType.SNMP_YML);
    if (snmpYmlFilename != null) {
      ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
      try {
        // Trim YAML before deserializing since Jackson doesn't ignore, e.g., a trailing tab,
        // but SONiC doesn't mind.
        SnmpYml snmpYml = mapper.readValue(_fileTexts.get(snmpYmlFilename).trim(), SnmpYml.class);
        _configuration.setSnmpYml(snmpYml);
      } catch (JsonProcessingException exception) {
        ErrorDetails errorDetails =
            new ErrorDetails(
                String.format(
                    "Error deserializing %s: %s", snmpYmlFilename, exception.getMessage()));
        _fileResults.get(snmpYmlFilename).getWarnings().setErrorDetails(errorDetails);
      }
    }
  }

  /** This method is called for FRR parsing, after configDb processing */
  @Override
  public void processParseTree(NetworkSnapshot snapshot, ParserRuleContext tree) {
    String frrFilename = _fileTypes.get(SonicFileType.FRR_CONF); // must exist

    FrrConfigurationBuilder cb =
        new FrrConfigurationBuilder(
            _configuration,
            _frrParser,
            _fileResults.get(frrFilename).getWarnings(),
            _fileTexts.get(frrFilename),
            _fileResults.get(frrFilename).getSilentSyntax());
    new BatfishParseTreeWalker(_frrParser).walk(cb, tree);
  }

  /**
   * Given filename to text map for a device, returns a map from {@link SonicFileType} to filename.
   *
   * <p>Expects exactly one config_db.json file and exactly one frr.conf file. Other filetypes are
   * optional, but expects at most one file of each type.
   *
   * <p>Throws {@link IllegalArgumentException} if these expectations are violated or if the type of
   * a file cannot be determined.
   */
  public static @Nonnull Map<SonicFileType, String> getSonicFileMap(Map<String, String> fileTexts) {
    Map<SonicFileType, String> fileTypeMap = new EnumMap<>(SonicFileType.class);

    // Filetype detection is based on the tail of the filename

    for (String filename : fileTexts.keySet()) {
      String filenameLower = filename.toLowerCase();
      SonicFileType fileType = null;
      if (filenameLower.endsWith("config_db.json")) {
        fileType = SonicFileType.CONFIG_DB_JSON;
      } else if (filenameLower.endsWith("frr.conf") || filenameLower.endsWith("frr.cfg")) {
        fileType = SonicFileType.FRR_CONF;
      } else if (filenameLower.endsWith("resolv.conf")) {
        fileType = SonicFileType.RESOLV_CONF;
      } else if (filenameLower.endsWith("snmp.yml")) {
        fileType = SonicFileType.SNMP_YML;
      }
      if (fileType != null) {
        // duplicate type?
        if (fileTypeMap.containsKey(fileType)) {
          throw new IllegalArgumentException(
              String.format(
                  "Found two %s SONiC files: '%s', '%s'",
                  fileType, filename, fileTypeMap.get(fileType)));
        }
        fileTypeMap.put(fileType, filename);
      }
    }
    // for the 2-file case, try the deprecated content-based method if we didn't find both files
    if (fileTexts.size() == 2
        && (!fileTypeMap.containsKey(SonicFileType.CONFIG_DB_JSON)
            || !fileTypeMap.containsKey(SonicFileType.FRR_CONF))) {
      String frrFilename = getSonicFrrFilename(fileTexts);
      String configDbFilename =
          Sets.difference(fileTexts.keySet(), ImmutableSet.of(frrFilename)).iterator().next();
      fileTypeMap =
          ImmutableMap.of(
              SonicFileType.FRR_CONF, frrFilename, SonicFileType.CONFIG_DB_JSON, configDbFilename);
    }
    if (!fileTypeMap.containsKey(SonicFileType.CONFIG_DB_JSON)) {
      throw new IllegalArgumentException(
          String.format("config_db file not found among: %s", fileTexts.keySet()));
    }
    if (!fileTypeMap.containsKey(SonicFileType.FRR_CONF)) {
      throw new IllegalArgumentException(
          String.format("frr configuration file not found among: %s", fileTexts.keySet()));
    }
    Set<String> unknownFiles =
        Sets.difference(fileTexts.keySet(), ImmutableSet.copyOf(fileTypeMap.values()));
    if (!unknownFiles.isEmpty()) {
      throw new IllegalArgumentException(
          String.format(
              "Cannot determine the type of SONiC files: '%s'. Make sure that they have legal"
                  + " names.",
              unknownFiles));
    }
    return ImmutableMap.copyOf(fileTypeMap);
  }

  /**
   * Given filename to text map for a device, return which of the two files is frr.conf.
   *
   * <p>This method is deprecated. Use {@link #getSonicFileMap(Map)}.
   */
  @VisibleForTesting
  static @Nonnull String getSonicFrrFilename(Map<String, String> fileTexts) {
    if (fileTexts.size() != 2) {
      // Batfish pairs up files -- but we double check
      throw new IllegalArgumentException(
          String.format(
              "SONiC files should come in pairs. Got %d files: %s",
              fileTexts.size(), fileTexts.keySet()));
    }
    Iterator<String> fileIterator = fileTexts.keySet().iterator();
    String filename1 = fileIterator.next();
    String filename2 = fileIterator.next();
    // The file starting with '{' is a cheap way to check if it is configdb.json. Valid frr.conf
    // files cannot start with '{'.
    boolean fileText1IsJson = LIKELY_JSON.matcher(fileTexts.get(filename1)).find();
    boolean fileText2IsJson = LIKELY_JSON.matcher(fileTexts.get(filename2)).find();
    if (fileText1IsJson && !fileText2IsJson) {
      return filename2;
    }
    if (!fileText1IsJson && fileText2IsJson) {
      return filename1;
    }
    if (fileText1IsJson) { // if this is true fileText2 must also be JSON
      throw new IllegalArgumentException("Neither SONiC file appears to be frr configuration");
    }
    // both start with '{'
    throw new IllegalArgumentException("Neither SONiC file appears to be configdb JSON file");
  }

  private static final Pattern LIKELY_JSON = Pattern.compile("^\\s*\\{", Pattern.DOTALL);
}
