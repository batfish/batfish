# bgpSessionStatus and bgpSessionCompatibility

## Overview

`bgpSessionStatus` and `bgpSessionCompatibility` are companion questions that analyze BGP peering configurations. Both questions share the same implementation package and have similar structure, but answer fundamentally different questions:

- **bgpSessionCompatibility**: Analyzes configuration only. Determines whether two BGP peers are *configured* compatibly (matching AS numbers, IP addresses, etc.). Does not consider reachability.

- **bgpSessionStatus**: Analyzes configuration and dataplane. Determines whether compatible sessions will actually *establish* by checking IP reachability via traceroute.

**Location**: `projects/question/src/main/java/org/batfish/question/bgpsessionstatus/`

**Question definitions**:
- `questions/experimental/bgpSessionStatus.json`
- `questions/experimental/bgpSessionCompatibility.json`

## Why These Questions Exist

BGP session failures are a common cause of network outages. Diagnosing them requires checking multiple conditions:

1. **Local configuration validity**: Does the peer have all required settings (local AS, remote AS, local IP, remote IP)?
2. **Remote peer existence**: Is there a matching peer configuration on another device?
3. **Configuration compatibility**: Do the local and remote configurations agree on AS numbers, IP addresses, etc.?
4. **Network reachability**: Can the peers actually reach each other through the network?

These questions provide two levels of analysis:

- **Compatibility** catches configuration errors before computing the dataplane, enabling fast feedback during configuration review.
- **Status** catches reachability issues (ACLs blocking TCP port 179, routing loops, etc.) that only manifest after dataplane computation.

## How It Works

### BGP Topology Construction

Both questions rely on `BgpTopologyUtils.initBgpTopology()` to build a graph of BGP peer relationships. The process:

1. **Enumerate peers**: Scan all configurations for BGP processes and their neighbor definitions (active, passive/dynamic, and unnumbered).

2. **Validate local configuration**: Check that each peer has required settings. Invalid peers may be kept (for compatibility analysis) or filtered (for dataplane computation).

