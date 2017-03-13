Part of the Augusta Project 2 - Java project, completed Fall Semester 2016-2017.

This entire project was created from scratch except for the AST library.  I developed part 1 of the project, the developer.  
My partner created the second part of the project, the interpreter.

My half, the developer, is meant to act as a drag-and-drop programmable GUI. The Nodes that are dragged and dropped are parsed
into a node tree, and outputted to a file for interpretting by the interpretter side of the project.

More information can be found inside the project assignment pdf.

To run, simply launch the .jar file.

The node list is parsed top-down, and requires certain nodes in order to parse correctly.  You will get error notifications
if you incorrectly try to place nodes where they should not be.

Follow these rules to ensure your program parses correctly:

	1. Every program needs one and only one "halt" command.  At the end.
	2. After every IF CRUMB, IF ACCESS, WHILE ACCESS, and REPEAT command nodes, you must being your content within the block 
	with a BEGIN command, and end the block content with an END command.  This is so the program knows when to start/end
	the logic within the IF/WHILE/REPEAT commands.
	3. If you want to use an ELSE command, place it within the IF command's BEGIN and END commands, and do not include a BEGIN and END command for it.

If you click "save" and it opens the file explorer, and you cancel, you will get a file writing error because the program cannot see the file after it
was supposed to be saved.
