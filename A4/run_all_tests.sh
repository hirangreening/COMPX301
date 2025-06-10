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

pass=0
fail=0

for file in Boxes/*.boxes; do
    echo -n "Testing $file ... "
    output=$(java NPCStack "$file" "$INIT_TEMP" "$COOL_RATE" 2>&1)
    status=$?
    if [[ $status -eq 0 && -n "$output" ]]; then
        # Extract the last number from the last line (total height)
        total_height=$(echo "$output" | tail -n 1 | awk '{print $4}')
        echo -e "${GREEN}PASS${NC} (height: $total_height)"
        echo "$output" > "${file%.boxes}.out"
        echo "  Top of stack:"
        echo "$output" | head -n 3
        ((pass++))
    else
        echo -e "${RED}FAIL${NC}"
        echo -e "${YELLOW}$output${NC}"
        ((fail++))
    fi
    echo "---"
done

echo -e "${GREEN}Passed: $pass${NC}, ${RED}Failed: $fail${NC}"