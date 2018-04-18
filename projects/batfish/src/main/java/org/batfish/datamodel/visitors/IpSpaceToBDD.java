package org.batfish.datamodel.visitors;

import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.JFactory;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.IpWildcardSetIpSpace;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.UniverseIpSpace;
import org.batfish.symbolic.bdd.BDDInteger;

public class IpSpaceToBDD implements GenericIpSpaceVisitor<BDDInteger> {
  public static IpSpaceToBDD INSTANCE = new IpSpaceToBDD();

  private static BDDFactory factory;

  static {
    factory = JFactory.init(10000, 1000);
    factory.disableReorder();
    factory.setCacheRatio(64);
  }

  private IpSpaceToBDD() {
  }

  @Override public BDD castToGenericIpSpaceVisitorReturnType(Object o) {
    return (BDD)o;
  }

  @Override public BDD visitAclIpSpace(AclIpSpace aclIpSpace) {
    return null;
  }

  @Override public BDD visitEmptyIpSpace(EmptyIpSpace emptyIpSpace) {
    factory.zero()
  }

  @Override public BDD visitIp(Ip ip) {
    return null;
  }

  @Override public BDD visitIpWildcard(IpWildcard ipWildcard) {
    return null;
  }

  @Override public BDD visitIpWildcardSetIpSpace(IpWildcardSetIpSpace ipWildcardSetIpSpace) {
    return null;
  }

  @Override public BDD visitPrefix(Prefix prefix) {
    return null;
  }

  @Override public BDD visitUniverseIpSpace(UniverseIpSpace universeIpSpace) {
    return null;
  }
}
