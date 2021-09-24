package org.batfish.vendor.check_point_management.parsing.parboiled;

import java.io.Serializable;
import javax.annotation.ParametersAreNonnullByDefault;

/** A node that may be placed on the value stack while parsing a service-other match expression. */
@ParametersAreNonnullByDefault
public interface AstNode extends Serializable {}
