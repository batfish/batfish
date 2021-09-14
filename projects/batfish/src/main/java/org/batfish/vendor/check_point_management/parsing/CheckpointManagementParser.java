package org.batfish.vendor.check_point_management.parsing;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.collect.Maps.immutableEntry;
import static org.batfish.common.BfConsts.RELPATH_CHECKPOINT_MANAGEMENT_DIR;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.LinkedListMultimap;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.batfish.common.Warning;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.answers.ParseVendorConfigurationAnswerElement;
import org.batfish.vendor.check_point_management.AccessLayer;
import org.batfish.vendor.check_point_management.AccessRule;
import org.batfish.vendor.check_point_management.AccessRuleOrSection;
import org.batfish.vendor.check_point_management.AccessSection;
import org.batfish.vendor.check_point_management.CheckpointManagementConfiguration;
import org.batfish.vendor.check_point_management.Domain;
import org.batfish.vendor.check_point_management.GatewaysAndServers;
import org.batfish.vendor.check_point_management.ManagementDomain;
import org.batfish.vendor.check_point_management.ManagementPackage;
import org.batfish.vendor.check_point_management.ManagementServer;
import org.batfish.vendor.check_point_management.NamedManagementObject;
import org.batfish.vendor.check_point_management.NatRule;
import org.batfish.vendor.check_point_management.NatRuleOrSection;
import org.batfish.vendor.check_point_management.NatRulebase;
import org.batfish.vendor.check_point_management.NatSection;
import org.batfish.vendor.check_point_management.ObjectPage;
import org.batfish.vendor.check_point_management.Package;
import org.batfish.vendor.check_point_management.TypedManagementObject;
import org.batfish.vendor.check_point_management.Uid;

public class CheckpointManagementParser {

  public static @Nonnull CheckpointManagementConfiguration parseCheckpointManagementData(
      Map<String, String> cpManagementData, ParseVendorConfigurationAnswerElement pvcae) {
    /* Organize server data into maps */
    // server -> domain -> filename -> file contents
    Map<String, Map<String, Map<String, String>>> domainFileMap = new HashMap<>();
    // server -> domain -> -> package -> filename -> file contents
    Map<String, Map<String, Map<String, Map<String, String>>>> packageFileMap = new HashMap<>();
    cpManagementData.forEach(
        (filePath, fileContent) -> {
          String[] parts = filePath.split("/");
          if (parts.length == 4) {
            // checkpoint_management/SERVER_NAME/DOMAIN_NAME/foo.json
            String serverName = parts[1];
            String domainName = parts[2];
            String fileName = parts[3];
            domainFileMap
                .computeIfAbsent(serverName, n -> new HashMap<>())
                .computeIfAbsent(domainName, n -> new HashMap<>())
                .put(fileName, fileContent);
          } else if (parts.length == 5) {
            // checkpoint_management/SERVER_NAME/DOMAIN_NAME/PACKAGE_NAME/foo.json
            String serverName = parts[1];
            String domainName = parts[2];
            String packageName = parts[3];
            String fileName = parts[4];
            packageFileMap
                .computeIfAbsent(serverName, n -> new HashMap<>())
                .computeIfAbsent(domainName, n -> new HashMap<>())
                .computeIfAbsent(packageName, n -> new HashMap<>())
                .put(fileName, fileContent);
          }
        });
    return new CheckpointManagementConfiguration(
        buildServersMap(domainFileMap, packageFileMap, pvcae));
  }

  /** Read Access Layer data. */
  private static @Nonnull List<AccessLayer> readAccessLayers(
      Package pakij,
      String domainName,
      Map<String, String> packageFiles,
      ParseVendorConfigurationAnswerElement pvcae,
      String serverName) {
    return mergeAccessLayers(
        firstNonNull(
            tryParseCheckpointPackageFile(
                packageFiles,
                new TypeReference<List<AccessLayer>>() {},
                pvcae,
                serverName,
                domainName,
                pakij.getName(),
                RELPATH_CHECKPOINT_SHOW_ACCESS_RULEBASE),
            ImmutableList.of()),
        pvcae);
  }

