parser grammar NetscreenParser;

options {
   superClass = 'org.batfish.grammar.BatfishParser';
   tokenVocab = NetscreenLexer;
}

netscreen_configuration
:
   statement+ EOF
;

port_range
:
    DEC DASH DEC
;

protocol
:
   TCP
   | UDP
;

statement
:
   (
      (
         (
            SET
            | UNSET
         )
         (
            s_address
            | s_group
            | s_hostname
            | s_interface
            | s_null
            | s_policy
            | s_service
            | s_vrouter
            | s_zone
         )
      )
      | EXIT
   ) NEWLINE
;

s_address
:
    ADDRESS zname = variable addname = variable address = IP_ADDRESS netmask = IP_ADDRESS
;

s_group
:
    GROUP ADDRESS zonename = variable groupname = variable
    (
       ADD address = variable
    )?
;

s_hostname
:
    HOSTNAME variable
;

s_interface
:
    INTERFACE variable
    (
       si_ip
       | si_mip
       | si_nat
       | si_null
       | si_route
       | si_zone
    )
;

s_null
:
    (
       CLOCK
       | ADD_DEFAULT_ROUTE
       | AUTH
       | AUTH_SERVER
       | AUTO_ROUTE_EXPORT
       | ADMIN
       | CONFIG
       | CONSOLE
       | IKE
       | IPSEC
       | FLOW
       | NSMGMT
       | NSRP
       | PKI
       | SNMP
       | SSH
       | URL
    ) (~NEWLINE)*
;

s_policy
:
    POLICY ID DEC
    (
       FROM fromzone = variable
       TO tozone = variable
       srcaddress = variable
       dstaddress = variable
       proto = variable
       (
          DENY
          | PERMIT
       )
       LOG?
    )?
;


s_service
:
    SERVICE name = variable
    PROTOCOL proto = protocol
    SRC_PORT src_ports = port_range
    DST_PORT dst_ports = port_range
;

s_vrouter
:
    VROUTER variable SHARABLE?
;


s_zone
:
    ZONE name = variable
    (
       BLOCK
       |
       (
            SCREEN sname = variable
       )
       | TCP_RST
       |
       (
          VROUTER vname = variable
       )
    )
;

si_ip
:
    IP
    (
       IP_PREFIX
       | MANAGEABLE
    )?
;

si_mip
:
    MIP address = IP_ADDRESS
    HOST host = IP_ADDRESS
    NETMASK mast = IP_ADDRESS
    VR variable
;

si_nat
:
    NAT
;

si_null
:
    (
        BYPASS_OTHERS_IPSEC
        | BYPASS_NON_IP
    )
;

si_route
:
    ROUTE
;

si_zone
:
   ZONE variable
;

variable
:
   (
      (
         DOUBLE_QUOTE QUOTED_TEXT DOUBLE_QUOTE
      )
      | VARIABLE
   )
;
