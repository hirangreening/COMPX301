#!/bin/bash
# Compare NPCStack output to rand0100.solB for rand0100.boxes

INIT_TEMP=${1:-5}
COOL_RATE=${2:-0.5}

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

file="Boxes/rand0100.boxes"
solfile="Boxes/rand0100.solB"

echo "Comparing your solution to the professor's for rand0100.boxes"
echo "-------------------------------------------------------------"

my_output=$(java NPCStack "$file" "$INIT_TEMP" "$COOL_RATE")
my_last_line=$(echo "$my_output" | tail -1)
my_height=$(echo "$my_last_line" | awk '{print $4}')

if [[ -f "$solfile" ]]; then
    prof_last_line=$(tail -1 "$solfile")
    prof_height=$(echo "$prof_last_line" | awk '{print $4}')
    echo -e "Your stack height:      ${YELLOW}$my_height${NC}"
    echo -e "Professor's stack height: ${GREEN}$prof_height${NC}"
    echo
    if [[ -z "$my_height" ]]; then
        echo -e "${RED}ERROR: No output from your program.${NC}"
    elif (( my_height >= prof_height )); then
        echo -e "${GREEN}PASS: Your stack is as tall or taller!${NC}"
    else
        echo -e "${YELLOW}SHORTER: Your stack is shorter than the professor's.${NC}"
    fi
    echo
    echo "Your top 5 boxes:"
    echo "$my_output" | head -n 5
    echo
    echo "Professor's top 5 boxes:"
    head -n 5 "$solfile"
else
    echo -e "${RED}Professor's solution file not found: $solfile${NC}"
fi