  /**
   * Returns a single AccessRuleOrSection representing the specified collection.
   *
   * <p>The specified collection of items should contain one of the following:
   *
   * <ul>
   *   <li>1. a single AccessRule (rules can't be split across pages)
   *   <li>2. a single AccessSection (contained fully on a single page)
   *   <li>3. multiple AccessSection (all with the same name and uid, with different rules)
   * </ul>
   */
  private static @Nonnull AccessRuleOrSection mergeRuleOrSection(
      Collection<AccessRuleOrSection> items, ParseVendorConfigurationAnswerElement pvcae) {
    AccessRuleOrSection first = items.iterator().next();
    if (items.stream().anyMatch(AccessRule.class::isInstance)) {
      // Shouldn't happen w/ well-formed data, but check to prevent bad casting below
      if (items.size() > 1) {
        pvcae.addRedFlagWarning(
            RELPATH_CHECKPOINT_MANAGEMENT_DIR,
            new Warning(
                String.format(
                    "Cannot merge AccessRule pages (for uid %s), ignoring instances after the"
                        + " first",
                    ((AccessRule) first).getUid().getValue()),
                "Checkpoint"));
      }
      return first;
    }

    AccessSection firstSection = (AccessSection) first;
    return new AccessSection(
        firstSection.getName(),
        items.stream()
            .flatMap(s -> ((AccessSection) s).getRulebase().stream())
            .collect(ImmutableList.toImmutableList()),
        firstSection.getUid());
  }

  /**
   * Returns a list of unique Access Rules and Access Sections, generated from the specified
   * collection of non-unique items; i.e. multiple partial Access Sections can be specified and will
   * be merged into a single Access Section in the resulting list.
   *
   * <p>The items should be provided in order.
   */
  private static @Nonnull List<AccessRuleOrSection> mergeRuleOrSections(
      Collection<AccessRuleOrSection> items, ParseVendorConfigurationAnswerElement pvcae) {
    LinkedListMultimap<Uid, AccessRuleOrSection> uidToChunks = LinkedListMultimap.create();
    items.forEach(i -> uidToChunks.put(((NamedManagementObject) i).getUid(), i));
    return uidToChunks.keySet().stream()
        .map(uid -> mergeRuleOrSection(uidToChunks.get(uid), pvcae))
        .collect(ImmutableList.toImmutableList());
  }

  /**
   * Merges multiple pages of a <i>single</i> Access Layer and returns the resulting Access Layer.
   *
   * <p>Assumes all supplied pages are for the same AccessLayer.
   */
  private static @Nonnull AccessLayer mergeAccessLayer(
      Collection<AccessLayer> pages, ParseVendorConfigurationAnswerElement pvcae) {
    assert !pages.isEmpty();
    AccessLayer first = pages.iterator().next();
    Uid uid = first.getUid();
    String name = first.getName();
    Map<Uid, NamedManagementObject> objs = new HashMap<>();
    pages.stream()
        .flatMap(p -> p.getObjectsDictionary().entrySet().stream())
        .forEach(o -> objs.put(o.getKey(), o.getValue()));
    return new AccessLayer(
        ImmutableMap.copyOf(objs),
        mergeRuleOrSections(
            pages.stream()
                .flatMap(p -> p.getRulebase().stream())
                .collect(ImmutableList.toImmutableList()),
            pvcae),
        uid,
        name);
  }

  /**
   * Merges multiple pages of non-unique Access Layers.
   *
   * <p>The pages should be provided in order.
   */
  @VisibleForTesting
  static @Nonnull List<AccessLayer> mergeAccessLayers(
      Collection<AccessLayer> pages, ParseVendorConfigurationAnswerElement pvcae) {
    LinkedListMultimap<Uid, AccessLayer> uidToPages = LinkedListMultimap.create();
    pages.forEach(p -> uidToPages.put(p.getUid(), p));
    return uidToPages.keySet().stream()
        .map(uid -> mergeAccessLayer(uidToPages.get(uid), pvcae))
        .collect(ImmutableList.toImmutableList());
  }

