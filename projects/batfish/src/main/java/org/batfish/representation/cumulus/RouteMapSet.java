package org.batfish.representation.cumulus;

import java.io.Serializable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * A transformation contained by a route-map entry that is applied to any route to which the entry
 * applies.
 */
@ParametersAreNonnullByDefault
public interface RouteMapSet extends Serializable {}
