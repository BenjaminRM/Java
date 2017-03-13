import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents the Quadtree data structure used to compress raw
 * grayscale images and uncompress back.  Conceptually, the tree is
 * a collection of QTNode's.  A QTNode either holds a grayscale image
 * value (0-255), or QUAD_SPLIT, meaning the node is split into four
 * sub-nodes that are equally sized sub-regions that divide up the
 * current space.
 *
 * To learn more about quadtrees:
 *      https://en.wikipedia.org/wiki/Quadtree
 *
 * @author Sean Strout @ RIT
 * @author Benjamin Mitchell
 */
public class QTree {
    /** the value of a node that indicates it is spplit into 4 sub-regions */
    public final static int QUAD_SPLIT = -1;

    /** the root node in the tree */
    private QTNode root;

    /** the square dimension of the tree */
    private int DIM;

    /**  the raw image */
    private int image[][];

    /** the size of the raw image */
    private int rawSize;

    /** the size of the compressed image */
    private int compressedSize;

    /**
     * Create an initially empty tree.
     */
    public QTree() {
        this.root = null;
        this.DIM = 0;
        this.image = null;
        this.rawSize = 0;
        this.compressedSize = 0;
    }

    /**
     * Get the images square dimension.
     *
     * @return the square dimension
     */
    public int getDim() { return this.DIM; }

    /** Get the raw image.
     *
     * @return the raw image
     */
    public int[][] getImage(){ return this.image; }

    /**
     * Get the size of the raw image.
     *
     * @return raw image size
     */
    public int getRawSize() { return this.rawSize; }

    /**
     * Get the size of the compressed image.
     *
     * @return compressed image size
     */
    public int getCompressedSize() { return this.compressedSize; }

    /**
     * A private helper routine for parsing the compressed image into
     * a tree of nodes.  When parsing through the values, there are
     * two cases:
     *
     * 1. The value is a grayscale color (0-255).  In this case
     * return a node containing the value.
     *
     * 2. The value is QUAD_SPLIT.  The node must be split into
     * four sub-regions.  Each sub-region is attained by recursively
     * calling this routine.  A node containing these four sub-regions
     * is returned.
     *
     * @param values the values in the compressed image
     * @return a node that encapsulates this portion of the compressed
     * image
     * @throws QTException if there are not enough values in the
     * compressed image
     */
    private QTNode parse(List<Integer> values) throws QTException
    {
        //Value of the node
        int value = values.remove(0);
        if(value != this.QUAD_SPLIT)
        {
            //base case - return the node's value
            return new QTNode(value);
        }
        else
        {
            //If the -1 node doesn't have sufficient information below it
            if(values.size() < 4)
            {
                throw new QTException("Error uncompressing.  Not enough data.");
            }
            //Recursively call parse() to create the node tree
            QTNode ul = parse(values);
            QTNode ur = parse(values);
            QTNode ll = parse(values);
            QTNode lr = parse(values);
            return new QTNode(value, ul, ur, ll, lr);
        }
    }

    /**
     * This is the core routine for uncompressing an image stored in a tree
     * into its raw image (a 2-D array of grayscale values (0-255).
     * It is called by the public uncompress routine.
     * The main idea is that we are working with a tree whose root represents the
     * entire 2^n x 2^n image.  There are two cases:
     *
     * 1. The node is not split.  We can write out the corresponding
     * "block" of values into the raw image array based on the size
     * of the region
     *
     * 2. The node is split.  We must recursively call ourselves with the
     * the four sub-regions.  Take note of the pattern for representing the
     * starting coordinate of the four sub-regions of a 4x4 grid:
     *      - upper left: (0, 0)
     *      - upper right: (0, 1)
     *      - lower left: (1, 0)
     *      - lower right: (1, 1)
     * We can generalize this pattern by computing the offset and adding
     * it to the starting row and column in the appropriate places
     * (there is a 1).
     *
     * @param node the node to uncompress
     * @param size the size of the square region this node represents
     * @param start the starting coordinate this row represents in the image
     */
    private void uncompress(QTNode node, int size, Coordinate start)
    {
        //If the node does not have blocks underneath it
        if(node.getVal() != this.QUAD_SPLIT)
        {
            int val = node.getVal();
            int rowStart = start.getRow();
            int colStart = start.getCol();

            for(int r = 0; r < size; r++)
            {
                for(int c = 0; c < size; c++)
                {
                    //Populating the image 2-D array of ints
                    int row = rowStart + r;
                    int column = colStart + c;
                    image[row][column] = val;
                }
            }
        }
        //If the node is a -1 node (parent)
        else
        {
            int offset = size/2;
            this.uncompress(node.getUpperLeft(), size/2, new Coordinate(start.getRow(), start.getCol()));
            this.uncompress(node.getUpperRight(), size/2, new Coordinate(start.getRow(), start.getCol() + offset));
            this.uncompress(node.getLowerLeft(), size/2, new Coordinate(start.getRow() + offset, start.getCol()));
            this.uncompress(node.getLowerRight(), size/2, new Coordinate(start.getRow() + offset, start.getCol() + offset));
        }
    }