  /** Read NAT rulebase data. */
  @VisibleForTesting
  static @Nullable NatRulebase readNatRulebase(
      Package pakij,
      String domainName,
      Map<String, String> packageFiles,
      ParseVendorConfigurationAnswerElement pvcae,
      String serverName) {
    if (!pakij.hasNatPolicy()) {
      return null;
    }
    String packageName = pakij.getName();
    List<NatRulebase> natRulebases =
        tryParseCheckpointPackageFile(
            packageFiles,
            new TypeReference<List<NatRulebase>>() {},
            pvcae,
            serverName,
            domainName,
            packageName,
            RELPATH_CHECKPOINT_SHOW_NAT_RULEBASE);
    if (natRulebases == null) {
      return null;
    } else if (natRulebases.isEmpty()) {
      warnCheckpointPackageFile(
          serverName,
          domainName,
          packageName,
          RELPATH_CHECKPOINT_SHOW_NAT_RULEBASE,
          "JSON file contains no NAT rulebase information.",
          pvcae,
          null);
      return null;
    }
    long numUids = natRulebases.stream().map(NatRulebase::getUid).distinct().count();
    Uid uid = natRulebases.iterator().next().getUid();
    if (numUids > 1) {
      warnCheckpointPackageFile(
          serverName,
          domainName,
          packageName,
          RELPATH_CHECKPOINT_SHOW_NAT_RULEBASE,
          String.format(
              "JSON file should contain one or more pages for exactly one NAT rulebase, but"
                  + " contains %s. Only reading pages for the first, with UID '%s'.",
              numUids, uid.getValue()),
          pvcae,
          null);
    }
    return mergeNatRulebasePages(
        natRulebases.stream()
            .filter(rulebase -> rulebase.getUid().equals(uid))
            .collect(ImmutableList.toImmutableList()),
        pvcae);
  }

  /**
   * Returns a single NatRuleOrSection representing the specified collection.
   *
   * <p>The specified collection of items should contain one of the following:
   *
   * <ul>
   *   <li>1. a single NatRule (rules can't be split across pages)
   *   <li>2. a single NatSection part (contained fully on a single page)
   *   <li>3. multiple NatSection parts (all with the same name and uid, with different rules)
   * </ul>
   */
  @VisibleForTesting
  static @Nonnull NatRuleOrSection mergeNatRuleOrSection(
      Collection<NatRuleOrSection> items, ParseVendorConfigurationAnswerElement pvcae) {
    NatRuleOrSection first = items.iterator().next();
    if (items.stream().anyMatch(NatRule.class::isInstance)) {
      // Shouldn't happen w/ well-formed data, but check to prevent bad casting below
      if (items.size() > 1) {
        Uid uid = first.getUid();
        pvcae.addRedFlagWarning(
            RELPATH_CHECKPOINT_MANAGEMENT_DIR,
            new Warning(
                String.format(
                    "Cannot merge NatRule pages (for uid %s), ignoring instances after the"
                        + " first",
                    uid.getValue()),
                "Checkpoint"));
      }
      return first;
    }

    NatSection firstSection = (NatSection) first;
    return new NatSection(
        firstSection.getName(),
        items.stream()
            .flatMap(s -> ((NatSection) s).getRulebase().stream())
            .collect(ImmutableList.toImmutableList()),
        firstSection.getUid());
  }

  /**
   * Returns a list of unique NAT Rules and NAT Sections, generated from the specified collection of
   * non-unique items; i.e. multiple partial NAT Sections can be specified and will be merged into a
   * single NAT Section in the resulting list.
   *
   * <p>The items should be provided in order.
   */
  private static @Nonnull List<NatRuleOrSection> mergeNatRuleOrSections(
      Collection<NatRuleOrSection> items, ParseVendorConfigurationAnswerElement pvcae) {
    LinkedListMultimap<Uid, NatRuleOrSection> uidToParts = LinkedListMultimap.create();
    items.forEach(i -> uidToParts.put((i).getUid(), i));
    return uidToParts.keySet().stream()
        .map(uid -> mergeNatRuleOrSection(uidToParts.get(uid), pvcae))
        .collect(ImmutableList.toImmutableList());
  }

