package org.batfish.datamodel.pojo;

import java.util.List;

public class Aggregate extends BfObject {

  public enum AggregateType {
    AWS,
    REGION,
    VPC,
    SUBNET
  }

  List<BfObject> _contents;

  String _name;

  AggregateType _type;

  public Aggregate(String name, AggregateType type) {
    super("agg-" + name);
    _name = name;
    _type = type;
  }
}
