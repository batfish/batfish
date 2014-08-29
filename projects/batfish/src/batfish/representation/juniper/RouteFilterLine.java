package batfish.representation.juniper;

import java.io.Serializable;

/**
 * 
 * A data structure used in RouteFilter to store prefix and prefix-length
 *
 */

public abstract class RouteFilterLine implements Serializable {

   private static final long serialVersionUID = 1L;

	public abstract RouteFilterLineType getType();
	
}
