Leaf show route
```
root@LEAF-01> show route table bgp.evpn.0

bgp.evpn.0: 2 destinations, 2 routes (2 active, 0 holddown, 0 hidden)
+ = Active Route, - = Last Active, * = Both

5:172.31.255.53:1006::0::10.14.18.0::23/248
                   *[EVPN/170] 00:20:47
                       Fictitious
5:172.31.255.60:1006::0::8.8.8.8::32/248
                   *[BGP/170] 00:16:46, localpref 100, from 172.31.255.0
                      AS path: I, validation-state: unverified
                    >  to 172.31.254.42 via ge-0/0/0.0

root@LEAF-01> show route table Campus.evpn.0

Campus.evpn.0: 2 destinations, 2 routes (2 active, 0 holddown, 0 hidden)
+ = Active Route, - = Last Active, * = Both

5:172.31.255.53:1006::0::10.14.18.0::23/248
                   *[EVPN/170] 00:21:24
                       Fictitious
5:172.31.255.60:1006::0::8.8.8.8::32/248
                   *[BGP/170] 00:17:23, localpref 100, from 172.31.255.0
                      AS path: I, validation-state: unverified
                    >  to 172.31.254.42 via ge-0/0/0.0

root@LEAF-01> show route table Campus.inet.0

Campus.inet.0: 3 destinations, 3 routes (3 active, 0 holddown, 0 hidden)
+ = Active Route, - = Last Active, * = Both

8.8.8.8/32         *[EVPN/170] 00:17:56
                    >  to 172.31.254.42 via ge-0/0/0.0
10.14.18.0/23      *[Direct/0] 00:21:57
                    >  via ge-0/0/2.0
10.14.18.1/32      *[Local/0] 00:21:57
                       Local via ge-0/0/2.0

root@LEAF-01> show route table bgp.evpn.0 match-prefix 5:* detail

bgp.evpn.0: 2 destinations, 2 routes (2 active, 0 holddown, 0 hidden)
5:172.31.255.53:1006::0::10.14.18.0::23/248 (1 entry, 1 announced)
        *EVPN   Preference: 170
                Next hop type: Fictitious, Next hop index: 0
                Address: 0x91b9494
                Next-hop reference count: 2
                Kernel Table Id: 0
                Next hop:
                State: <Secondary Active Int Ext>
                Age: 22:11
                Validation State: unverified
                Task: Campus-EVPN-L3-context
                Announcement bits (1): 0-BGP_RT_Background
                AS path: I
                Communities: target:10755:1006 encapsulation:vxlan(0x8) router-mac:2c:6b:f5:e9:d6:f0
                Route Label: 1006
                Overlay gateway address: 0.0.0.0
                ESI 00:00:00:00:00:00:00:00:00:00
                Primary Routing Table: Campus.evpn.0
                Thread: junos-main

5:172.31.255.60:1006::0::8.8.8.8::32/248 (1 entry, 0 announced)
        *BGP    Preference: 170/-101
                Route Distinguisher: 172.31.255.60:1006
                Next hop type: Indirect, Next hop index: 0
                Address: 0x91ba794
                Next-hop reference count: 2
                Kernel Table Id: 0
                Source: 172.31.255.0
                Protocol next hop: 172.31.255.60
                Indirect next hop: 0x2 no-forward INH Session ID: 0, INH non-key opaque: 0x0, INH key opaque: 0x0
                State: <Active Int Ext>
                Local AS: 65100 Peer AS: 65100
                Age: 18:10      Metric2: 0
                Validation State: unverified
                Task: BGP_65100.172.31.255.0
                AS path: I  (Originator)
                Cluster list:  172.31.255.0
                Originator ID: 172.31.255.60
                Communities: target:10755:1006 encapsulation:vxlan(0x8) router-mac:2c:6b:f5:60:71:f0
                Import Accepted
                Route Label: 1006
                Overlay gateway address: 0.0.0.0
                ESI 00:00:00:00:00:00:00:00:00:00
                Localpref: 100
                Router ID: 172.31.255.0
                Secondary Tables: Campus.evpn.0
                Thread: junos-main
```

