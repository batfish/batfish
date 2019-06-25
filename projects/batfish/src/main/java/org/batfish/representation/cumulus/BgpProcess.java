package org.batfish.representation.cumulus;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Ip;

/** BGP routing process */
public class BgpProcess implements Serializable {

  public static final Ip BGP_UNNUMBERED_IP = Ip.parse("169.254.0.1");

  private final @Nonnull BgpVrf _defaultVrf;
  private final @Nonnull Map<String, BgpVrf> _vrfs;

  public BgpProcess() {
    _defaultVrf = new BgpVrf(Configuration.DEFAULT_VRF_NAME);
    _vrfs = new HashMap<>();
  }

  public @Nonnull BgpVrf getDefaultVrf() {
    return _defaultVrf;
  }

  public @Nonnull Map<String, BgpVrf> getVrfs() {
    return _vrfs;
  }
}
