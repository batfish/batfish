package org.batfish.representation.cisco_asa;

import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpSpace;

public interface NetworkObject {

  String getDescription();

  /**
   * @return The highest (or only) {@link Ip} which matches this network object, or null if this
   *     object is not supported
   */
  Ip getEnd();

  /**
   * @return The lowest (or only) {@link Ip} which matches this network object, or null if this
   *     object is not supported
   */
  Ip getStart();

  String getName();

  void setInfo(NetworkObjectInfo info);

  /**
   * @return An {@link IpSpace} which matches this network object, or an EmptyIpSpace if this object
   *     is not supported
   */
  IpSpace toIpSpace();
}