    /**
     * Uncompress a RIT compressed file.  This is the public facing routine
     * meant to be used by a client to uncompress an image for displaying.
     *
     * The file is expected to be 2^n x 2^n pixels.  The first line in
     * the file is its size (number of values).  The remaining lines are
     * the values in the compressed image, one per line, of "size" lines.
     *
     * Once this routine completes, the raw image of grayscale values (0-255)
     * is stored internally and can be retrieved by the client using getImage().
     *
     * @param filename the name of the compressed file
     * @throws IOException if there are issues working with the compressed file
     * @throws QTException if there are issues parsing the data in the file
     */
    public void uncompress(String filename) throws IOException, QTException
    {
        //Reading Object
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        //Compressed size is the number at the beginning of the file
        this.compressedSize = Integer.parseInt(reader.readLine());
        this.DIM = (int) Math.sqrt(compressedSize);
        //Creating a list to store read in values
        List<Integer> lst = new ArrayList<>();
        String num;
        //reading in the values from the file
        while((num = reader.readLine()) != null)
        {
            //populating the list
            lst.add(Integer.parseInt(num));
        }
        //creating the node tree by calling parse() on the list of values
        this.root = parse(lst);
        //instantiating the 2-D array for the image with it's size
        this.image = new int[this.DIM][this.DIM];
        //populating the 2-D array
        this.uncompress(this.root, this.DIM, new Coordinate(0, 0));
        reader.close();

    }

    /**
     * The private writer is a recursive helper routine that writes out the
     * compressed image.  It goes through the tree in preorder fashion
     * writing out the values of each node as they are encountered.
     *
     * @param node the current node in the tree
     * @param writer the writer to write the node data out to
     * @throws IOException if there are issues with the writer
     */
    private void write(QTNode node, BufferedWriter writer) throws IOException
    {
        //If the node value is not empty, do something with it
        if(node != null)
        {
            //If it's a 0-255 value, write it!
            if(node.getVal() != this.QUAD_SPLIT)
            {
                writer.write(node.getVal() + "\n");
            }
            //otherwise, recrusively call write(), further traversing through the QTree
            else
            {
                writer.write(node.getVal() + "\n");
                write(node.getUpperLeft(), writer);
                write(node.getUpperRight(), writer);
                write(node.getLowerLeft(), writer);
                write(node.getLowerRight(), writer);
            }

        }
    }

    /**
     * Write the compressed image to the output file.  This routine is meant to be
     * called from a client after it has been compressed
     *
     * @rit.pre client has called compress() to compress the input file
     * @param outFile the name of the file to write the compressed image to
     * @throws IOException any errors involved with writing the file out
     * @throws QTException if the file has not been compressed yet
     */
    public void write(String outFile) throws IOException, QTException
    {
        if(root != null)
        {
            //Writing utensil
            BufferedWriter writer = new BufferedWriter(new FileWriter(outFile));
            writer.write(this.rawSize + "\n");
            //Writes the QTree node tree + other information
            this.write(root, writer);
            writer.close();
        }
        else
        {
            throw new QTException("Error writing compressed file.  FIle has not been compressed.");
        }
    }

