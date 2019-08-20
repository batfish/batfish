package org.batfish.representation.cisco_nxos;

import static org.batfish.representation.cisco_nxos.CiscoNxosConfiguration.DEFAULT_VRF_NAME;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents the EIGRP configuration for a process in Cisco NX-OS.
 *
 * <p>Configuration commands entered at the {@code config-router} level that cannot also be run in a
 * {@code config-router-vrf} level are set in the EIGRP process.
 */
public final class EigrpProcessConfiguration implements Serializable {

  public EigrpProcessConfiguration() {
    _isolate = false; // disabled by default
    _vrfs = new HashMap<>();
    // The default VRF always exists.
    getOrCreateVrf(DEFAULT_VRF_NAME);
  }

  /** A read-only map containing the per-VRF BGP configuration. */
  public Map<String, EigrpVrfConfiguration> getVrfs() {
    return Collections.unmodifiableMap(_vrfs);
  }

  public @Nonnull EigrpVrfConfiguration getOrCreateVrf(String vrfName) {
    return _vrfs.computeIfAbsent(vrfName, name -> new EigrpVrfConfiguration());
  }

  public @Nullable EigrpVrfConfiguration getVrf(String vrfName) {
    return _vrfs.get(vrfName);
  }

  public boolean getIsolate() {
    return _isolate;
  }

  public void setIsolate(boolean isolate) {
    _isolate = isolate;
  }

  ///////////////////////////////////
  // Private implementation details
  ///////////////////////////////////

  private boolean _isolate;
  private final Map<String, EigrpVrfConfiguration> _vrfs;
}
