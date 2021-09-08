parser grammar CheckPointGateway_static_route;

options {
    tokenVocab = CheckPointGatewayLexer;
}

s_static_route: STATIC_ROUTE static_route_prefix ssr;

static_route_prefix: DEFAULT | ip_prefix;

ssr: ssr_comment | ssr_nexthop | ssr_off;

ssr_comment: COMMENT static_route_comment;

ssr_nexthop: NEXTHOP ssrn;

ssrn
:
    ssrn_blackhole
    | ssrn_gateway
    | ssrn_reject
;

ssrn_blackhole: BLACKHOLE;

ssrn_gateway: GATEWAY ssrng on_or_off;

ssrng: ssrng_address | ssrng_logical;

ssrng_address: ADDRESS ip_address ssrng_priority?;

ssrng_logical: LOGICAL iface = word ssrng_priority?;

ssrng_priority: PRIORITY static_route_priority;

ssrn_reject: REJECT;

ssr_off: OFF;
