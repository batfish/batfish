package org.batfish.representation.juniper;

import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/** Resolution RIB settings for a routing instance. */
@ParametersAreNonnullByDefault
public final class ResolutionRib implements Serializable {

  public ResolutionRib(String name) {
    _name = name;
    _importPolicies = ImmutableList.of();
    _inet6ImportPolicies = ImmutableList.of();
    _inet6ResolutionRibs = ImmutableList.of();
    _inetImportPolicies = ImmutableList.of();
    _inetResolutionRibs = ImmutableList.of();
    _isoImportPolicies = ImmutableList.of();
    _isoResolutionRibs = ImmutableList.of();
    _resolutionRibs = ImmutableList.of();
  }

  public @Nonnull String getName() {
    return _name;
  }

  public void addImportPolicy(String policy) {
    _importPolicies = ImmutableList.<String>builder().addAll(_importPolicies).add(policy).build();
  }

  /** Returns the policy(ies) routes in this RIB must match to be usable for next-hop resolution. */
  public @Nonnull List<String> getImportPolicies() {
    return _importPolicies;
  }

  public void addInet6ImportPolicy(String policy) {
    _inet6ImportPolicies =
        ImmutableList.<String>builder().addAll(_inet6ImportPolicies).add(policy).build();
  }

  public @Nonnull List<String> getInet6ImportPolicies() {
    return _inet6ImportPolicies;
  }

  public void addInet6ResolutionRib(String resolutionRib) {
    _inet6ResolutionRibs =
        ImmutableList.<String>builder().addAll(_inet6ResolutionRibs).add(resolutionRib).build();
  }

  public @Nonnull List<String> getInet6ResolutionRibs() {
    return _inet6ResolutionRibs;
  }

  public void addInetImportPolicy(String policy) {
    _inetImportPolicies =
        ImmutableList.<String>builder().addAll(_inetImportPolicies).add(policy).build();
  }

  public @Nonnull List<String> getInetImportPolicies() {
    return _inetImportPolicies;
  }

  public void addInetResolutionRib(String resolutionRib) {
    _inetResolutionRibs =
        ImmutableList.<String>builder().addAll(_inetResolutionRibs).add(resolutionRib).build();
  }

  public @Nonnull List<String> getInetResolutionRibs() {
    return _inetResolutionRibs;
  }

  public void addIsoImportPolicy(String policy) {
    _isoImportPolicies =
        ImmutableList.<String>builder().addAll(_isoImportPolicies).add(policy).build();
  }

  public @Nonnull List<String> getIsoImportPolicies() {
    return _isoImportPolicies;
  }

  public void addIsoResolutionRib(String resolutionRib) {
    _isoResolutionRibs =
        ImmutableList.<String>builder().addAll(_isoResolutionRibs).add(resolutionRib).build();
  }

  public @Nonnull List<String> getIsoResolutionRibs() {
    return _isoResolutionRibs;
  }

  public void addResolutionRib(String resolutionRib) {
    _resolutionRibs =
        ImmutableList.<String>builder().addAll(_resolutionRibs).add(resolutionRib).build();
  }

  /** Returns the RIBs used for next-hop resolution, in lookup order. */
  public @Nonnull List<String> getResolutionRibs() {
    return _resolutionRibs;
  }

  private @Nonnull List<String> _importPolicies;
  private @Nonnull List<String> _inet6ImportPolicies;
  private @Nonnull List<String> _inet6ResolutionRibs;
  private @Nonnull List<String> _inetImportPolicies;
  private @Nonnull List<String> _inetResolutionRibs;
  private @Nonnull List<String> _isoImportPolicies;
  private @Nonnull List<String> _isoResolutionRibs;
  private final @Nonnull String _name;
  private @Nonnull List<String> _resolutionRibs;
}
