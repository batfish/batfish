package org.batfish.datamodel.routing_policy.expr;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.Serializable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.visitors.PrefixSpaceExprVisitor;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "class")
@ParametersAreNonnullByDefault
public interface PrefixSpaceExpr extends Serializable {

  <T, U> T accept(PrefixSpaceExprVisitor<T, U> visitor, U arg);
}