    /**
     * Check to see whether a region in the raw image contains the same value.
     * This routine is used by the private compress routine so that it can
     * construct the nodes in the tree.
     *
     * @param start the starting coordinate in the region
     * @param size the size of the region
     * @return whether the region can be compressed or not
     */
    private boolean canCompressBlock(Coordinate start, int size)
    {
        //Value of the pixel at a certain location
        int init = image[start.getRow()][start.getCol()];
        //Comparing the value to the rest of the prospective pixels
        for(int r = 0;r < size; r++)
        {
            for(int c = 0;c < size; c++)
            {
                int row = r + start.getRow();
                int col = c + start.getCol();
                if(image[row][col] != init)
                {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * This is the core compression routine.  Its job is to work over a region
     * of the image and compress it.  It is a recursive routine with two cases:
     *
     * 1. The entire region represented by this image has the same value, or
     * we are down to one pixel.  In either case, we can now create a node
     * that represents this.
     *
     * 2. If we can't compress at this level, we need to divide into 4
     * equally sized sub-regions and call ourselves again.  Just like with
     * uncompressing, we can compute the starting point of the four sub-regions
     * by using the starting point and size of the full region.
     *
     * @param start the start coordinate for this region
     * @param size the size this region represents
     * @return a node containing the compression information for the region
     */
    private QTNode compress(Coordinate start, int size)
    {
        //Value of the pixel at a certain location
        int num = image[start.getRow()][start.getCol()];
        //If the size of the area (in pixels) is 1, the block can no longer
        //be broken down, so just return a new node with said node's value
        if(size == 1)
        {
            //+1 to node count
            this.compressedSize += 1;
            return new QTNode(num);
        }
        //Although this does the same thing as the 'if' above, it checks if the
        //block surrounding this pixel can be collapsed into less nodes
        else if(canCompressBlock(start, size))
        {
            //+1 to node count
            this.compressedSize += 1;
            return new QTNode(num);
        }
        //Size is >1 and can't be collapsed
        else
        {
            //+1 to node count
            this.compressedSize += 1;
            int row = start.getRow();
            int col = start.getCol();
            int offset = size/2;
            //I got lost in the parenthesis for about 5 minutes
            return new QTNode(-1, (this.compress(new Coordinate(row, col), size/2)),
                    (this.compress(new Coordinate(row, col + offset), size/2)),
                    (this.compress(new Coordinate(row + offset, col), size/2)),
                    (this.compress(new Coordinate(row + offset, col + offset), size/2)));
        }
    }

    /**
     * Compress a raw image into the RIT format.  This routine is meant to be
     * called by a client.  It is expected to be passed a file which represents
     * the raw image.  It is ASCII formatted and contains a series of grayscale
     * values (0-255).  There is one value per line, and 2^n x 2^n total lines.
     *
     * @param inputFile the raw image file name
     * @throws IOException if there are issues working with the file
     */
    public void compress(String inputFile) throws IOException
    {
        //Creating our reading object
        BufferedReader reader = new BufferedReader(new FileReader(inputFile));

        //creates a list of the integers
        List<Integer> lst = new ArrayList<>();
        String num;
        //reading in the file, stores in an integer array
        while((num = reader.readLine()) != null)
        {
            lst.add(Integer.parseInt(num));
        }

        int arySize = (int) Math.sqrt(lst.size());

        //setting the size of the 2-D array
        this.image = new int[arySize][arySize];
        int counter = 0;

        //Populating the 2-D array of integers that represents the image pixel colors
        //(One coordinate = one pixel)
        for(int r = 0; r < arySize; r++)
        {
            for(int c = 0; c < arySize; c++)
            {
                image[r][c] = lst.get(counter);
                counter += 1;
            }
        }
        //+1 to count the root
        this.compressedSize += 1;
        //Calling the compressing routine further to create the tree
        //and populate the root object
        this.root = this.compress(new Coordinate(0, 0), arySize);
        //The raw size of this file/image, used in the % compressed calculation
        this.rawSize = (arySize * arySize);
        reader.close();
    }

    /**
     * A preorder (parent, left, right) traversal of a node.  It returns
     * a string which is empty if the node is null.  Otherwise
     * it returns a string that concatenates the current node's value
     * with the values of the 4 sub-regions (with spaces between).
     *
     * @param node the node being traversed on
     * @return the string of the node
     */
    private String preorder(QTNode node)
    {
        //If there is no value, just return upward (base case 1)
        if(node == null)
        {
            return "";
        }
        //Recursively call if the node being passed is a -1
        //signaling more block breakage within this block of pixels
        else if(node.getVal() == this.QUAD_SPLIT)
        {
            String result = "";

            result += preorder(node.getUpperLeft());
            result += preorder(node.getUpperRight());
            result += preorder(node.getLowerLeft());
            result += preorder(node.getLowerRight());

            return (node.getVal() + " " + result);

        }
        //Otherwise, add to the string return the node value (base case 2)
        else
        {
            return node.getVal() + " ";
        }
    }

    /**
     * Returns a string which is a preorder traversal of the tree.
     *
     * @return the qtree string representation
     */
    @Override
    public String toString()
    {
        //Print the tree's preorder
        return "QTree: " + preorder(this.root);
    }
}