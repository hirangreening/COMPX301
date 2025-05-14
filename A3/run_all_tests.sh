#!/bin/bash
# filepath: /home/hiran/Documents/Trimester A 2025/COMPX301/A3/batch_regex_tests.sh

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Files
TESTFILE="test.txt"
MYOUT="my_output.txt"
GREPOUT="grep_output.txt"

# Test patterns (A-P)
declare -A patterns=(
  [A]="a"
  [B]="ab"
  [C]="aa*b"
  [D]="a?b"
  [E]="a.a"
  [F]="a.*b"
  [G]="a\\.b"
  [H]="a\\.*b"
  [I]="ab|cd"
  [J]="a(b|c)d"
  [K]="a(b\\|c)d"
  [L]="a(b|c)(de|fg)a"
  [M]="ab*b?a"
  [N]="g(a(b|c)a)|(de)g"
  [O]="(bb|cc)?d*e\\.\\?\\*\\)|\\(\\)aa"
  [P]="a(dd|ee)?a(gg|ff)*a"
)

# Initialize counters
passed=0
failed=0
total=0

# Arrays to track which tests passed/failed
passed_tests=()
failed_tests=()

echo "Compiling Java files..."
javac *.java

echo "===== RUNNING BATCH REGEX TESTS ====="
echo "Total tests: ${#patterns[@]}"
echo "====================================="

for key in $(printf "%s\n" "${!patterns[@]}" | sort); do
  regex="${patterns[$key]}"
  echo -e "\nTest $key: '$regex'"

  # Run your matcher
  java REcompile "$regex" | java REsearch "$TESTFILE" > "$MYOUT" 2>/dev/null

  # Run grep for reference
  grep -E "$regex" "$TESTFILE" > "$GREPOUT"

  # Compare outputs (ignore order, blank lines, trailing whitespace, and carriage returns)
  diff -u \
    <(sort "$MYOUT" | sed '/^$/d' | tr -d '\r' | sed 's/[[:space:]]*$//') \
    <(sort "$GREPOUT" | sed '/^$/d' | tr -d '\r' | sed 's/[[:space:]]*$//') > diff.txt

  if [[ $? -eq 0 ]]; then
    echo -e "${GREEN}SUCCESS: Output matches grep${NC}"
    passed=$((passed + 1))
    passed_tests+=("$key")
  else
    echo -e "${RED}FAILURE: Output differs from grep${NC}"
    echo -e "${RED}--- Your output:${NC}"
    cat "$MYOUT"
    echo -e "${RED}--- Expected (grep):${NC}"
    cat "$GREPOUT"
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
echo -e "\n====================================="
echo -e "Tests completed: $total"
echo -e "${GREEN}Tests passed: $passed${NC}"
for key in "${passed_tests[@]}"; do
  echo -e "  $key (${patterns[$key]})"
done
echo -e "${RED}Tests failed: $failed${NC}"
for key in "${failed_tests[@]}"; do
  echo -e "  $key (${patterns[$key]})"
done
echo "====================================="