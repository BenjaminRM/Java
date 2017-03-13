import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;


/**
 *  The full representation of a configuration in the TentsAndTrees puzzle.
 *  It can read an initial configuration from a file, and supports the
 *  Configuration methods necessary for the Backtracker solver.
 *
 *  @author: Sean Strout @ RIT CS
 *  @author: Benjamin Mitchell
 *  @date: 11/20/2016
 */
public class TentConfig implements Configuration
{
    // INPUT CONSTANTS
    /** An empty cell */
    public final static String EMPTY = ".";
    /** A cell occupied with grass */
    public final static String GRASS = "#";
    /** A cell occupied with a tent */
    public final static String TENT = "^";
    /** A cell occupied with a tree */
    public final static String TREE = "%";
    /** Marker for trees not yet searched */
    public final static String MORPHED_TREE = "X";

    // OUTPUT CONSTANTS
    /** A horizontal divider */
    public final static String HORI_DIVIDE = "-";
    /** A vertical divider */
    public final static String VERT_DIVIDE = "|";

    //GRID, LOOKING VALUES, POSITION TRACKERS
    /** X looking values */
    public String[] xList;
    /** Y looking values */
    public String[] yList;
    /** Grid of Trees/morphed trees, empty space, grass, and tents */
    public String[][] grid;
    /** size of the grid */
    public int gridSize;
    /** Row of currently searched tree */
    public int treeRow;
    /** Column of currently searched tree */
    public int treeCol;
    /** Row of recently placed Tent */
    public int tentRow = 0;
    /** Column of recently placed Tent */
    public int tentCol = 0;
    /** Number of morphed trees remaining in the grid */
    public int morphedTreeCount = 0;

    /**
     * Construct the initial configuration from an input file whose contents
     * are, for example:<br>
     * <tt><br>
     * 3        # square dimension of field<br>
     * 2 0 1    # row looking values, top to bottom<br>
     * 2 0 1    # column looking values, left to right<br>
     * . % .    # row 1, .=empty, %=tree<br>
     * % . .    # row 2<br>
     * . % .    # row 3<br>
     * </tt><br>
     * @param filename the name of the file to read from
     * @throws FileNotFoundException if the file is not found
     */
    public TentConfig(String filename) throws FileNotFoundException
    {
        Scanner in = new Scanner(new File(filename));
        //Used for creating the grid
        int currentRow = 0;
        //Size of the grid
        gridSize = Integer.parseInt(in.nextLine());
        //instantiating the grid
        grid = new String[gridSize][gridSize];

        //Creating the yList of lookings values
        String y = in.nextLine();
        yList = y.split(" ");

        //Creating the xList of looking values
        String x = in.nextLine();
        xList = x.split(" ");

        //Reading the file into the grid
        while(in.hasNextLine())
        {
            String[] s = in.nextLine().split(" ");
            for(int i = 0;i<gridSize;i++)
            {
                grid[currentRow][i] = s[i];
            }

            currentRow += 1;
        }

        //Filling the EMPTY grid places with GRASS
        this.fillGrass();
        //Morphing all the trees into MORPHED_TREEs
        this.morphTrees();

        in.close();
    }

    /**
     * Copy constructor.  Takes a config, other, and makes a full "deep" copy
     * of its instance data.
     * @param other the config to copy
     */
    public TentConfig(TentConfig other)
    {
        //Copying over the xList, yList and gridSize (No changes, so no need to iterate)
        this.xList = other.xList;
        this.yList = other.yList;
        this.gridSize = other.xList.length;
        this.morphedTreeCount = other.morphedTreeCount;

        //Instantiating the new grid's size
        this.grid = new String[this.gridSize][this.gridSize];

        //Copying over the grid
        for(int r = 0;r < this.gridSize;r++)
        {
            for(int c = 0;c < this.gridSize;c++)
            {
                this.grid[r][c] = other.grid[r][c];
            }
        }
    }

    /**
     * Creating necessary successors for the next unchecked tree
     *
     * @return A collection of successors
     */
    @Override
    public Collection<Configuration> getSuccessors()
    {
        //Looping through the entire grid to find the next morphed tree
        for(int r = 0;r < this.gridSize;r++)
        {
            for (int c = 0; c < this.gridSize; c++)
            {
                if(this.grid[r][c].equals(MORPHED_TREE))
                {
                    //Setting the new tree's location
                    this.setTree(r, c);
                    //Making the (unchecked) MORPHED_TREE into a (checked) TREE
                    this.grid[r][c] = TREE;
                    //Decrementing the morphed tree count
                    this.morphedTreeCount -= 1;
                    //Returning the list of successors using this really ugly method
                    return adjacentToTree();
                }
            }
        }
        //Returns null if there are no more unchecked trees (really shouldn't happen)
        return null;
    }

