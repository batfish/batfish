# Engine

NOCFlow's per-tenant what-if engine: Batfish embedded as a library behind a small
HTTP API. The contract is [`openapi.yaml`](openapi.yaml).

- `SnapshotBuilder` writes each device's config text with its format pinned, the
  layer-1 topology and the observed environment, and loads them through Batfish's
  normal parse and storage path. What Batfish could not model becomes coverage
  holes per device and fact family.
- `Lifter` produces each device's neutral facts, including a readable routing
  policy view taken from the parser's defined structures.
- `Deriver` accepts complete candidate config text, or legacy config lines for
  explicitly validated operations. Complete configs replace only the changed
  device's text; other inputs remain pinned. Holes the change introduces make
  its verdict unknown. Appending CLI is not a general device configuration editor.
- `ModelInjector` injects self-built models (no config text) through storage, so
  load-time post-processing still runs.

## Build, test, run

```
bazel test //projects/engine:engine_tests
bazel run //projects/engine:update_goldens     # after an intended change; review the diff
bazel build //projects/engine:engine_main_deploy.jar
ENGINE_PORT=9800 java -jar bazel-bin/projects/engine/engine_main_deploy.jar
```

## Dialects

| Dialect | Input | Notes |
| --- | --- | --- |
| `ARISTA_EOS`, `CISCO_IOS`, `CISCO_NXOS` | running config | |
| `JUNOS` | `show configuration \| display set` | set form, so proposed `set`/`delete` lines append |
| `FRR_LINUX` | `vtysh -c "show running-config"` plus kernel interfaces | composed into Batfish's Cumulus concatenated format; standalone FRR is not parsed by Batfish |

Known FRR gaps, reported rather than hidden: the default `bgp ebgp-requires-policy`,
`neighbor ... shutdown`, and `no route-map ...` are not modeled by Batfish's FRR grammar.

## Portable analysis inputs

`GET /snapshots/{id}/export` returns a standard Batfish ZIP with `snapshot/configs/`
and `snapshot/batfish/` inputs. It reads the exact files analyzed, including FRR
composition and runtime state; it does not regenerate them from current evidence.
`X-Snapshot-SHA256` identifies the returned bytes. Export baseline and candidate
before snapshot deletion and retain them with question parameters and the exact
engine build. This endpoint does not persist the in-memory snapshot/job registry.

API 1.1.0 accepts `{device, configText}` as an alternative to `{device, lines}` in
derive requests. Supply exactly one form. Replacement text is a full native
configuration, not a command delta; deployment commands belong to the caller's
reviewed proposal. Linux interfaces with `up: false` are modeled line-down even
when not separately listed in `environment.interfacesDown`.

## Packet paths and ACL evidence

API 1.5.0 exposes Batfish's `testFilters` question alongside `traceroute`.
Traceroute `Filter` steps identify the device, filter, direction, input interface
and packet at that step, with `PERMITTED` or `DENIED` actions. They do not contain
a matching ACL line. Query `testFilters` with exact quoted `nodes` and `filters`,
an explicit `startLocation`, and complete packet headers to obtain `Line_Content`
and the structured ACL `Trace`. `Line_Content` is a vendor line name when
available, otherwise `line:<zero-based index>`; `no-match` means implicit deny.

An interface location (`"node"["interface"]`) originates traffic in that
interface's VRF. An interface-link location (`@enter("node"["interface"])`)
injects traffic into the interface and applies ingress filters. Use the filter
step's packet and input interface, including any prior NAT transformation, when
explaining a path. Pin source/destination addresses and ports, protocol, DSCP,
ECN, packet length, fragment offset and protocol flags/type/code as applicable.
Batfish selects one representative packet for unspecified fields or ranges;
compare returned packet fields and ingress context before joining its matched
line to a traceroute step. These answers model packet forwarding and ACL policy,
not a listening application or a completed TCP handshake.

Internal snapshot identities are unique across engine restarts. API 1.5.1 fixes
reuse of persisted models when restarting with the same `ENGINE_STORAGE`;
refresh snapshots created by earlier versions before relying on their results.

## Retention and storage ownership

The standalone engine exclusively owns its dedicated ENGINE_STORAGE directory
and holds an OS file lock until shutdown. Stop older engine versions before
upgrading: versions predating this lock do not participate in ownership checks.
The snapshot/job registry is process-local. On startup, after acquiring the
lock, the engine reclaims the previous process's networks/tenant/snapshots cache.
Other networks and unrelated files are preserved; symlinked cache directories
are refused. Nocflow must rehydrate snapshots from its retained revisions.

ENGINE_MAX_SNAPSHOTS bounds retained snapshots and jobs (default 16).
Completed/failed snapshots are evicted in least-recently-used order, reclaiming
their compiled models, inputs, dataplanes and job records. Use at least two slots
for baseline/candidate comparisons. Evicted IDs return 404 and can be rebuilt
from retained inputs. Creation/derivation returns 429 when all slots are running
or the only possible victim is the requested base. Queries and reclamation are
serialized to prevent deleting a snapshot while it is being answered.
A cleanup failure retains the internal identity for retry through DELETE and
marks any partially deleted model failed, so it cannot answer assurance questions.

API 1.6.0 introduces bounded retention, exclusive cache ownership, retryable
cleanup, and conservative forwarding-policy coverage. Refresh older models
after deploying this version so previous coverage classifications are retired.
