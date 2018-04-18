package org.batfish.datamodel;

import javax.annotation.Nonnull;
import org.batfish.datamodel.visitors.GenericIpSpaceVisitor;

public interface IpSpace {

  <R> R accept(GenericIpSpaceVisitor<R> visitor);

  boolean containsIp(@Nonnull Ip ip);

  IpSpace complement();

  boolean intersects(@Nonnull IpWildcard ipWildcard);

  boolean intersects(@Nonnull Prefix prefix);
}