    /**
     * Checks to see if the most recently added tent is valid
     *
     * @return true if the tent is a valid tent, false if not.
     */
    @Override
    public boolean isValid()
    {
        //If this tent is not adjacent to another tent, and does not exceed
        //the looking values for the xList or yList
        if(!adjacentToTent() && !exceedsRow() && !exceedsCol())
        {
            return true;
        }
        return false;
    }

    /**
     * Finds if this
     * @return true if the goal of a full campground has been reached, false if not.
     */
    @Override
    public boolean isGoal()
    {
        //If there are no more morphed trees remaining, check if this final
        //grid composition is the goal
        if(morphedTreeCount == 0)
        {
            //Count of total trees/tents in the grid
            int treeCount = 0;
            int tentCount = 0;

            //finding the total trees/tents in the grid
            for(int r = 0;r < this.gridSize;r++)
            {
                for(int c = 0;c < this.gridSize;c++)
                {
                    if(this.grid[r][c].equals(TREE) || this.grid[r][c].equals(MORPHED_TREE))
                    {
                        treeCount += 1;
                    }

                    if(this.grid[r][c].equals(TENT))
                    {
                        tentCount += 1;
                    }
                }
            }

            //if the trees = tents, then the goal has been met.
            //All other [goal] requirements have been met elsewhere
            if(treeCount > 0  && treeCount == tentCount)
            {
                return true;
            }
            return false;
        }
        return false;
    }

    /**
     *Building a toString that will print a representation of the current grid
     *
     * @return a string representation of the grid, and it's respective details
     */
    @Override
    public String toString()
    {
        String result = "";
        String horiBorder = stringRepeat(HORI_DIVIDE, (this.gridSize * 2));
        result += " " + horiBorder + "\n";

        for(int r = 0;r < this.gridSize;r++)
        {
            result += VERT_DIVIDE + " ";

            for(int c = 0;c < this.gridSize;c++)
            {
                result += this.grid[r][c] + " ";
            }

            result += VERT_DIVIDE + this.yList[r] + "\n";
        }
        result += " " + horiBorder + "\n  ";

        for(int i = 0; i < gridSize; i++)
        {
            result += xList[i] + " ";
        }

        return result;
    }

    /**
     * Used to assist the toString creation of the horizontal borders
     *
     * @param s the String being repeated
     * @param n the number of times it's being repeated
     * @return the new, repeeated string
     */
    private String stringRepeat(String s, int n)
    {
        StringBuilder res = new StringBuilder(s.length() * n);

        for(int i = 0; i < n; i++)
        {
            res.append(s);
        }

        return res.toString();
    }

