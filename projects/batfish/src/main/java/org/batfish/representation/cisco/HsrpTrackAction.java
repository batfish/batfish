package org.batfish.representation.cisco;

import java.io.Serializable;
import javax.annotation.ParametersAreNonnullByDefault;

/** An action to take on HSRP when a track fails. */
@ParametersAreNonnullByDefault
public interface HsrpTrackAction extends Serializable {}
