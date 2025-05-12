# README for REcompile.java  
**Author:** Hiran Greening  
**Student ID:** 1522172  

## Overview  
This program compiles regular expressions into a **Finite State Machine (FSM)** using Java.  
Supports: **wildcard (`.`), alternation (`|`), repetition (`*`, `+`, `?`), escaped characters (`\`), and grouping (`()`).**  

## Usage  
Compile:  javac REcompile.java
Run: java REcompile "your_regex_here"
Example: java REcompile "a.b+c|(def)ghi?"

## FSM Format
State-Number, Symbol/Type, Next-State-1, Next-State-2
Example: 0,BR,1,-1 1,a,2,-1 2,WC,3,-1 3,b,4,-1 ...  

## Grammar

<regexp>         ::= <alternation>
<alternation>    ::= <concatenation> | <alternation> '|' <concatenation>
<concatenation>  ::= <repetition> | <concatenation> <repetition>
<repetition>     ::= <base> | <base> '*' | <base> '+' | <base> '?'
<base>           ::= <literal> | '.' | '(' <regexp> ')' | <escaped>
<literal>        ::= <non_special_char>
<escaped>        ::= '\' <special_char> | '\' <non_special_char>
<special_char>   ::= '*' | '+' | '?' | '|' | '.' | '(' | ')' | '\'
<non_special_char>::= any character not in <special_char>

## Features  
✅ Literals, Wildcard (`.`), Concatenation, Alternation (`|`)  
✅ Repetition (`*`, `+`, `?`), Grouping (`()`), Escape (`\`)  
✅ Validates regex & detects errors  

## Submission  
- **Submit:** `1522172.zip` via Moodle  
- **Deadline:** **Friday, 16 May 2025, 11:59pm**  