#!/bin/bash
# Compare NPCStack output to .solB solution files for all .boxes files in Boxes/
# Usage: ./compare_solutions.sh [initial_temperature] [cooling_rate]
# Prints green if your stack is as tall or taller, yellow if shorter, red if error.

INIT_TEMP=${1:-5}
COOL_RATE=${2:-0.5}

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

for file in Boxes/*.boxes; do
    base=$(basename "$file" .boxes)
    solfile="${file%.boxes}.solB"
    if [[ ! -f "$solfile" ]]; then
        echo -e "${YELLOW}No solution file for $file${NC}"
        continue
    fi
    # Run your program and get the top line (bottom of stack)
    my_last_line=$(java NPCStack "$file" "$INIT_TEMP" "$COOL_RATE" | tail -1)
    my_height=$(echo "$my_last_line" | awk '{print $4}')
    sol_last_line=$(tail -1 "$solfile")
    sol_height=$(echo "$sol_last_line" | awk '{print $4}')
    if [[ -z "$my_height" ]]; then
        echo -e "$file: ${RED}ERROR (no output)${NC}"
    elif (( my_height >= sol_height )); then
        echo -e "$file: ${GREEN}PASS (your height: $my_height, solution: $sol_height)${NC}"
    else
        echo -e "$file: ${YELLOW}SHORTER (your height: $my_height, solution: $sol_height)${NC}"
    fi
    echo "---"
done
