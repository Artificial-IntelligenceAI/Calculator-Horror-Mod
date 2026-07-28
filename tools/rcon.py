#!/usr/bin/env python3
"""
Send a single RCON command to the local dev server and print the response.

Requires the `mcrcon` package (pip install mcrcon) and a server started with
enable-rcon=true / rcon.password set to match RCON_PASSWORD (see run/server.properties).
This is test-session tooling only - see memory/mod_architecture.md for the full
enable-RCON -> test -> revert workflow this fits into.

Usage:
    python3 tools/rcon.py "calculatorhorror test selftest"
    RCON_PASSWORD=mypassword python3 tools/rcon.py "list" --host 127.0.0.1 --port 25575
"""

import argparse
import os
import sys

from mcrcon import MCRcon

DEFAULT_PASSWORD = "selftestlocalonly"


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("command", help="The command to run, without a leading slash")
    parser.add_argument("--host", default="127.0.0.1")
    parser.add_argument("--port", type=int, default=25575)
    parser.add_argument(
        "--password",
        default=os.environ.get("RCON_PASSWORD", DEFAULT_PASSWORD),
        help="Defaults to $RCON_PASSWORD, then the conventional local test password",
    )
    args = parser.parse_args()

    with MCRcon(args.host, args.password, port=args.port) as mcr:
        print(mcr.command(args.command))


if __name__ == "__main__":
    sys.exit(main())
