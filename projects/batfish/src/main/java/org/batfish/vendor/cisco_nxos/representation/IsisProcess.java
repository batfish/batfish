package org.batfish.vendor.cisco_nxos.representation;

import static org.batfish.vendor.cisco_nxos.representation.CiscoNxosConfiguration.DEFAULT_VRF_NAME;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents an IS-IS routing process (a single {@code router isis <tag>} instance) in Cisco NX-OS.
 *
 * <p>Per-VRF settings (the {@code config-router} and {@code config-router-vrf} {@code net} / {@code
 * is-type}) live in {@link IsisVrfConfiguration}; the default VRF always exists.
 */
public final class IsisProcess implements Serializable {

  public IsisProcess(String tag) {
    _tag = tag;
    _vrfs = new HashMap<>();
    // The default VRF always exists.
    getOrCreateVrf(DEFAULT_VRF_NAME);
  }

  public @Nonnull String getTag() {
    return _tag;
  }

  /** A read-only map containing the per-VRF IS-IS configuration. */
  public @Nonnull Map<String, IsisVrfConfiguration> getVrfs() {
    return Collections.unmodifiableMap(_vrfs);
  }

  public @Nonnull IsisVrfConfiguration getOrCreateVrf(String vrfName) {
    return _vrfs.computeIfAbsent(vrfName, name -> new IsisVrfConfiguration());
  }

  public @Nullable IsisVrfConfiguration getVrf(String vrfName) {
    return _vrfs.get(vrfName);
  }

  private final @Nonnull String _tag;
  private final @Nonnull Map<String, IsisVrfConfiguration> _vrfs;
}
