package org.batfish.datamodel;

import javax.annotation.Nonnull;
import org.batfish.datamodel.visitors.GenericIpSpaceVisitor;

public interface IpSpace {

  <R> R accept(GenericIpSpaceVisitor<R> visitor);

  boolean containsIp(@Nonnull Ip ip);

  IpSpace complement();
}
