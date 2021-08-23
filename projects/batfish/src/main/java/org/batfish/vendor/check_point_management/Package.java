package org.batfish.vendor.check_point_management;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.util.BatfishObjectMapper;

/** Data model for a package from the response to the {@code show-packages} command. */
public final class Package extends TypedManagementObject {

  @VisibleForTesting
  Package(
      Domain domain,
      InstallationTargets installationTargets,
      String name,
      boolean natPolicy,
      Uid uid) {
    super(name, uid);
    _domain = domain;
    _installationTargets = installationTargets;
    _natPolicy = natPolicy;
  }

  @JsonCreator
  private static @Nonnull Package create(
      @JsonProperty(PROP_DOMAIN) @Nullable Domain domain,
      @JsonProperty(PROP_INSTALLATION_TARGETS) @Nullable JsonNode installationTargets,
      @JsonProperty(PROP_NAME) @Nullable String name,
      @JsonProperty(PROP_NAT_POLICY) @Nullable Boolean natPolicy,
      @JsonProperty(PROP_UID) @Nullable Uid uid) {
    checkArgument(domain != null, "Missing %s", PROP_DOMAIN);
    checkArgument(installationTargets != null, "Missing %s", PROP_INSTALLATION_TARGETS);
    checkArgument(name != null, "Missing %s", PROP_NAME);
    checkArgument(natPolicy != null, "Missing %s", PROP_NAT_POLICY);
    checkArgument(uid != null, "Missing %s", PROP_UID);
    return new Package(
        domain, deserializeInstallationTargets(installationTargets), name, natPolicy, uid);
  }

  private static @Nonnull InstallationTargets deserializeInstallationTargets(
      JsonNode installationTargets) {
    if (installationTargets instanceof TextNode) {
      String text = installationTargets.textValue();
      checkArgument(
          text.equals("all"),
          "Unsupported text value for installation-targets (expected \"all\"): %s",
          text);
      return AllInstallationTargets.instance();
    } else if (installationTargets instanceof ArrayNode) {
      List<PackageInstallationTarget> targets =
          ImmutableList.copyOf(
              BatfishObjectMapper.ignoreUnknownMapper()
                  .convertValue(
                      installationTargets,
                      new TypeReference<List<PackageInstallationTarget>>() {}));
      return new ListInstallationTargets(targets);
    } else {
      throw new IllegalArgumentException(
          String.format(
              "Unsupporeted JSON node type for value of %s field: %s",
              PROP_INSTALLATION_TARGETS, installationTargets.getClass()));
    }
  }

  public @Nonnull Domain getDomain() {
    return _domain;
  }

  public boolean hasNatPolicy() {
    return _natPolicy;
  }

  @Override
  public boolean equals(Object obj) {
    if (!baseEquals(obj)) {
      return false;
    }
    Package that = (Package) obj;
    return _domain.equals(that._domain)
        && _installationTargets.equals(that._installationTargets)
        && _natPolicy == that._natPolicy;
  }

  @Override
  public int hashCode() {
    return Objects.hash(baseHashcode(), _domain, _installationTargets, _natPolicy);
  }

  @Override
  public String toString() {
    return baseToStringHelper()
        .add(PROP_DOMAIN, _domain)
        .add(PROP_INSTALLATION_TARGETS, _installationTargets)
        .add(PROP_NAT_POLICY, _natPolicy)
        .toString();
  }

  private static final String PROP_DOMAIN = "domain";
  private static final String PROP_INSTALLATION_TARGETS = "installation-targets";
  private static final String PROP_NAT_POLICY = "nat-policy";

  private final @Nonnull Domain _domain;
  private final @Nonnull InstallationTargets _installationTargets;
  private final boolean _natPolicy;
}
