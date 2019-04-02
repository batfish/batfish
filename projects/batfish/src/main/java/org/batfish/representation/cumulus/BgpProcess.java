package org.batfish.representation.cumulus;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;

/** BGP routing process */
public class BgpProcess implements Serializable {

  private static final long serialVersionUID = 1L;

  private final @Nonnull BgpVrf _defaultVrf;
  private final @Nonnull Map<String, BgpVrf> _vrfs;

  public BgpProcess() {
    _defaultVrf = new BgpVrf("");
    _vrfs = new HashMap<>();
  }

  public @Nonnull BgpVrf getDefaultVrf() {
    return _defaultVrf;
  }

  public @Nonnull Map<String, BgpVrf> getVrfs() {
    return _vrfs;
  }
}
