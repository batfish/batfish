package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.Serializable;
import java.util.Map;
import javax.annotation.Nonnull;
import org.batfish.datamodel.visitors.GenericIpSpaceVisitor;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "class")
public abstract class IpSpace implements Comparable<IpSpace>, Serializable {

  /** */
  private static final long serialVersionUID = 1L;

  public abstract <R> R accept(GenericIpSpaceVisitor<R> visitor);

  public abstract boolean containsIp(@Nonnull Ip ip, @Nonnull Map<String, IpSpace> namedIpSpaces);

  public abstract IpSpace complement();

  @Override
  public final int compareTo(IpSpace o) {
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

  protected abstract int compareSameClass(IpSpace o);

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
  public abstract String toString();
}
