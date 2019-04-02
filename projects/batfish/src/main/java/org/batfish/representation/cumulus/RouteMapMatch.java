package org.batfish.representation.cumulus;

import java.io.Serializable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * A condition contained by a route-map entry which a route must match for the entry to be applied
 * to the route.
 */
@ParametersAreNonnullByDefault
public interface RouteMapMatch extends Serializable {}
