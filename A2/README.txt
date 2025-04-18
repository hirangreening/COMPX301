I have added a sample text file (FiftySentences.txt) and the expected output for running a good solo solution program searching for "marmot" (FiftyMarmots.txt)

E.g.  
   $ java KMPsearch "marmot" FiftySentences.txt > FiftyMarmots.txt


Note each line containing "marmot" is output once, preceded by the index into that line where the string starts (indexed from 1), separated by a space.
I did mean to post a sample like this earlier, so don't worry about any penalty if your output is at least reasonable given the specification.  
The main thing is that your string search works and uses your correctly calculated KMP skip table to achieve that goal.

