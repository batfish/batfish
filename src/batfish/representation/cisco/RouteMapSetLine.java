package batfish.representation.cisco;

import batfish.representation.Configuration;
import batfish.representation.PolicyMapSetLine;

public abstract class RouteMapSetLine {

	public abstract PolicyMapSetLine toPolicyMapSetLine(Configuration c);

}
