#!/bin/bash

# Set test profile
export SPRING_PROFILES_ACTIVE=test

echo "Running tests with coverage..."
./mvnw clean test jacoco:report

echo "=========================================="
echo "           COVERAGE SUMMARY               "
echo "=========================================="

if [ -f target/site/jacoco/jacoco.csv ]; then
    # Skip header and calculate overall coverage
    TOTAL_LINES=$(tail -n +2 target/site/jacoco/jacoco.csv | awk -F',' '{sum1 += $8; sum2 += $9} END {print sum1 + sum2}')
    COVERED_LINES=$(tail -n +2 target/site/jacoco/jacoco.csv | awk -F',' '{sum += $9} END {print sum}')
    
    if [ $TOTAL_LINES -gt 0 ]; then
        COVERAGE=$(awk "BEGIN { printf \"%.2f\", ($COVERED_LINES / $TOTAL_LINES) * 100 }")
        echo "Line Coverage: $COVERAGE% ($COVERED_LINES of $TOTAL_LINES lines)"
    else
        echo "No lines of code found to calculate coverage"
    fi
    
    echo
    
    # Show top 5 classes with lowest coverage (excluding 0% classes)
    echo "Top 5 classes with lowest coverage:"
    echo "-----------------------------------"
    tail -n +2 target/site/jacoco/jacoco.csv | 
      awk -F',' '$8 + $9 > 0 {
        covered=$9; 
        total=$8 + $9; 
        if (covered > 0 && covered < total) 
          printf "%s.%s: %.2f%% (%d of %d lines)\n", $2, $3, (covered/total)*100, covered, total
      }' | 
      sort -t: -k2 -n | 
      head -5
    
    echo
    echo "Full report available at: target/site/jacoco/index.html"
    echo "=========================================="
else
    echo "No coverage data found. Check if tests ran successfully."
fi

# Open the HTML report if possible
if [ -f target/site/jacoco/index.html ]; then
    if command -v open &> /dev/null; then
        open target/site/jacoco/index.html
    elif command -v xdg-open &> /dev/null; then
        xdg-open target/site/jacoco/index.html
    else
        echo "Please open the code coverage report manually in your browser"
    fi
fi 