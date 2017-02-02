# SmartQA
Identify whether the question asked on forum by the user is an appropriate question or not based on the subject and content of the question

## How to use
1. Download "SQA.jar" file in JarFile folder.
2. Open the terminal and go to where the jar file is downloaded.
3. Type in java -jar SQA.jar along with acceptable commands and arguments (provided that Java is installed)
4. Press "Enter/Return" to execute the command.

## Available commands
- `help`: Shows available functionality.
- `check <text file directory>`: Check the appropriate level of the question contained in the text file.
- `add <dictionary code> <word>`: Add new word to specified dictionary.
- `remove <dictionary code> <word>`: Remove word from the specified dictionary.

## Dictionary code:
- 0: English dictionary (used to check for grammar)
- 1: Inappropriate words level 1 (lose points if found in question)
- 2: Inappropriate words level 2 (automatically marked as RTFM)
