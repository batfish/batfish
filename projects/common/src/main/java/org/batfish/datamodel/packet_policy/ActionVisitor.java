package org.batfish.datamodel.packet_policy;

/** Visitor for packet policy {@link Action actions} */
public interface ActionVisitor<T> {

  default T visit(Action action) {
    return action.accept(this);
  }

  T visitDrop(Drop drop);

  T visitFibLookup(FibLookup fibLookup);

  T visitFibLookupOverrideLookupIp(FibLookupOverrideLookupIp fibLookup);
}