  /**
   * Merges multiple pages of a <i>single</i> NAT rulebase and returns the resulting rulebase.
   *
   * <p>Assumes all supplied pages are for the same rulebase.
   */
  static @Nonnull NatRulebase mergeNatRulebasePages(
      Collection<NatRulebase> pages, ParseVendorConfigurationAnswerElement pvcae) {
    assert !pages.isEmpty();
    NatRulebase first = pages.iterator().next();
    Uid uid = first.getUid();
    Map<Uid, TypedManagementObject> objs = new HashMap<>();
    pages.stream()
        .flatMap(p -> p.getObjectsDictionary().entrySet().stream())
        .forEach(o -> objs.put(o.getKey(), o.getValue()));

    return new NatRulebase(
        ImmutableMap.copyOf(objs),
        mergeNatRuleOrSections(
            pages.stream()
                .flatMap(p -> p.getRulebase().stream())
                .collect(ImmutableList.toImmutableList()),
            pvcae),
        uid);
  }

  /**
   * Reads all {@link ObjectPage}s from specified {@code filename} and returns a consolidated list
   * of {@link TypedManagementObject} from all pages.
   */
  private static List<TypedManagementObject> readObjects(
      String filename,
      Map<String, Map<String, Map<String, String>>> domainFileMap,
      String domainName,
      String serverName,
      ParseVendorConfigurationAnswerElement pvcae) {
    List<ObjectPage> objectPages =
        tryParseCheckpointDomainFile(
            domainFileMap,
            new TypeReference<List<ObjectPage>>() {},
            pvcae,
            serverName,
            domainName,
            filename);
    return objectPages == null
        ? ImmutableList.of()
        : objectPages.stream()
            .flatMap(p -> p.getObjects().stream())
            .collect(ImmutableList.toImmutableList());
  }

  @VisibleForTesting
  static List<TypedManagementObject> buildObjectsList(
      Map<String, Map<String, Map<String, String>>> domainFileMap,
      String domainName,
      String serverName,
      ParseVendorConfigurationAnswerElement pvcae) {
    return ImmutableList.of(
            RELPATH_CHECKPOINT_SHOW_GROUPS,
            RELPATH_CHECKPOINT_SHOW_HOSTS,
            RELPATH_CHECKPOINT_SHOW_NETWORKS,
            RELPATH_CHECKPOINT_SHOW_SERVICE_GROUPS,
            RELPATH_CHECKPOINT_SHOW_SERVICES_ICMP,
            RELPATH_CHECKPOINT_SHOW_SERVICES_OTHER,
            RELPATH_CHECKPOINT_SHOW_SERVICES_TCP,
            RELPATH_CHECKPOINT_SHOW_SERVICES_UDP)
        .stream()
        .flatMap(f -> readObjects(f, domainFileMap, domainName, serverName, pvcae).stream())
        .filter(Objects::nonNull)
        .collect(ImmutableList.toImmutableList());
  }

