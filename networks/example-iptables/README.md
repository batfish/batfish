# Introduction

This is a simple network comprising two separate zones zone1 and 
zone2, each having two hosts and a firewall. There is a core backbone
of routers all running ospf, one area. There is also an Internet 
host. The firewalls are overlay ospf routers with iptables. These allow
 similar functionality to cisco ACLs.