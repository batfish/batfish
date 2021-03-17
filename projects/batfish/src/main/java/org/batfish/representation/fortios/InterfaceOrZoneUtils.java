package org.batfish.representation.fortios;

import com.google.common.collect.ImmutableSet;
import java.util.Set;

/** Utility functions for {@link InterfaceOrZone}. */
public class InterfaceOrZoneUtils {
  /** Returns the names of interfaces included in the provided {@link InterfaceOrZone}. */
  public static Set<String> getIncludedInterfaces(InterfaceOrZone interfaceOrZone) {
    return interfaceOrZone.accept(
        new InterfaceOrZoneVisitor<Set<String>>() {
          @Override
          public Set<String> visitInterface(Interface iface) {
            return ImmutableSet.of(iface.getName());
          }

          @Override
          public Set<String> visitZone(Zone zone) {
            return zone.getInterface();
          }
        });
  }

  /** Returns the default {@link Zone.IntrazoneAction} for the provided {@link InterfaceOrZone}. */
  public static Zone.IntrazoneAction getDefaultIntrazoneAction(InterfaceOrZone interfaceOrZone) {
    return interfaceOrZone.accept(
        new InterfaceOrZoneVisitor<Zone.IntrazoneAction>() {
          @Override
          public Zone.IntrazoneAction visitInterface(Interface iface) {
            // TODO Confirm that an unzoned interface does not permit out traffic that originally
            //  entered the same interface, unless directed to do so by a policy
            return Zone.IntrazoneAction.DENY;
          }

          @Override
          public Zone.IntrazoneAction visitZone(Zone zone) {
            return zone.getIntrazoneEffective();
          }
        });
  }

  /** Returns the {@link FortiosStructureType} for the provided {@link InterfaceOrZone}. */
  public static FortiosStructureType getStructureType(InterfaceOrZone interfaceOrZone) {
    return interfaceOrZone.accept(
        new InterfaceOrZoneVisitor<FortiosStructureType>() {
          @Override
          public FortiosStructureType visitInterface(Interface iface) {
            return FortiosStructureType.INTERFACE;
          }

          @Override
          public FortiosStructureType visitZone(Zone zone) {
            return FortiosStructureType.ZONE;
          }
        });
  }

  /** Whether the given {@link Policy} can match traffic from the given {@link InterfaceOrZone}. */
  public static boolean policyMatchesFrom(Policy policy, InterfaceOrZone interfaceOrZone) {
    if (policy.getSrcIntf().contains(Policy.ANY_INTERFACE)) {
      return true;
    }
    return interfaceOrZone.accept(
        new InterfaceOrZoneVisitor<Boolean>() {
          @Override
          public Boolean visitInterface(Interface iface) {
            return policy.getSrcIntf().contains(iface.getName());
          }

          @Override
          public Boolean visitZone(Zone zone) {
            return policy.getSrcIntfZones().contains(zone.getName());
          }
        });
  }

  /** Whether the given {@link Policy} can match traffic to the given {@link InterfaceOrZone}. */
  public static boolean policyMatchesTo(Policy policy, InterfaceOrZone interfaceOrZone) {
    if (policy.getDstIntf().contains(Policy.ANY_INTERFACE)) {
      return true;
    }
    return interfaceOrZone.accept(
        new InterfaceOrZoneVisitor<Boolean>() {
          @Override
          public Boolean visitInterface(Interface iface) {
            return policy.getDstIntf().contains(iface.getName());
          }

          @Override
          public Boolean visitZone(Zone zone) {
            return policy.getDstIntfZones().contains(zone.getName());
          }
        });
  }
}