Spine show routes
```
root@SPINE-01> show route table bgp.evpn.0

bgp.evpn.0: 2 destinations, 2 routes (2 active, 0 holddown, 0 hidden)
+ = Active Route, - = Last Active, * = Both

5:172.31.255.53:1006::0::10.14.18.0::23/248
                   *[BGP/170] 00:22:10, localpref 100, from 172.31.255.53
                      AS path: I, validation-state: unverified
                    >  to 172.31.254.43 via ge-0/0/0.0
5:172.31.255.60:1006::0::8.8.8.8::32/248
                   *[BGP/170] 00:19:30, localpref 100, from 172.31.255.60
                      AS path: I, validation-state: unverified
                    >  to 172.31.254.91 via ge-0/0/1.0

root@SPINE-01> show route table inet.0

inet.0: 7 destinations, 7 routes (7 active, 0 holddown, 0 hidden)
+ = Active Route, - = Last Active, * = Both

172.31.254.42/31   *[Direct/0] 00:22:40
                    >  via ge-0/0/0.0
172.31.254.42/32   *[Local/0] 00:22:40
                       Local via ge-0/0/0.0
172.31.254.90/31   *[Direct/0] 00:22:40
                    >  via ge-0/0/1.0
172.31.254.90/32   *[Local/0] 00:22:40
                       Local via ge-0/0/1.0
172.31.255.0/32    *[Direct/0] 00:22:40
                    >  via lo0.0
172.31.255.53/32   *[BGP/170] 00:22:38, localpref 100
                      AS path: 65001 I, validation-state: unverified
                    >  to 172.31.254.43 via ge-0/0/0.0
172.31.255.60/32   *[BGP/170] 00:19:54, localpref 100
                      AS path: 65002 I, validation-state: unverified
                    >  to 172.31.254.91 via ge-0/0/1.0

root@SPINE-01> show route table bgp.evpn.0 match-prefix 5:* detail

bgp.evpn.0: 2 destinations, 2 routes (2 active, 0 holddown, 0 hidden)
5:172.31.255.53:1006::0::10.14.18.0::23/248 (1 entry, 1 announced)
        *BGP    Preference: 170/-101
                Route Distinguisher: 172.31.255.53:1006
                Next hop type: Indirect, Next hop index: 0
                Address: 0x91bdd14
                Next-hop reference count: 1
                Kernel Table Id: 0
                Source: 172.31.255.53
                Protocol next hop: 172.31.255.53
                Indirect next hop: 0x2 no-forward INH Session ID: 0, INH non-key opaque: 0x0, INH key opaque: 0x0
                State: <Active Int Ext>
                Local AS: 65100 Peer AS: 65100
                Age: 22:53      Metric2: 0
                Validation State: unverified
                Task: BGP_65100.172.31.255.53
                Announcement bits (1): 0-BGP_RT_Background
                AS path: I
                Communities: target:10755:1006 encapsulation:vxlan(0x8) router-mac:2c:6b:f5:e9:d6:f0
                Accepted
                Route Label: 1006
                Overlay gateway address: 0.0.0.0
                ESI 00:00:00:00:00:00:00:00:00:00
                Localpref: 100
                Router ID: 172.31.255.53
                Thread: junos-main

5:172.31.255.60:1006::0::8.8.8.8::32/248 (1 entry, 1 announced)
        *BGP    Preference: 170/-101
                Route Distinguisher: 172.31.255.60:1006
                Next hop type: Indirect, Next hop index: 0
                Address: 0x91bd714
                Next-hop reference count: 1
                Kernel Table Id: 0
                Source: 172.31.255.60
                Protocol next hop: 172.31.255.60
                Indirect next hop: 0x2 no-forward INH Session ID: 0, INH non-key opaque: 0x0, INH key opaque: 0x0
                State: <Active Int Ext>
                Local AS: 65100 Peer AS: 65100
                Age: 20:13      Metric2: 0
                Validation State: unverified
                Task: BGP_65100.172.31.255.60
                Announcement bits (1): 0-BGP_RT_Background
                AS path: I
                Communities: target:10755:1006 encapsulation:vxlan(0x8) router-mac:2c:6b:f5:60:71:f0
                Accepted
                Route Label: 1006
                Overlay gateway address: 0.0.0.0
                ESI 00:00:00:00:00:00:00:00:00:00
                Localpref: 100
                Router ID: 172.31.255.60
                Thread: junos-main
```

