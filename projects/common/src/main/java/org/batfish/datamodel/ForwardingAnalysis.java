package org.batfish.datamodel;

import java.io.Serializable;
import java.util.Map;
import javax.annotation.Nonnull;

public interface ForwardingAnalysis extends Serializable {
  /** Mapping: hostname -&gt; inInterface -&gt; ipsToArpReplyTo */
  Map<String, Map<String, IpSpace>> getArpReplies();

  /**
   * Return the forwarding behavior for each VRF. Mapping: hostname -&gt; vrfName -&gt; {@link
   * VrfForwardingBehavior}.
   */
  @Nonnull
  Map<String, Map<String, VrfForwardingBehavior>> getVrfForwardingBehavior();
}
