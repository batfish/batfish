package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.Serializable;
import java.util.Map;
import javax.annotation.Nonnull;
import org.batfish.datamodel.visitors.GenericIp6SpaceVisitor;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "class")
public abstract class Ip6Space implements Comparable<Ip6Space>, Serializable {

  public abstract <R> R accept(GenericIp6SpaceVisitor<R> visitor);

  public final boolean containsIp6(
      @Nonnull Ip6 ip6, @Nonnull Map<String, Ip6Space> namedIp6Spaces) {
    return accept(new Ip6SpaceContainsIp(ip6, namedIp6Spaces));
  }

  @Override
  public final int compareTo(Ip6Space o) {
    if (this == o) {
      return 0;
    }
    int ret;
    ret = getClass().getSimpleName().compareTo(o.getClass().getSimpleName());
    if (ret != 0) {
      return ret;
    }
    return compareSameClass(o);
  }

  /** Return the {@link Ip6Space} of all IPs not in {@code this}. */
  public Ip6Space complement() {
    return AclIp6Space.difference(UniverseIp6Space.INSTANCE, this);
  }

  protected abstract int compareSameClass(Ip6Space o);

  @Override
  public final boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null) {
      return false;
    }
    if (!(getClass() == o.getClass())) {
      return false;
    }
    return exprEquals(o);
  }

  protected abstract boolean exprEquals(Object o);

  @Override
  public abstract int hashCode();

  @Override
  public abstract @Nonnull String toString();
}