  /**
   * Returns a map of server name to {@link ManagementServer} based on supplied domain and package
   * files.
   *
   * <p>{@code domainFileMap} is a map of server -> domain -> filename -> file contents
   *
   * <p>{@code packageFileMap} is a map of server -> domain -> -> package -> filename -> file
   * contents
   */
  private static Map<String, ManagementServer> buildServersMap(
      Map<String, Map<String, Map<String, String>>> domainFileMap,
      Map<String, Map<String, Map<String, Map<String, String>>>> packageFileMap,
      ParseVendorConfigurationAnswerElement pvcae) {

    Map<Entry<String, String>, ManagementDomain> domainByServerDomain =
        domainFileMap.entrySet().stream()
            .flatMap(
                domainsByServerEntry -> {
                  String serverName = domainsByServerEntry.getKey();
                  return domainsByServerEntry.getValue().keySet().stream()
                      .map(domainName -> immutableEntry(serverName, domainName));
                })
            .parallel() // stream of entries of <serverName, domainName>
            .map(
                serverDomain ->
                    immutableEntry(
                        serverDomain,
                        buildManagementDomain(
                            serverDomain.getKey(),
                            serverDomain.getValue(),
                            domainFileMap,
                            packageFileMap,
                            pvcae)))
            .filter(domainByServerDomainEntry -> domainByServerDomainEntry.getValue() != null)
            .collect(ImmutableMap.toImmutableMap(Entry::getKey, Entry::getValue));
    Map<String, ImmutableMap.Builder<String, ManagementDomain>> domainsByServer = new HashMap<>();
    domainByServerDomain.forEach(
        (domainsByServerDomainEntry, managementDomain) -> {
          String serverName = domainsByServerDomainEntry.getKey();
          String domainName = domainsByServerDomainEntry.getValue();
          domainsByServer
              .computeIfAbsent(serverName, n -> ImmutableMap.builder())
              .put(domainName, managementDomain);
        });
    ImmutableMap.Builder<String, ManagementServer> serversMap = ImmutableMap.builder();
    domainsByServer.forEach(
        (serverName, domainByName) ->
            serversMap.put(serverName, new ManagementServer(domainByName.build(), serverName)));
    return serversMap.build();
  }

  private static @Nullable ManagementDomain buildManagementDomain(
      String serverName,
      String domainName,
      Map<String, Map<String, Map<String, String>>> domainFileMap,
      Map<String, Map<String, Map<String, Map<String, String>>>> packageFileMap,
      ParseVendorConfigurationAnswerElement pvcae) {
    GatewaysAndServers gatewaysAndServers =
        readGatewaysAndServers(serverName, domainName, domainFileMap, pvcae);
    if (gatewaysAndServers == null) {
      return null;
    }

    List<TypedManagementObject> objects =
        buildObjectsList(domainFileMap, domainName, serverName, pvcae);

    ImmutableMap.Builder<Uid, ManagementPackage> packagesBuilder = ImmutableMap.builder();
    for (Entry<String, Map<String, String>> packageEntry :
        packageFileMap.get(serverName).get(domainName).entrySet()) {
      String packageName = packageEntry.getKey();
      Map<String, String> packageFiles = packageEntry.getValue();
      List<Package> showPackageListEntries =
          tryParseCheckpointPackageFile(
              packageFiles,
              new TypeReference<List<Package>>() {},
              pvcae,
              serverName,
              domainName,
              packageName,
              RELPATH_CHECKPOINT_SHOW_PACKAGE);
      if (showPackageListEntries == null) {
        continue;
      } else if (showPackageListEntries.isEmpty()) {
        warnCheckpointPackageFile(
            serverName,
            domainName,
            packageName,
            RELPATH_CHECKPOINT_SHOW_PACKAGE,
            "has no package entry in the JSON",
            pvcae,
            null);
        continue;
      } else if (showPackageListEntries.size() > 1) {
        warnCheckpointPackageFile(
            serverName,
            domainName,
            packageName,
            RELPATH_CHECKPOINT_SHOW_PACKAGE,
            "has extra packages in the JSON. Using the first entry.",
            pvcae,
            null);
      }
      Package pakij = showPackageListEntries.get(0);
      // Note that warnings from hereon will use package name from JSON rather than directory
      // name. In the future we may want to encode that name in base64 so we can guarantee
      // the same package name can be retrieved from JSON and directory name.
      List<AccessLayer> accessLayers =
          readAccessLayers(pakij, domainName, packageFiles, pvcae, serverName);
      NatRulebase natRulebase = readNatRulebase(pakij, domainName, packageFiles, pvcae, serverName);
      ManagementPackage mgmtPackage = new ManagementPackage(accessLayers, natRulebase, pakij);
      packagesBuilder.put(mgmtPackage.getPackage().getUid(), mgmtPackage);
    }
    Map<Uid, ManagementPackage> packages = packagesBuilder.build();
    if (packages.isEmpty()) {
      String message =
          String.format(
              "Ignoring Checkpoint management domain %s on server %s: no packages present",
              domainName, serverName);
      pvcae.addRedFlagWarning(
          RELPATH_CHECKPOINT_MANAGEMENT_DIR, new Warning(message, "Checkpoint"));
      LOGGER.warn(message);
      return null;
    }

    // Use any package to find domain
    Domain domain = packages.values().iterator().next().getPackage().getDomain();
    return new ManagementDomain(
        domain, gatewaysAndServers.getGatewaysAndServers(), packages, objects);
  }