Core Show Routes
```
root@CORE-01> show route table bgp.evpn.0

bgp.evpn.0: 2 destinations, 2 routes (2 active, 0 holddown, 0 hidden)
+ = Active Route, - = Last Active, * = Both

5:172.31.255.53:1006::0::10.14.18.0::23/248
                   *[BGP/170] 00:20:30, localpref 100, from 172.31.255.0
                      AS path: I, validation-state: unverified
                    >  to 172.31.254.90 via ge-0/0/1.0
5:172.31.255.60:1006::0::8.8.8.8::32/248
                   *[EVPN/170] 00:20:36
                       Fictitious

root@CORE-01> show route table ?
Possible completions:
  <table>              Name of routing table
  Campus.evpn.0
  Campus.inet.0
  bgp.evpn.0
  inet.0
  inet6.0
root@CORE-01> show route table Campus.evpn.0

Campus.evpn.0: 2 destinations, 2 routes (2 active, 0 holddown, 0 hidden)
+ = Active Route, - = Last Active, * = Both

5:172.31.255.53:1006::0::10.14.18.0::23/248
                   *[BGP/170] 00:20:52, localpref 100, from 172.31.255.0
                      AS path: I, validation-state: unverified
                    >  to 172.31.254.90 via ge-0/0/1.0
5:172.31.255.60:1006::0::8.8.8.8::32/248
                   *[EVPN/170] 00:20:58
                       Fictitious

root@CORE-01> show route table Campus.inet.0

Campus.inet.0: 2 destinations, 2 routes (2 active, 0 holddown, 0 hidden)
+ = Active Route, - = Last Active, * = Both

8.8.8.8/32         *[Direct/0] 00:21:08
                    >  via lo0.100
10.14.18.0/23      *[EVPN/170] 00:21:02
                    >  to 172.31.254.90 via ge-0/0/1.0

root@CORE-01> show route table bgp.evpn.0 match-prefix 5:* detail

bgp.evpn.0: 2 destinations, 2 routes (2 active, 0 holddown, 0 hidden)
5:172.31.255.53:1006::0::10.14.18.0::23/248 (1 entry, 0 announced)
        *BGP    Preference: 170/-101
                Route Distinguisher: 172.31.255.53:1006
                Next hop type: Indirect, Next hop index: 0
                Address: 0x91c3714
                Next-hop reference count: 2
                Kernel Table Id: 0
                Source: 172.31.255.0
                Protocol next hop: 172.31.255.53
                Indirect next hop: 0x2 no-forward INH Session ID: 0, INH non-key opaque: 0x0, INH key opaque: 0x0
                State: <Active Int Ext>
                Local AS: 65100 Peer AS: 65100
                Age: 21:08      Metric2: 0
                Validation State: unverified
                Task: BGP_65100.172.31.255.0
                AS path: I  (Originator)
                Cluster list:  172.31.255.0
                Originator ID: 172.31.255.53
                Communities: target:10755:1006 encapsulation:vxlan(0x8) router-mac:2c:6b:f5:e9:d6:f0
                Import Accepted
                Route Label: 1006
                Overlay gateway address: 0.0.0.0
                ESI 00:00:00:00:00:00:00:00:00:00
                Localpref: 100
                Router ID: 172.31.255.0
                Secondary Tables: Campus.evpn.0
                Thread: junos-main

5:172.31.255.60:1006::0::8.8.8.8::32/248 (1 entry, 1 announced)
        *EVPN   Preference: 170
                Next hop type: Fictitious, Next hop index: 0
                Address: 0x91b9494
                Next-hop reference count: 2
                Kernel Table Id: 0
                Next hop:
                State: <Secondary Active Int Ext>
                Age: 21:14
                Validation State: unverified
                Task: Campus-EVPN-L3-context
                Announcement bits (1): 0-BGP_RT_Background
                AS path: I
                Communities: target:10755:1006 encapsulation:vxlan(0x8) router-mac:2c:6b:f5:60:71:f0
                Route Label: 1006
                Overlay gateway address: 0.0.0.0
                ESI 00:00:00:00:00:00:00:00:00:00
                Primary Routing Table: Campus.evpn.0
                Thread: junos-main
```

pybatfish tests
```
headers = HeaderConstraints(dstIps='8.8.8.8', srcIps='10.14.18.10', applications='DNS')
bf.q.traceroute(startLocation="@enter(leaf-01[ge-0/0/2.0])", headers=headers).answer().frame()
start=leaf-01 interface=ge-0/0/2.0 [10.14.18.10:49152->8.8.8.8:53 UDP]	[((RECEIVED(ge-0/0/2.0), NO_ROUTE(Discarded)))]	1
