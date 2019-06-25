package org.batfish.datamodel.routing_policy.expr;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.Serializable;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.routing_policy.Environment;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "class")
public abstract class NextHopExpr implements Serializable {

  @Override
  public abstract boolean equals(Object obj);

  /**
   * Whether to ignore the next-hop-ip and install as a discard route. Should only be true in
   * context of import policy, as discard is non-transitive.
   */
  @JsonIgnore
  public boolean getDiscard() {
    return false;
  }

  @Nullable
  public abstract Ip getNextHopIp(Environment environment);

  @Override
  public abstract int hashCode();
}
