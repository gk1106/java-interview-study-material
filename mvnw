#!/bin/bash
# Local helper: prepends the cached Maven distribution to PATH and delegates to mvn.
export PATH="/c/Users/gk110/.m2/wrapper/dists/apache-maven-3.9.11/03d7e36a140982eea48e22c1dcac01d8862b2550b2939e09a0809bbc5182a5bc/bin:$PATH"
exec mvn "$@"
