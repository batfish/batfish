package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.visitors.GenericIpSpaceVisitor;

/** A representation of a set of {@link Ip} addresses. */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "class")
public abstract class IpSpace implements Comparable<IpSpace>, Serializable {
  static final String PROP_TRACE_ELEMENT = "traceElement";

  private final @Nullable TraceElement _traceElement;

  IpSpace(@Nullable TraceElement traceElement) {
    _traceElement = traceElement;
  }

  IpSpace() {
    this(null);
  }

  @JsonProperty(PROP_TRACE_ELEMENT)
  public @Nullable TraceElement getTraceElement() {
    return _traceElement;
  }

  public abstract <R> R accept(GenericIpSpaceVisitor<R> visitor);

  public abstract boolean containsIp(@Nonnull Ip ip, @Nonnull Map<String, IpSpace> namedIpSpaces);

  /** Return the {@link IpSpace} of all IPs not in {@code this}. */
  public final IpSpace complement() {
    if (this == UniverseIpSpace.INSTANCE) {
      return EmptyIpSpace.INSTANCE;
    }
    if (this == EmptyIpSpace.INSTANCE) {
      return UniverseIpSpace.INSTANCE;
    }
    return AclIpSpace.difference(UniverseIpSpace.INSTANCE, this);
  }

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
    ret =
        Comparator.nullsLast(TraceElement::compareTo)
            .compare(this._traceElement, o.getTraceElement());
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
    IpSpace other = (IpSpace) o;
    return Objects.equals(_traceElement, other.getTraceElement()) && exprEquals(o);
  }

  protected abstract boolean exprEquals(Object o);

  @Override
  public abstract int hashCode();

  @Override
  public abstract String toString();
}