3. **Find compatible remotes**: For each peer, search for potential remote peers based on:
   - IP address matching (active peers must have matching local/remote IPs)
   - AS number compatibility (local AS must be in remote's allowed remote ASNs, and vice versa)
   - Layer-3 adjacency (unnumbered peers must be on directly connected interfaces)

4. **Build edges**: Create directed edges between compatible peer pairs, storing negotiated `BgpSessionProperties` on each edge.

### Compatibility vs Status: The Key Difference

**BgpSessionCompatibilityAnswerer** uses only the "configured topology":
- Calls `initBgpTopology()` with `keepInvalid=true` and `checkReachability=false`
- Reports configuration issues without checking network reachability
- Fast: no dataplane computation required

**BgpSessionStatusAnswerer** uses both configured and established topologies:
- Configured topology: same as compatibility, identifies potential sessions
- Established topology: obtained from `TopologyProvider.getBgpTopology()`, which uses traceroute to verify TCP reachability between peers
- A session is ESTABLISHED only if it appears in the established topology

### Session Status Determination

For `bgpSessionStatus`, each peer gets one of three statuses:

| Status | Meaning |
|--------|---------|
| `ESTABLISHED` | Session is compatible AND peers can reach each other |
| `NOT_ESTABLISHED` | Session is compatible but peers cannot reach each other |
| `NOT_COMPATIBLE` | Local or remote configuration is invalid |

For `bgpSessionCompatibility`, statuses describe the configuration state:

| Status | Meaning |
|--------|---------|
| `NO_LOCAL_AS` | Local AS not configured |
| `NO_REMOTE_AS` | Remote AS not configured |
| `NO_LOCAL_IP` | Local IP not configured (eBGP single-hop) |
| `LOCAL_IP_UNKNOWN_STATICALLY` | Local IP not configured (iBGP or eBGP multi-hop) |
| `NO_REMOTE_IP` | Remote IP not configured (active peers) |
| `NO_REMOTE_PREFIX` | Remote prefix not configured (passive peers) |
| `INVALID_LOCAL_IP` | Configured local IP not owned by any interface |
| `UNKNOWN_REMOTE` | Remote IP not present in the network |
| `HALF_OPEN` | Valid configuration but no compatible remote peer found |
| `MULTIPLE_REMOTES` | Multiple compatible remote peers (ambiguous) |
| `UNIQUE_MATCH` | Exactly one compatible remote peer (ideal) |
| `DYNAMIC_MATCH` | Passive peer with at least one compatible active peer |
| `NO_MATCH_FOUND` | Passive peer with no compatible active peers |

### Peer Types

The questions handle three types of BGP peers:

1. **Active peers** (`BgpActivePeerConfig`): Traditional point-to-point BGP with explicit remote IP.

2. **Passive/Dynamic peers** (`BgpPassivePeerConfig`): Accept connections from any peer within a prefix range. Used for route reflector clients or large-scale deployments.

3. **Unnumbered peers** (`BgpUnnumberedPeerConfig`): BGP sessions established over interface link-local addresses. Requires Layer-3 adjacency (point-to-point connection) between the interfaces.

### Reachability Checking

When computing the established topology (used by `bgpSessionStatus`), the dataplane engine:

1. Constructs a TCP SYN flow from initiator to listener on port 179
2. Runs bidirectional traceroute (forward and reverse)
3. Checks that the flow is ACCEPTED at the destination
4. For eBGP single-hop sessions, verifies the path is at most 2 hops
5. Marks the session as established only if both directions succeed

This catches issues like:
- ACLs blocking BGP traffic
- Missing routes to the peer
- NAT breaking the session
- Firewall policies dropping the connection

## Key Classes

| Class | Responsibility |
|-------|---------------|
| `BgpSessionStatusAnswerer` | Compares configured and established topologies to determine session status |
| `BgpSessionCompatibilityAnswerer` | Analyzes configured topology only to find configuration issues |
| `BgpSessionAnswererUtils` | Shared utilities: column definitions, status computation, filtering |
| `BgpSessionQuestion` | Abstract base class defining common parameters (nodes, remoteNodes, status, type) |
| `BgpSessionStatusQuestion` | Status-specific question with `BgpSessionStatus` filtering |
| `BgpSessionCompatibilityQuestion` | Compatibility-specific question with `ConfiguredSessionStatus` filtering |
| `BgpTopologyUtils` | Core topology construction logic (in `projects/common`) |
| `BgpSessionProperties` | Negotiated session properties stored on topology edges |

## Output Schema

### Common Columns (Both Questions)

| Column | Type | Description |
|--------|------|-------------|
| `Node` | Node | The node where this session is configured |
| `VRF` | String | The VRF containing the BGP process |
| `Local_AS` | Long | The local AS number |
| `Local_Interface` | Interface | Local interface (unnumbered peers only) |
| `Local_IP` | IP | The local IP address |
| `Remote_AS` | String | The remote AS or AS range |
| `Remote_Node` | Node | The matched remote node (if found) |
| `Remote_Interface` | Interface | Remote interface (unnumbered peers only) |
| `Remote_IP` | IP or Prefix | Remote IP (active) or prefix (passive) |
| `Address_Families` | Set&lt;String&gt; | Address families negotiated (IPV4_UNICAST, etc.) |
| `Session_Type` | String | IBGP, EBGP_SINGLEHOP, EBGP_MULTIHOP, EBGP_UNNUMBERED, IBGP_UNNUMBERED |

### bgpSessionStatus-Specific Column

| Column | Type | Description |
|--------|------|-------------|
| `Established_Status` | String | ESTABLISHED, NOT_ESTABLISHED, or NOT_COMPATIBLE |

### bgpSessionCompatibility-Specific Column

| Column | Type | Description |
|--------|------|-------------|
| `Configured_Status` | String | One of the ConfiguredSessionStatus values |

## Performance Considerations

- **Compatibility is fast**: No dataplane computation required. Suitable for CI/CD pipelines where quick feedback is needed.

- **Status requires dataplane**: Must compute routes and run traceroute for each potential session. More expensive but catches reachability issues.

- **Parallel topology construction**: `BgpTopologyUtils.initBgpTopology()` processes peers in parallel when building edges (batfish/batfish#7955 pattern).

- **Dynamic peers multiply output**: A passive peer with N compatible active peers generates N rows. Large listen ranges can produce many rows.

## Known Limitations

1. **Session establishment is simulated**: The questions predict whether sessions *would* establish based on configuration and reachability. They do not observe actual BGP state from running devices.

2. **Dynamic local IP inference**: When local IP is not explicitly configured, Batfish infers potential source IPs from the FIB. This may not match the actual IP selection algorithm of all vendors.

3. **No authentication checking**: The questions do not verify that MD5 passwords or other authentication settings match between peers.

4. **Confederation support is complex**: Peers within the same confederation, across confederation boundaries, and external to confederations are all handled, but edge cases may exist (see batfish/batfish#9757).

5. **eBGP single-hop verification**: The 2-hop limit for eBGP single-hop is based on the initiator's configuration. If only the listener is configured as single-hop, the check may not apply correctly.

## Common Sources of Confusion

### "Why is my session NOT_ESTABLISHED when both peers exist?"

The `NOT_ESTABLISHED` status means peers are configured compatibly but cannot reach each other. Common causes:

- An ACL is blocking TCP port 179
- The route to the peer goes through a firewall that drops the connection
- The peer's IP is not routable (no route in the FIB)
- NAT is translating addresses unexpectedly

Use `traceroute` question to debug reachability.

### "Why do I see multiple rows for one BGP neighbor?"

Dynamic (passive) peers generate one row per compatible remote peer. If your listen range matches many active peers, you will see many rows.

Similarly, if an active peer has multiple compatible remotes (MULTIPLE_REMOTES status), this indicates ambiguous configuration that should be resolved.

### "What's the difference between HALF_OPEN and UNKNOWN_REMOTE?"

- `UNKNOWN_REMOTE`: The configured remote IP does not exist anywhere in the network.
- `HALF_OPEN`: The remote IP exists, but the device owning it has no compatible BGP peer configuration.

Both indicate the remote side needs configuration, but UNKNOWN_REMOTE suggests a typo in the IP address.

## Related Questions

- **`bgpEdges`**: Lists established BGP adjacencies (simpler output, less diagnostic info)
- **`bgpRib`**: Shows routes in the BGP RIB (what routes were learned)
- **`bgpProcessConfiguration`**: Shows BGP process-level configuration
- **`bgpPeerConfiguration`**: Shows per-peer BGP configuration details
- **`traceroute`**: Debug reachability issues between BGP peers

## References

- `org.batfish.datamodel.bgp.BgpTopologyUtils` - Core topology construction
- `org.batfish.datamodel.BgpSessionProperties` - Session properties and types
- `org.batfish.datamodel.questions.ConfiguredSessionStatus` - Compatibility status enum
- `org.batfish.datamodel.questions.BgpSessionStatus` - Session status enum
