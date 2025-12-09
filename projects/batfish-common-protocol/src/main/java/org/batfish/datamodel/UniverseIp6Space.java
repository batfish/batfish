package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.io.ObjectStreamException;
import java.io.Serial;
import javax.annotation.Nonnull;
import org.batfish.datamodel.visitors.GenericIp6SpaceVisitor;

/** An {@link Ip6Space} that contains all IPv6 addresses. */
public class UniverseIp6Space extends Ip6Space {

  public static final UniverseIp6Space INSTANCE = new UniverseIp6Space();

  private UniverseIp6Space() {}

  @Override
  public <R> R accept(GenericIp6SpaceVisitor<R> ip6SpaceVisitor) {
    return ip6SpaceVisitor.visitUniverseIp6Space(this);
  }

  @Override
  protected int compareSameClass(Ip6Space o) {
    return 0;
  }

  @Override
  public Ip6Space complement() {
    return EmptyIp6Space.INSTANCE;
  }

  @Override
  protected boolean exprEquals(Object o) {
    return true;
  }

  @Override
  public int hashCode() {
    return getClass().getCanonicalName().hashCode();
  }

  @Override
  public @Nonnull String toString() {
    return "universe";
  }

  ///////// Ensure that instances are interned.

  @JsonCreator
  private static UniverseIp6Space jsonCreator() {
    return INSTANCE;
  }

  /** Cache after deserialization. */
  @Serial
  private Object readResolve() throws ObjectStreamException {
    return INSTANCE;
  }
}
