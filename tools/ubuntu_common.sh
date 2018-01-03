#!/usr/bin/env bash

ubuntu_version() {
   head -n1 /etc/issue | cut -f2 -d' ' | cut -f1,2 -d'.'
}

