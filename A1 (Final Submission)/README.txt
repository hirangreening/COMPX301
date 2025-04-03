usage: cat MobyDick.txt | java XSort 512 2 > Moby.sorted

Note: When comparing my program to the output of using linux sort, I was only able to have them match successfully
when changing the LC_ALL=C.

LC_ALL=C sort MobyDick.txt > expected_output.txt
diff Moby.sorted expected_output.txt

I am unsure as to why or whether this note is necessary to include.