    /**
     * Finds all possible tent placements around a tree
     *
     * +For the cases of: top left, top right, bottom left, bottom right,
     * top row, bottom row, left column, right column, or any of the center points of the grid
     * we have an if/else if case. (9 in total)
     *
     * For each one, we then see if the surrounding grid positions are grass that are within
     * the grid, to avoid any ArrayOutOfBounds exceptions.  If the position is grass, we make a new
     * successor for that position (with a maximum of 4 successors each time this method is called).
     *
     * For each successor,we create a new TentConfig, set it's changed grass to a tent, set the object's
     * position of last changed location (where the tent was just placed, for future reference), and then
     * add it to the set of successors.
     *
     * @return collection of new succsessor TentConfigs (possible grids)
     */
    private Collection<Configuration> adjacentToTree()
    {
        //Set to store the new possible sucsessors in
        Set<Configuration> lst = new HashSet<>();
        //declaring but not yet instantiating new TentConfigs
        TentConfig tc1;
        TentConfig tc2;
        TentConfig tc3;
        TentConfig tc4;

        //top left
        if(treeRow == 0 && treeCol == 0)
        {
            if(grid[treeRow+1][treeCol].equals(GRASS))
            {
                tc1 = new TentConfig(this);
                tc1.grid[treeRow+1][treeCol] = TENT;
                tc1.setTent(treeRow+1, treeCol);
                lst.add(tc1);
            }

            if(grid[treeRow][treeCol+1].equals(GRASS))
            {
                tc2 = new TentConfig(this);
                tc2.grid[treeRow][treeCol+1] = TENT;
                tc2.setTent(treeRow, treeCol+1);
                lst.add(tc2);
            }
        }
        //top right
        else if(treeRow == 0 && treeCol == gridSize-1)
        {
            if(grid[treeRow+1][treeCol].equals(GRASS))
            {
                tc1 = new TentConfig(this);
                tc1.grid[treeRow+1][treeCol] = TENT;
                tc1.setTent(treeRow+1, treeCol);
                lst.add(tc1);
            }

            if(grid[treeRow][treeCol-1].equals(GRASS))
            {
                tc2 = new TentConfig(this);
                tc2.grid[treeRow][treeCol-1] = TENT;
                tc2.setTent(treeRow, treeCol-1);
                lst.add(tc2);
            }
        }
        //bottom left
        else if(treeRow == gridSize-1 && treeCol == 0)
        {
            if(grid[treeRow-1][treeCol].equals(GRASS))
            {
                tc1 = new TentConfig(this);
                tc1.grid[treeRow-1][treeCol] = TENT;
                tc1.setTent(treeRow-1, treeCol);
                lst.add(tc1);
            }

            if(grid[treeRow][treeCol+1].equals(GRASS))
            {
                tc2 = new TentConfig(this);
                tc2.grid[treeRow][treeCol+1] = TENT;
                tc2.setTent(treeRow, treeCol+1);
                lst.add(tc2);
            }
        }
        //bottom right
        else if(treeRow == gridSize-1 && treeCol == gridSize-1)
        {
            if(grid[treeRow][treeCol-1].equals(GRASS))
            {
                tc1 = new TentConfig(this);
                tc1.grid[treeRow][treeCol-1] = TENT;
                tc1.setTent(treeRow, treeCol-1);
                lst.add(tc1);
            }

            if(grid[treeRow-1][treeCol].equals(GRASS))
            {
                tc2 = new TentConfig(this);
                tc2.grid[treeRow-1][treeCol] = TENT;
                tc2.setTent(treeRow-1, treeCol);
                lst.add(tc2);
            }
        }
        //top row
        else if(treeRow == 0)
        {
            if(grid[treeRow+1][treeCol].equals(GRASS))
            {
                tc1 = new TentConfig(this);
                tc1.grid[treeRow+1][treeCol] = TENT;
                tc1.setTent(treeRow+1, treeCol);
                lst.add(tc1);
            }

            if(grid[treeRow][treeCol+1].equals(GRASS))
            {
                tc2 = new TentConfig(this);
                tc2.grid[treeRow][treeCol+1] = TENT;
                tc2.setTent(treeRow, treeCol+1);
                lst.add(tc2);
            }

            if(grid[treeRow][treeCol-1].equals(GRASS))
            {
                tc3 = new TentConfig(this);
                tc3.grid[treeRow][treeCol-1] = TENT;
                tc3.setTent(treeRow, treeCol-1);
                lst.add(tc3);
            }
        }
        //bottom row
        else if(treeRow == gridSize-1)
        {
            if(grid[treeRow-1][treeCol].equals(GRASS))
            {
                tc1 = new TentConfig(this);
                tc1.grid[treeRow-1][treeCol] = TENT;
                tc1.setTent(treeRow-1, treeCol);
                lst.add(tc1);
            }

            if(grid[treeRow][treeCol+1].equals(GRASS))
            {
                tc2 = new TentConfig(this);
                tc2.grid[treeRow][treeCol+1] = TENT;
                tc2.setTent(treeRow, treeCol+1);
                lst.add(tc2);
            }

            if(grid[treeRow][treeCol-1].equals(GRASS))
            {
                tc3 = new TentConfig(this);
                tc3.grid[treeRow][treeCol-1] = TENT;
                tc3.setTent(treeRow, treeCol-1);
                lst.add(tc3);
            }
        }
        //left column
        else if(treeCol == 0)
        {

            if(grid[treeRow+1][treeCol].equals(GRASS))
            {
                tc1 = new TentConfig(this);
                tc1.grid[treeRow+1][treeCol] = TENT;
                tc1.setTent(treeRow+1, treeCol);
                lst.add(tc1);
            }

            if(grid[treeRow-1][treeCol].equals(GRASS))
            {
                tc2 = new TentConfig(this);
                tc2.grid[treeRow-1][treeCol] = TENT;
                tc2.setTent(treeRow-1, treeCol);
                lst.add(tc2);
            }

            if(grid[treeRow][treeCol+1].equals(GRASS))
            {
                tc3 = new TentConfig(this);
                tc3.grid[treeRow][treeCol+1] = TENT;
                tc3.setTent(treeRow, treeCol+1);
                lst.add(tc3);
            }
        }
        //right column
        else if(treeCol == gridSize-1)
        {
            if(grid[treeRow+1][treeCol].equals(GRASS))
            {
                tc1 = new TentConfig(this);
                tc1.grid[treeRow+1][treeCol] = TENT;
                tc1.setTent(treeRow+1, treeCol);
                lst.add(tc1);
            }

            if(grid[treeRow-1][treeCol].equals(GRASS))
            {
                tc2 = new TentConfig(this);
                tc2.grid[treeRow-1][treeCol] = TENT;
                tc2.setTent(treeRow-1, treeCol);
                lst.add(tc2);
            }

            if(grid[treeRow][treeCol-1].equals(GRASS))
            {
                tc3 = new TentConfig(this);
                tc3.grid[treeRow][treeCol-1] = TENT;
                tc3.setTent(treeRow, treeCol-1);
                lst.add(tc3);
            }
        }
        //any remaining positions (center positions)
        else
        {
            if(grid[treeRow+1][treeCol].equals(GRASS))
            {
                tc1 = new TentConfig(this);
                tc1.grid[treeRow+1][treeCol] = TENT;
                tc1.setTent(treeRow+1, treeCol);
                lst.add(tc1);
            }

            if(grid[treeRow-1][treeCol].equals(GRASS))
            {
                tc2 = new TentConfig(this);
                tc2.grid[treeRow-1][treeCol] = TENT;
                tc2.setTent(treeRow-1, treeCol);
                lst.add(tc2);
            }

            if(grid[treeRow][treeCol-1].equals(GRASS))
            {
                tc3 = new TentConfig(this);
                tc3.grid[treeRow][treeCol-1] = TENT;
                tc3.setTent(treeRow, treeCol-1);
                lst.add(tc3);
            }

            if(grid[treeRow][treeCol+1].equals(GRASS))
            {
                tc4 = new TentConfig(this);
                tc4.grid[treeRow][treeCol+1] = TENT;
                tc4.setTent(treeRow, treeCol+1);
                lst.add(tc4);
            }
        }
        return lst;
    }