  /** Read gateways and servers data. */
  @VisibleForTesting
  static GatewaysAndServers readGatewaysAndServers(
      String serverName,
      String domainName,
      Map<String, Map<String, Map<String, String>>> domainFileMap,
      ParseVendorConfigurationAnswerElement pvcae) {
    List<GatewaysAndServers> gatewaysAndServersList =
        tryParseCheckpointDomainFile(
            domainFileMap,
            new TypeReference<List<GatewaysAndServers>>() {},
            pvcae,
            serverName,
            domainName,
            RELPATH_CHECKPOINT_SHOW_GATEWAYS_AND_SERVERS);
    if (gatewaysAndServersList == null) {
      return null;
    } else if (gatewaysAndServersList.isEmpty()) {
      warnCheckpointDomainFile(
          serverName,
          domainName,
          RELPATH_CHECKPOINT_SHOW_GATEWAYS_AND_SERVERS,
          "JSON file contains no gateways-and-servers pages.",
          pvcae,
          null);
      return null;
    }
    return mergeGatewaysAndServersPages(gatewaysAndServersList);
  }

  private static @Nullable <T> T tryParseCheckpointDomainFile(
      Map<String, Map<String, Map<String, String>>> domainFileMap,
      TypeReference<T> typeReference,
      ParseVendorConfigurationAnswerElement pvcae,
      String serverName,
      String domainName,
      String filename) {
    String jsonText =
        domainFileMap
            .getOrDefault(serverName, ImmutableMap.of())
            .getOrDefault(domainName, ImmutableMap.of())
            .get(filename);
    if (jsonText == null) {
      warnCheckpointDomainFile(serverName, domainName, filename, "file is missing", pvcae, null);
      return null;
    }
    try {
      return BatfishObjectMapper.ignoreUnknownMapper().readValue(jsonText, typeReference);
    } catch (JsonProcessingException e) {
      warnCheckpointDomainFile(serverName, domainName, filename, "failed to parse JSON", pvcae, e);
      return null;
    }
  }

  private static void warnCheckpointDomainFile(
      String serverName,
      String domainName,
      String filename,
      String reason,
      ParseVendorConfigurationAnswerElement pvcae,
      @Nullable Throwable throwable) {
    String inputObjectKey =
        String.format(
            "%s/%s/%s/%s", RELPATH_CHECKPOINT_MANAGEMENT_DIR, serverName, domainName, filename);
    String warning =
        String.format(
            "Checkpoint management server '%s' domain '%s' file '%s' at '%s': %s",
            serverName, domainName, filename, inputObjectKey, reason);
    if (throwable != null) {
      LOGGER.warn(warning, throwable);
      pvcae.addRedFlagWarning(
          RELPATH_CHECKPOINT_MANAGEMENT_DIR,
          new Warning(
              String.format("%s: %s", warning, Throwables.getStackTraceAsString(throwable)),
              "Checkpoint"));
    } else {
      LOGGER.warn(warning);
      pvcae.addRedFlagWarning(
          RELPATH_CHECKPOINT_MANAGEMENT_DIR, new Warning(warning, "Checkpoint"));
    }
  }

