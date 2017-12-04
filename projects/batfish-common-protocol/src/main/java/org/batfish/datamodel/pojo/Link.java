package org.batfish.datamodel.pojo;

public class Link extends BfObject {

  public enum LinkType {
    PHYISCAL,
    UNKNOWN,
    VIRTUAL,
  }

  BfObject _dst;

  BfObject _src;

  LinkType _type;

  public Link(BfObject src, BfObject dst, LinkType type) {
    super("link-" + src.getId() + "-" + dst.getId());
    _src = src;
    _dst = dst;
    _type = type;
  }
}