    /**
     * Finds if the tent we just placed is adjacent to another, already placed tent.
     *
     * Checks the same 9 cases as the adjacentToTree method does, but this time only adds
     * the symbols that were found at the surrounding positions.
     *
     * Once all the surrounding symbols are added to the list, they are checked.  If any of them
     * are tents, this returns false.
     *
     * @return true if the checked tent placement is in fact adjacent to a tent (diagonals count)
     * and false if it is not.
     */
    private boolean adjacentToTent()
    {
        ArrayList<String> lst = new ArrayList<>();

        //top left corner
        if(tentRow == 0 && tentCol == 0)
        {
            lst.add(this.grid[tentRow+1][tentCol]);
            lst.add(this.grid[tentRow][tentCol+1]);
            lst.add(this.grid[tentRow+1][tentCol+1]);
        }
        //top right corner
        else if(tentRow == 0 && tentCol == gridSize-1)
        {
            lst.add(this.grid[tentRow+1][tentCol]);
            lst.add(this.grid[tentRow][tentCol-1]);
            lst.add(this.grid[tentRow+1][tentCol-1]);
        }
        //bottom left corner
        else if(tentRow == gridSize-1 && tentCol == 0)
        {
            lst.add(this.grid[tentRow-1][tentCol]);
            lst.add(this.grid[tentRow][tentCol+1]);
            lst.add(this.grid[tentRow-1][tentCol+1]);
        }
        //bottom right corner
        else if(tentRow == gridSize-1 && tentCol == gridSize-1)
        {
            lst.add(this.grid[tentRow-1][tentCol]);
            lst.add(this.grid[tentRow][tentCol-1]);
            lst.add(this.grid[tentRow-1][tentCol-1]);
        }
        //left side
        else if(tentCol == 0)
        {
            lst.add(this.grid[tentRow+1][tentCol]);
            lst.add(this.grid[tentRow-1][tentCol]);
            lst.add(this.grid[tentRow][tentCol+1]);
            lst.add(this.grid[tentRow+1][tentCol+1]);
            lst.add(this.grid[tentRow-1][tentCol+1]);
        }
        //right side
        else if(tentCol == gridSize-1)
        {
            lst.add(this.grid[tentRow+1][tentCol]);
            lst.add(this.grid[tentRow-1][tentCol]);
            lst.add(this.grid[tentRow][tentCol-1]);
            lst.add(this.grid[tentRow+1][tentCol-1]);
            lst.add(this.grid[tentRow-1][tentCol-1]);
        }
        //top row
        else if(tentRow == 0)
        {
            lst.add(this.grid[tentRow+1][tentCol]);
            lst.add(this.grid[tentRow][tentCol-1]);
            lst.add(this.grid[tentRow][tentCol+1]);
            lst.add(this.grid[tentRow+1][tentCol-1]);
            lst.add(this.grid[tentRow+1][tentCol+1]);
        }
        //bottom row
        else if(tentRow == gridSize-1)
        {
            lst.add(this.grid[tentRow-1][tentCol]);
            lst.add(this.grid[tentRow][tentCol+1]);
            lst.add(this.grid[tentRow][tentCol-1]);
            lst.add(this.grid[tentRow-1][tentCol-1]);
            lst.add(this.grid[tentRow-1][tentCol+1]);
        }
        //center of grid
        else
        {
            lst.add(this.grid[tentRow+1][tentCol]);
            lst.add(this.grid[tentRow-1][tentCol]);
            lst.add(this.grid[tentRow][tentCol+1]);
            lst.add(this.grid[tentRow][tentCol-1]);
            lst.add(this.grid[tentRow+1][tentCol+1]);
            lst.add(this.grid[tentRow+1][tentCol-1]);
            lst.add(this.grid[tentRow-1][tentCol+1]);
            lst.add(this.grid[tentRow-1][tentCol-1]);
        }

        //Checks if any of the added items to the list are tents
        for(String s: lst)
        {
            if(s.equals(TENT))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Sets the location of the last checked tree.
     *
     * @param row tree x (row)
     * @param col tree y (column)
     */
    public void setTree(int row, int col)
    {
        this.treeRow = row;
        this.treeCol = col;
    }

    /**
     *Fills all the EMPTY positions with GRASS
     */
    public void fillGrass()
    {
        for(int r = 0;r < this.gridSize;r++)
        {
            for(int c = 0;c < this.gridSize;c++)
            {
                if(this.grid[r][c].equals(EMPTY))
                {
                    this.grid[r][c] = GRASS;
                }
            }
        }
    }

    /**
     * Sets the position of the most recently added tent
     *
     * @param row the tent's x (row)
     * @param col the tent's y (column)
     */
    public void setTent(int row, int col)
    {
        this.tentRow = row;
        this.tentCol = col;
    }

    /**
     * Checks to see if the recently placed tent exceeds the looking value for that row
     *
     * @return true if the recently added tent will make that row exceed it's allowed looking value,
     * and false if it does not.
     */
    public boolean exceedsRow()
    {
        int count = 0;

        for(int i = 0;i<gridSize;i++)
        {
            if(grid[tentRow][i].equals(TENT))
            {
                count++;
            }
        }

        if(count <= Integer.parseInt(yList[tentRow]))
        {
            return false;
        }
        return true;
    }

    /**
     * Checks to see if the recently placed tent exceeds the looking value for that column
     *
     * @return true if the recently added tent will make that column exceed it's allowed looking value,
     * and false if it does not.
     */
    public boolean exceedsCol()
    {
        int count = 0;

        for(int i = 0;i<gridSize;i++)
        {
            if(grid[i][tentCol].equals(TENT))
            {
                count++;
            }
        }

        if(count <= Integer.parseInt(xList[tentCol]))
        {
            return false;
        }
        return true;
    }

    /**
     * Changes all the trees in the initial grid to MORPHED_TREEs
     * (to mark that they have yet to be checked.)
     *
     * This does not effect the final goal output
     */
    public void morphTrees()
    {
        for(int r = 0;r < this.gridSize;r++)
        {
            for(int c = 0;c < this.gridSize;c++)
            {
                if(this.grid[r][c].equals(TREE))
                {
                    this.grid[r][c] = MORPHED_TREE;
                    morphedTreeCount += 1;
                }
            }
        }
    }
}
