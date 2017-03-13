Lab 8 Completed Fall Semester 2016 - 2017
I was given all necessary files to complete this lab EXCEPT QTree.java, RITCompress.java, and RITUncompress.java

RITCompress:
	This program takes a raw image file (/images/raw/) of grayscale values (0-255) and compresses them into the .RIT format (/images/compressed).
	The number at the top of the file signifies the number of pixels in the grayscale image.  Each line in the compressed format is considered a 
	new node.

RITUncompress:
	Takes the compressed data that RITCompressed produced (/images/compressed/) and converts it back to the image format it was before being
	compressed.  This program will also produce a GUI with a visual representation of the image after decompression.  

* These programs are also set to output files containing their node trees, and compression rates.  Check /output/compress and /output/uncompress for more.

*****************************Compress************************************

USAGE: RITCompress
	$java -jar RITCompress.jar <OPTION1> <OPTION2>

	Where OPTION1 is:
		images/raw/simple4x4.txt
		images/raw/simple8x8.txt
		images/raw/ritlogo128x128.txt
		images/raw/smileyface256x256.txt
		images/raw/mascot256x256.txt
		images/raw/redsox512x512.txt

		(Check "/images/raw/" for more options)

	Where OPTION2 is:
		output/compress/<filename>.txt

	Example Runs:
		$java -jar RITCompress.jar images/raw/simple8x8.txt output/compress/simple8x8.txt
		$java -jar RITCompress.jar images/raw/ritlogo128x128.txt output/compress/ritlogo128x128.txt
		$java -jar RITCompress.jar images/raw/smileyface256x256.txt output/compress/smileyface256x256.txt

******************************Uncompress***********************************

USAGE: RITUncompress
	$java -jar RITUncompress.jar <OPTION1>

	Where OPTION1 is:
		images/compressed/simple4x4.rit
		images/compressed/simple16x16.rit
		images/compressed/simple256x256.rit
		images/compressed/earth256x256.rit
		images/compressed/incline256x256.rit
		images/compressed/smileyface256x256.rit
		images/compressed/redsox512x512.rit

		(Check "/images/compressed/" for more options)

	Example Runs:
		$java -jar RITUncompress.jar images/compressed/simple16x16.rit
		$java -jar RITUncompress.jar images/compressed/incline256x256.rit
		$java -jar RITUncompress.jar images/compressed/smileyface256x256.rit