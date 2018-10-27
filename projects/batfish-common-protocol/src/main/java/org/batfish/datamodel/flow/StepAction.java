package org.batfish.datamodel.flow;

/** Types of actions which can be taken at the end of a {@link Step} */
public enum StepAction {
  /** Action if the packet gets accepted at an input interface */
  ACCEPTED,
  /** Action if the packet gets delivered to subnet from the final hop */
  DELIVERED_TO_SUBNET,
  /** Action if the packet gets denied at I/P or O/P interface */
  DENIED,
  /** Action if the it is found that the packet will exit the network from the last hop */
  EXITS_NETWORK,
  /** Action when packet gets forwarded from I/P to O/P interface */
  FORWARDED,
  /** Action when information is insufficient to tell the final fate of the packet */
  INSUFFICIENT_INFO,
  /** Action when next neighbor is unreachable at the last hop */
  NEIGHBOR_UNREACHABLE,
  /** Action when no route exists for the given dest IP on a hop */
  NO_ROUTE,
  /** Action when packet will be forwarded to a null interface */
  NULL_ROUTED,
  /** Action to show the origination of a flow in a virtual router */
  ORIGINATED,
  /** Action to show the receiving of a packet at an input interface */
  RECEIVED,
  /** Action to show the transmission of a packet from an output interface */
  TRANSMITTED
}
