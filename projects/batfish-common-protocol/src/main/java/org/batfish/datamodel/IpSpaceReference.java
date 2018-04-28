package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.util.Map;
import javax.annotation.Nonnull;
import org.batfish.datamodel.visitors.GenericIpSpaceVisitor;

public class IpSpaceReference extends IpSpace {

  private static final String PROP_NAME = "name";

  /** */
  private static final long serialVersionUID = 1L;

  private final String _name;

  /** A reference to a named {@link IpSpace} */
  @JsonCreator
  public IpSpaceReference(@JsonProperty(PROP_NAME) @Nonnull String name) {
    _name = name;
  }

  @Override
  public <R> R accept(GenericIpSpaceVisitor<R> visitor) {
    return visitor.visitIpSpaceReference(this);
  }

  @Override
  protected int compareSameClass(IpSpace o) {
    return _name.compareTo(((IpSpaceReference) o)._name);
  }

  @Override
  public IpSpace complement() {
    return AclIpSpace.rejecting(this).thenPermitting(UniverseIpSpace.INSTANCE).build();
  }

  @Override
  public boolean containsIp(Ip ip, Map<String, IpSpace> namedIpSpaces) {
    return namedIpSpaces.get(_name).containsIp(ip, namedIpSpaces);
  }

  @Override
  protected boolean exprEquals(Object o) {
    return _name.equals(((IpSpaceReference) o)._name);
  }

  @JsonProperty(PROP_NAME)
  public String getName() {
    return _name;
  }

  @Override
  public int hashCode() {
    return _name.hashCode();
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(getClass()).add(PROP_NAME, _name).toString();
  }
}