  private static @Nullable <T> T tryParseCheckpointPackageFile(
      Map<String, String> packageFiles,
      TypeReference<T> typeReference,
      ParseVendorConfigurationAnswerElement pvcae,
      String serverName,
      String domainName,
      String packageName,
      String filename) {
    String jsonText = packageFiles.get(filename);
    if (jsonText == null) {
      warnCheckpointPackageFile(
          serverName, domainName, packageName, filename, "file is missing", pvcae, null);
      return null;
    }
    try {
      return BatfishObjectMapper.ignoreUnknownMapper().readValue(jsonText, typeReference);
    } catch (JsonProcessingException e) {
      warnCheckpointPackageFile(
          serverName, domainName, packageName, filename, "failed to parse JSON", pvcae, e);
      return null;
    }
  }

  private static void warnCheckpointPackageFile(
      String serverName,
      String domainName,
      String packageName,
      String filename,
      String reason,
      ParseVendorConfigurationAnswerElement pvcae,
      @Nullable Throwable throwable) {
    String inputObjectKey =
        String.format(
            "%s/%s/%s/%s/%s",
            RELPATH_CHECKPOINT_MANAGEMENT_DIR, serverName, domainName, packageName, filename);
    String warning =
        String.format(
            "Checkpoint management server '%s' domain '%s' package '%s' file '%s' at '%s': %s",
            serverName, domainName, packageName, filename, inputObjectKey, reason);
    if (throwable != null) {
      LOGGER.warn(warning, throwable);
      pvcae.addRedFlagWarning(
          RELPATH_CHECKPOINT_MANAGEMENT_DIR,
          new Warning(
              String.format("%s: %s", warning, Throwables.getStackTraceAsString(throwable)),
              "Checkpoint"));
    } else {
      LOGGER.warn(warning);
      pvcae.addRedFlagWarning(
          RELPATH_CHECKPOINT_MANAGEMENT_DIR, new Warning(warning, "Checkpoint"));
    }
  }

  /**
   * Merge objects from each page of gateways and servers data into a single {@link
   * GatewaysAndServers} object.
   */
  private static @Nonnull GatewaysAndServers mergeGatewaysAndServersPages(
      List<GatewaysAndServers> gatewaysAndServersList) {
    if (gatewaysAndServersList.size() == 1) {
      return gatewaysAndServersList.get(0);
    }
    return new GatewaysAndServers(
        gatewaysAndServersList.stream()
            .map(GatewaysAndServers::getGatewaysAndServers)
            .map(Map::entrySet)
            .flatMap(Collection::stream)
            .collect(ImmutableMap.toImmutableMap(Entry::getKey, Entry::getValue)));
  }

  private static final Logger LOGGER = LogManager.getLogger(CheckpointManagementParser.class);

  private static final String RELPATH_CHECKPOINT_SHOW_ACCESS_RULEBASE = "show-access-rulebase.json";

  @VisibleForTesting
  static final String RELPATH_CHECKPOINT_SHOW_GATEWAYS_AND_SERVERS =
      "show-gateways-and-servers.json";

  @VisibleForTesting static final String RELPATH_CHECKPOINT_SHOW_GROUPS = "show-groups.json";
  private static final String RELPATH_CHECKPOINT_SHOW_HOSTS = "show-hosts.json";

  @VisibleForTesting
  static final String RELPATH_CHECKPOINT_SHOW_NAT_RULEBASE = "show-nat-rulebase.json";

  private static final String RELPATH_CHECKPOINT_SHOW_NETWORKS = "show-networks.json";
  private static final String RELPATH_CHECKPOINT_SHOW_PACKAGE = "show-package.json";

  @VisibleForTesting
  static final String RELPATH_CHECKPOINT_SHOW_SERVICE_GROUPS = "show-service-groups.json";

  @VisibleForTesting
  static final String RELPATH_CHECKPOINT_SHOW_SERVICES_ICMP = "show-services-icmp.json";

  @VisibleForTesting
  static final String RELPATH_CHECKPOINT_SHOW_SERVICES_OTHER = "show-services-other.json";

  @VisibleForTesting
  static final String RELPATH_CHECKPOINT_SHOW_SERVICES_TCP = "show-services-tcp.json";

  @VisibleForTesting
  static final String RELPATH_CHECKPOINT_SHOW_SERVICES_UDP = "show-services-udp.json";
}
