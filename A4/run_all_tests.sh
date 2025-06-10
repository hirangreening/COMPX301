#!/bin/bash
# Run NPCStack on all .boxes files in the Boxes directory
# Usage: ./run_all_tests.sh [initial_temperature] [cooling_rate]
# Defaults: initial_temperature=5, cooling_rate=0.5

INIT_TEMP=${1:-5}
COOL_RATE=${2:-0.5}

GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

for file in Boxes/*.boxes; do
    echo -n "Testing $file ... "
    output=$(java NPCStack "$file" "$INIT_TEMP" "$COOL_RATE" 2>&1)
    if [[ $? -eq 0 && -n "$output" ]]; then
        echo -e "${GREEN}PASS${NC}"
    else
        echo -e "${RED}FAIL${NC}"
        echo -e "${YELLOW}$output${NC}"
    fi
    echo "---"
done
