package org.batfish.representation.cisco;

import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.Prefix;

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

  /**
   * @return A {@link Prefix} which matches this network object, or null if this object is not
   *     supported or cannot be described by a {@link Prefix}
   */
  Prefix getPrefix();

  void setInfo(NetworkObjectInfo info);

  /**
   * @return An {@link IpSpace} which matches this network object, or an EmptyIpSpace if this object
   *     is not supported
   */
  IpSpace toIpSpace();
}
