#!/usr/bin/env bash
export BATFISH_SOURCED_SCRIPT=$BASH_SOURCE
export BATFISH_TOOLS_PATH="$(cd "$(dirname "$BATFISH_SOURCED_SCRIPT")" && pwd)"

. "$BATFISH_TOOLS_PATH/common.sh" || return 1
. "$BATFISH_TOOLS_PATH/completion.sh"

