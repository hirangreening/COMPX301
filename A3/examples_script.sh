#!/bin/bash

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m' # No Color

TESTFILE="simple.txt"
MYOUT="my_output.txt"
GREPOUT="grep_output.txt"

declare -A patterns=(
  [A]="z"
  [B]="j|z"
  [C]="aardvark|zebra"
)

# Expected lines for each pattern (for demonstration, you may want to fill these in)
declare -A expected_lines
expected_lines[A]="a zebra was found by the dog .\na jumbo dog kissed the zebra ."
expected_lines[B]="a zebra was found by the dog .\nthe dog was loved by a jumbo fish .\nthe jumbo bird hugged the mouse .\na jumbo dog kissed the zebra ."
expected_lines[C]="an aardvark was cheated by the fish .\na zebra was found by the dog .\nthe fish was cheated by an aardvark .\na jumbo dog kissed the zebra ."

passed=0
failed=0
total=0
passed_tests=()
failed_tests=()

echo "===== RUNNING ALLEG EXAMPLES TESTS ====="
echo "Total tests: ${#patterns[@]}"
echo "========================================"

for key in $(printf "%s\n" "${!patterns[@]}" | sort); do
  regex="${patterns[$key]}"
  echo -e "\nTest $key: '$regex'"

  # Run your matcher
  java REcompile "$regex" | java REsearch "$TESTFILE" > "$MYOUT" 2>/dev/null

  # Run grep for reference (for comparison, but not used for expected output here)
  grep -E "$regex" "$TESTFILE" > "$GREPOUT"

  # Compare outputs (ignore order, blank lines, trailing whitespace, and carriage returns)
  diff -u \
    <(sort "$MYOUT" | sed '/^$/d' | tr -d '\r' | sed 's/[[:space:]]*$//') \
    <(echo -e "${expected_lines[$key]}" | sort | sed '/^$/d' | tr -d '\r' | sed 's/[[:space:]]*$//') > diff.txt

  if [[ $? -eq 0 ]]; then
    echo -e "${GREEN}SUCCESS: Output matches expected${NC}"
    passed=$((passed + 1))
    passed_tests+=("$key")
  else
    echo -e "${RED}FAILURE: Output differs from expected${NC}"
    echo -e "${RED}--- Your output:${NC}"
    cat "$MYOUT"
    echo -e "${RED}--- Expected:${NC}"
    echo -e "${expected_lines[$key]}"
    echo -e "${RED}--- Diff:${NC}"
    cat diff.txt
    failed=$((failed + 1))
    failed_tests+=("$key")
  fi
  total=$((total + 1))
done

# Clean up
rm -f "$MYOUT" "$GREPOUT" diff.txt

# Print summary
echo -e "\n========================================"
echo -e "Tests completed: $total"
echo -e "${GREEN}Tests passed: $passed${NC}"
for key in "${passed_tests[@]}"; do
  echo -e "  $key (${patterns[$key]})"
done
echo -e "${RED}Tests failed: $failed${NC}"
for key in "${failed_tests[@]}"; do
  echo -e "  $key (${patterns[$key]})"
done
echo "========================================"