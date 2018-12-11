package org.batfish.datamodel.flow;

/** Types of actions which can be taken at the end of a {@link Step} */
public enum StepAction {
  /** Action if the flow gets accepted at a hop */
  ACCEPTED,
  /** Action if the flow gets delivered to subnet from the final hop */
  DELIVERED_TO_SUBNET,
  /** Action if the flow gets denied at I/P or O/P interface */
  DENIED,
  /** Action if the flow will exit the network from the last hop */
  EXITS_NETWORK,
  /** Action if flow gets forwarded from I/P to O/P interface */
  FORWARDED,
  /** Action if information is insufficient to tell the final fate of the flow */
  INSUFFICIENT_INFO,
  /** Action when next neighbor is unreachable at the last hop */
  NEIGHBOR_UNREACHABLE,
  /** Action when no route exists for the given dest IP on a hop */
  NO_ROUTE,
  /** Action when flow will be forwarded to a null interface */
  NULL_ROUTED,
  /** Action to show the origination of a flow in a virtual router */
  ORIGINATED,
  /** Action to show the receiving of a flow at a hop */
  RECEIVED,
  /** Action to show the transmission of a flow from an output interface */
  TRANSMITTED,
  /** Action that a filter permits a packet */
  PERMITTED
}
