#!/bin/bash
# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Temporary files for testing
TEMP_FILE="test_string.txt"
OUTPUT_FILE="output.txt"

# Function to run a single test
run_test() {
  local regex="$1"
  local test_string="$2"

  # Create temporary file with the test string
  echo "$test_string" >"$TEMP_FILE"

  # Run the REcompile and REsearch commands and redirect output to a file
  java REcompile "$regex" | java REsearch "$TEMP_FILE" >"$OUTPUT_FILE" 2>&1

  # Check if the output file has content
  if [[ ! -s "$OUTPUT_FILE" ]]; then
    echo -e "${RED}FAILURE: No output received for regex '$regex' on string '$test_string'${NC}"
    echo -e "${RED}This likely indicates an error in the program execution${NC}"
    return 1
  fi

  # Read the output
  output=$(<"$OUTPUT_FILE")

  # Check if there's any output at all - indicating a match was found
  if [[ -n "$output" ]]; then
    echo -e "${GREEN}SUCCESS: Regex '$regex' matched in string '$test_string'${NC}"
    echo -e "${GREEN}Match found: $output${NC}"
    return 0
  else
    echo -e "${RED}FAILURE: Regex '$regex' did not match in string '$test_string'${NC}"
    echo -e "${RED}No match found in: $test_string${NC}"
    return 1
  fi
}

# Initialize counters
passed=0
failed=0
total=0

# Test cases array
# Format: ("regex1:test_string1" "regex2:test_string2" ...)
declare -a test_cases=(
  # Add your test cases here in the format "regex:test_string"
  "a:a"
  "a:abc"
  "a:cba"
  "abc:abc"
  "abc:xabcx"
  "a|b|c:a"
  "a|b|c:b"
  "a|b|c:c"
  "abc|def|ghi:abc"
  "abc|def|ghi:def"
  "abc|def|ghi:ghi"
)

# Compile all Java files
echo "Compiling Java files..."
javac *.java

# Run all tests
echo "===== RUNNING REGEX TESTS ====="
echo "Total tests: ${#test_cases[@]}"
echo "=============================="

for test_case in "${test_cases[@]}"; do
  # Split the test case into regex and test string
  IFS=':' read -r regex test_string <<<"$test_case"

  echo -e "\nTest #$((total + 1)): '$regex' on '$test_string'"
  if run_test "$regex" "$test_string"; then
    passed=$((passed + 1))
  else
    failed=$((failed + 1))
  fi
  total=$((total + 1))
done

# Clean up temporary files
rm -f "$TEMP_FILE" "$OUTPUT_FILE"

# Print summary
echo -e "\n=============================="
echo -e "Tests completed: $total"
echo -e "${GREEN}Tests passed: $passed${NC}"
echo -e "${RED}Tests failed: $failed${NC}"
echo "=============================="
