package jdd.util;

/** interface for an object that gives "names" to each node in a *DD tree */
public interface NodeName {
  String zero();

  String one();

  String zeroShort();

  String oneShort();

  String variable(int n);
}
