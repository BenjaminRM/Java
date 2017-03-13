package developer;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observable;
import augusta.properties.Access;
import augusta.properties.Direction;
import augusta.tree.*;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.FileChooser;
import java.io.ObjectOutputStream;


/**
 * The model to the AugustaDeveloper Tool.  Created for command sequence sorting, validation, parsing, and saving
 *
 * Created by Benjamin on 11/17/2016.
 */
class AugustaModel extends Observable
{
    /** List of the sorted commands (just strings) in the order they appear in their sequence */
    ArrayList<String> commandsSorted = new ArrayList<>();
    /** The hashmap of commands we will be looking through: keys: buttons, Values: Their Y position */
    private HashMap<Button, Double> commands = new HashMap<>();
    /** END commands required for validation */
    private int endsNeeded = 0;
    /** Number of actual END commands*/
    private int actualEnds = 0;
    /** The position the parser is currently at within the commandsSorted List */
    int position = 0;
    /** The final "to write" list of prognodes */
    ArrayList<ProgNode> tree = new ArrayList<>();
    /** The FileChooser object that is communicated to the View for file opening */
    private final FileChooser fileChooser = new FileChooser();

    /**
     * The main processing call from the "save" button
     *
     * @param cmdList the Hashmap of buttons in play, and their Y coordinate values
     */
    void process(HashMap<Button, Double> cmdList)
    {
        //Re-assigning the cmdList to a new object so we can mess with it
        commands.putAll(cmdList);

        //First, sort the commands by ranking Y translate values
        this.sortCommands();

        //Secondly, check the order of the commands to make sure it's a valid sequence of commands
        if(this.validateCommands())
        {
            //Once the sequence of nodes have been validated, put them in the "to write" tree
            this.parse();

            //Opens the file chooser, and continues the saving sequence
            this.updateValue();
        }
        //There was a problem with command sequence validation
        else
        {
            //Reset markers to zero
            endsNeeded = 0;
            actualEnds = 0;

            //Notify the user of their bad choices
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Augusta Developer Warning");
            alert.setHeaderText("Invalid Command Chain");
            alert.setContentText("You must have a valid sequence of commands in able to create your program!");

            alert.showAndWait();
        }
    }

    /**
     * Sorts the commands in order of their Y coordinate values
     */
    private void sortCommands()
    {
        //Used to mark the next button to be added to the list
        Button btn = new Button();

        //Builds the arraylist of sorted commands (by the button text) from left to right (top to bottom)
        while(!commands.isEmpty())
        {
            double smallest = 1300;

            for(Button b: commands.keySet())
            {
                if(commands.get(b) < smallest)
                {
                    smallest = commands.get(b);
                    btn = b;
                }
            }
            //Adds the button text to the sorted commands list
            commandsSorted.add(btn.getText());
            //removes the button from the hashmap (LOCAL, NOT THE ONE FROM THE VIEW) - the copied one
            commands.remove(btn);
        }
    }

    /**
     * Valids the required structure of the commands
     * Not ALL cases have been considered.
     *
     * @return true if it's a valid chain, false if not
     */
    private boolean validateCommands()
    {
        //Has to have EXACTLY one HALT command
        if(checkNumOfHalts() != 1)
        {
            return false;
        }

        //the HALT command MUST be the last command in the program
        if(!commandsSorted.get(commandsSorted.size()-1).equals("HALT"))
        {
            return false;
        }

        //If you have an If crumb, repeat, if access or while access command,
        //the next command HAS to be a BEGIN command
        for(int i = 0; i < commandsSorted.size();i++)
        {
            String[] cmd = commandsSorted.get(i).split(" ");

            if(cmd[0].equals("IF") || cmd[0].equals("WHILE") || cmd[0].equals("REPEAT"))
            {
                if(!commandsSorted.get(i+1).equals("BEGIN"))
                {
                    return false;
                }
                else
                {
                    endsNeeded += 1;
                }
            }
            else if(commandsSorted.get(i).equals("END"))
            {
                actualEnds += 1;
            }
        }

        //If there is not at least one end for each begin, the structure is faulty
        if(endsNeeded != actualEnds)
        {
            return false;
        }

        //If all these checks have passed, the method returns true (valid)
        return true;
    }

    /**
     * Iterates over the commandsSorted list to check for the total number of HALT commands in the sequence
     *
     * @return Count of HALT commands in the sequence
     */
    private int checkNumOfHalts()
    {
        int count = 0;

        for(String s: commandsSorted)
        {
            if(s.equals("HALT"))
            {
                count += 1;
            }
        }

        return count;
    }

    /**
     * Parses the commandsSorted list into another list of ProgNodes which represents the commands tree.
     */
    private void parse()
    {
        //While the position is less than the end of available commands in the commandsSorted List

        //This WHILE statement runs until the globally declared and incremented position variable reaches it's limit.
        //The Prognodes are each created in their own methods
        while(position < commandsSorted.size())
        {
            String command = commandsSorted.get(position);

            if (command.equals("FORWARD"))
            {
                tree.add(createForward());
            }
            else if(command.equals("DROP"))
            {
                tree.add(createDrop());
            }
            else if(command.equals("EAT CRUMB"))
            {
                tree.add(createEat());
            }
            else if(command.equals("HALT"))
            {
                tree.add(createHalt());
            }
            else if(command.split(" ")[0].equals("TURN"))
            {
                tree.add(createTurn(command));
            }
            else if(command.split(" ")[0].equals("IF") && command.split(" ")[1].equals("CRUMB"))
            {
                tree.add(createCrumb());
            }
            else if(command.split(" ")[0].equals("IF"))
            {
                tree.add(createIfBlock(command));
            }
            else if(command.split(" ")[0].equals("WHILE"))
            {
                tree.add(createWhileBlock(command));
            }
            else if(command.split(" ")[0].equals("REPEAT"))
            {
                tree.add(createRepeat(command));
            }
        }
    }

    /**
     * Creates the forward prognode
     * increments the position accordingly
     *
     * @return A forward prognode
     */
    private ProgNode createForward()
    {
        position += 1;
        return new Forward();
    }

    /**
     * Creates the drop prognode
     * increments the position accordingly
     *
     * @return A drop prognode
     */
    private ProgNode createDrop()
    {
        position += 1;
        return new Drop();
    }

    /**
     * Creates the eat prognode
     * increments the position accordingly
     *
     * @return A eat prognode
     */
    private ProgNode createEat()
    {
        position += 1;
        return new Eat();
    }

    /**
     * Creates the halt prognode
     * increments the position accordingly
     *
     * @return A halt prognode
     */
    private ProgNode createHalt()
    {
        position += 1;
        return new Halt();
    }

    /**
     * Creates the Do Nothing prognode
     * increments the position accordingly
     *
     * @return A Do Nothing prognode
     */
    private ProgNode createNOP()
    {
        position += 1;
        return new DoNothing();
    }

    /**
     * Creates the turn prognode
     * increments the position accordingly
     *
     * @return A turn prognode
     */
    private ProgNode createTurn(String command)
    {
        position += 1;

        String[] split = command.split(" ");

        if(split[1].equals("(RIGHT)"))
        {
            return new Turn(Direction.RIGHT);
        }
        else if(split[1].equals("(LEFT)"))
        {
            return new Turn(Direction.LEFT);
        }
        else if(split[1].equals("(AHEAD)"))
        {
            return new Turn(Direction.AHEAD);
        }
        else if(split[1].equals("(BEHIND)"))
        {
            return new Turn(Direction.BEHIND);
        }

        //Returns null if the cases don't work: this should NEVER happen
        return null;
    }

    /**
     * Creates the if crumb prognode
     * increments the position accordingly
     *
     * @return A if crumb prognode
     */
    private ProgNode createCrumb()
    {
        //Basically skipping over the required BEGIN statement
        position += 2;
        //If we should be adding the following commands to the elsePart list instead
        boolean addToElse = false;

        //Then and Else part lists
        ArrayList<ProgNode> thenPart = new ArrayList<>();
        ArrayList<ProgNode> elsePart = new ArrayList<>();

        //While the next command in line is not the END command,
        //this must be recursively enabled, checking for any other possible ProgNodes it
        //Needs to create and add to either it's thenPart or elsePart lists
        while(!commandsSorted.get(position).equals("END"))
        {
            String cmd = commandsSorted.get(position);

            if(cmd.equals("ELSE"))
            {
                addToElse = true;
                position += 1;
            }
            else if(cmd.equals("FORWARD"))
            {
                if(!addToElse)
                {
                    thenPart.add(createForward());
                }
                else
                {
                    elsePart.add(createForward());
                }
            }
            else if(cmd.equals("DROP"))
            {
                if(!addToElse)
                {
                    thenPart.add(createDrop());
                }
                else
                {
                    elsePart.add(createDrop());
                }
            }
            else if(cmd.equals("EAT CRUMB"))
            {
                if(!addToElse)
                {
                    thenPart.add(createEat());
                }
                else
                {
                    elsePart.add(createEat());
                }
            }
            else if(cmd.equals("NOP"))
            {
                if(!addToElse)
                {
                    thenPart.add(createNOP());
                }
                else
                {
                    elsePart.add(createNOP());
                }
            }
            else if(cmd.split(" ")[0].equals("TURN"))
            {
                if(!addToElse)
                {
                    thenPart.add(createTurn(cmd));
                }
                else
                {
                    elsePart.add(createTurn(cmd));
                }
            }
            else if(cmd.split(" ")[0].equals("IF") && cmd.split(" ")[1].equals("CRUMB"))
            {
                if(!addToElse)
                {
                    thenPart.add(createCrumb());
                }
                else
                {
                    elsePart.add(createCrumb());
                }
            }
            else if(cmd.split(" ")[0].equals("IF"))
            {
                if(!addToElse)
                {
                    thenPart.add(createIfBlock(cmd));
                }
                else
                {
                    elsePart.add(createIfBlock(cmd));
                }
            }
            else if(cmd.split(" ")[0].equals("WHILE"))
            {
                if(!addToElse)
                {
                    thenPart.add(createWhileBlock(cmd));
                }
                else
                {
                    elsePart.add(createWhileBlock(cmd));
                }
            }
            else if(cmd.split(" ")[0].equals("REPEAT"))
            {
                if(!addToElse)
                {
                    thenPart.add(createRepeat(cmd));
                }
                else
                {
                    elsePart.add(createRepeat(cmd));
                }
            }
        }

        //Skipping over the END command we just encountered
        position += 1;

        return new IfCrumb(thenPart, elsePart);
    }

    /**
     * Creates the if access prognode
     * increments the position accordingly
     *
     * @return A if access prognode
     */
    private ProgNode createIfBlock(String command)
    {
        //Getting this IfBlock's access and direction values
        Access access = determineAccess(commandsSorted.get(position).split(" ")[2]);
        Direction dir = determineDirection(commandsSorted.get(position).split(" ")[3]);

        //Basically skipping over the required BEGIN statement
        position += 2;
        //Whether or not we should be adding to the elsePart list
        boolean addToElse = false;

        //Then and ELse part Lists
        ArrayList<ProgNode> thenPart = new ArrayList<>();
        ArrayList<ProgNode> elsePart = new ArrayList<>();

        //While the next command in line is not the END command,
        //this must be recursively enabled, checking for any other possible ProgNodes it
        //Needs to create and add to either it's thenPart or elsePart lists
        while(!commandsSorted.get(position).equals("END"))
        {
            String cmd = commandsSorted.get(position);

            if(cmd.equals("ELSE"))
            {
                addToElse = true;
                position += 1;
            }
            else if(cmd.equals("FORWARD"))
            {
                if(!addToElse)
                {
                    thenPart.add(createForward());
                }
                else
                {
                    elsePart.add(createForward());
                }
            }
            else if(cmd.equals("DROP"))
            {
                if(!addToElse)
                {
                    thenPart.add(createDrop());
                }
                else
                {
                    elsePart.add(createDrop());
                }
            }
            else if(cmd.equals("EAT CRUMB"))
            {
                if(!addToElse)
                {
                    thenPart.add(createEat());
                }
                else
                {
                    elsePart.add(createEat());
                }
            }
            else if(cmd.equals("NOP"))
            {
                if(!addToElse)
                {
                    thenPart.add(createNOP());
                }
                else
                {
                    elsePart.add(createNOP());
                }
            }
            else if(cmd.split(" ")[0].equals("TURN"))
            {
                if(!addToElse)
                {
                    thenPart.add(createTurn(cmd));
                }
                else
                {
                    elsePart.add(createTurn(cmd));
                }
            }
            else if(cmd.split(" ")[0].equals("IF") && cmd.split(" ")[1].equals("CRUMB"))
            {
                if(!addToElse)
                {
                    thenPart.add(createCrumb());
                }
                else
                {
                    elsePart.add(createCrumb());
                }
            }
            else if(cmd.split(" ")[0].equals("IF"))
            {
                if(!addToElse)
                {
                    thenPart.add(createIfBlock(cmd));
                }
                else
                {
                    elsePart.add(createIfBlock(cmd));
                }
            }
            else if(cmd.split(" ")[0].equals("WHILE"))
            {
                if(!addToElse)
                {
                    thenPart.add(createWhileBlock(cmd));
                }
                else
                {
                    elsePart.add(createWhileBlock(cmd));
                }
            }
            else if(cmd.split(" ")[0].equals("REPEAT"))
            {
                if(!addToElse)
                {
                    thenPart.add(createRepeat(cmd));
                }
                else
                {
                    elsePart.add(createRepeat(cmd));
                }
            }
        }

        //Skipping over the END command we just encountered
        position += 1;

        return new IfBlocks(access, dir, thenPart, elsePart);
    }

    /**
     * Creates the while access prognode
     * increments the position accordingly
     *
     * @return A while access prognode
     */
    private ProgNode createWhileBlock(String command)
    {
        //Getting this IfBlock's access and direction values
        Access access = determineAccess(commandsSorted.get(position).split(" ")[2]);
        Direction dir = determineDirection(commandsSorted.get(position).split(" ")[3]);

        //Basically skipping over the required BEGIN statement
        position += 2;

        //the prognodes within the while loop
        ArrayList<ProgNode> whileNodeList = new ArrayList<>();

        //While the next command in line is not the END command,
        //this must be recursively enabled, checking for any other possible ProgNodes it
        //Needs to create and add to either it's thenPart or elsePart lists
        while(!commandsSorted.get(position).equals("END"))
        {
            String cmd = commandsSorted.get(position);

            if(cmd.equals("FORWARD"))
            {
                whileNodeList.add(createForward());
            }
            else if(cmd.equals("DROP"))
            {
                whileNodeList.add(createDrop());
            }
            else if(cmd.equals("EAT CRUMB"))
            {
                whileNodeList.add(createEat());
            }
            else if(cmd.split(" ")[0].equals("TURN"))
            {
                whileNodeList.add(createTurn(cmd));
            }
            else if(cmd.split(" ")[0].equals("IF") && cmd.split(" ")[1].equals("CRUMB"))
            {
                whileNodeList.add(createCrumb());
            }
            else if(cmd.split(" ")[0].equals("IF"))
            {
                whileNodeList.add(createIfBlock(cmd));
            }
            else if(cmd.split(" ")[0].equals("WHILE"))
            {
                whileNodeList.add(createWhileBlock(cmd));
            }
            else if(cmd.split(" ")[0].equals("REPEAT"))
            {
                whileNodeList.add(createRepeat(cmd));
            }
        }

        //Skipping over the END command we just encountered
        position += 1;

        return new While(access, dir, whileNodeList);
    }

    /**
     * Creates the repeat prognode
     * increments the position accordingly
     *
     * @return A repeat prognode
     */
    private ProgNode createRepeat(String command)
    {
        int reps = Integer.parseInt(commandsSorted.get(position).split(" ")[1].substring(1,2));

        ArrayList<ProgNode> repeatNodeList = new ArrayList<>();

        //Basically skipping over the required BEGIN statement
        position += 2;

        //While the next command in line is not the END command,
        //this must be recursively enabled, checking for any other possible ProgNodes it
        //Needs to create and add to either it's thenPart or elsePart lists
        while(!commandsSorted.get(position).equals("END"))
        {
            String cmd = commandsSorted.get(position);

            if(cmd.equals("FORWARD"))
            {
                repeatNodeList.add(createForward());
            }
            else if(cmd.equals("DROP"))
            {
                repeatNodeList.add(createDrop());
            }
            else if(cmd.equals("EAT CRUMB"))
            {
                repeatNodeList.add(createEat());
            }
            else if(cmd.split(" ")[0].equals("TURN"))
            {
                repeatNodeList.add(createTurn(cmd));
            }
            else if(cmd.split(" ")[0].equals("IF") && cmd.split(" ")[1].equals("CRUMB"))
            {
                repeatNodeList.add(createCrumb());
            }
            else if(cmd.split(" ")[0].equals("IF"))
            {
                repeatNodeList.add(createIfBlock(cmd));
            }
            else if(cmd.split(" ")[0].equals("WHILE"))
            {
                repeatNodeList.add(createWhileBlock(cmd));
            }
            else if(cmd.split(" ")[0].equals("REPEAT"))
            {
                repeatNodeList.add(createRepeat(cmd));
            }
        }

        //Skipping over the END command we just encountered
        position += 1;

        return new Repeat(reps, repeatNodeList);
    }

    /**
     * Determines the access field through the use of the string within the commandsSorted arraylist
     *
     * @param s the string from the commandsSorted arraylist that contains the text of the prognode command
     * @return the access appropriately matching ENUM
     */
    private Access determineAccess(String s)
    {
        if(s.equals("(OPEN"))
        {
            return Access.OPEN;
        }
        else
        {
            return Access.BLOCKED;
        }
    }

    /**
     * Determines the Direction field through the use of the string within the commandsSorted arraylist
     *
     * @param s the string from the commandsSorted arraylist that contains the text of the prognode command
     * @return the Direction appropriately matching ENUM
     */
    private Direction determineDirection(String s)
    {
        if(s.equals("RIGHT)"))
        {
            return Direction.RIGHT;
        }
        else if(s.equals("LEFT)"))
        {
            return Direction.LEFT;
        }
        else if(s.equals("AHEAD)"))
        {
            return Direction.AHEAD;
        }
        else
        {
            return Direction.BEHIND;
        }
    }

    /**
     * The operation to save to the file
     *
     * @param filename filename from the view's filechooser window (full path)
     */
    void save(File filename)
    {
        try
        {
            //Creating the Object Output stream
            ObjectOutputStream oout = new ObjectOutputStream(new FileOutputStream(filename));

            //Write all the prognodes in the "to write" tree
            for(ProgNode p: tree)
            {
                oout.writeObject(p);
            }

            oout.close();

            //Save confirmation
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Augusta Developer Save Confirmation");
            alert.setHeaderText("Your program has been saved!");
            alert.setContentText("You can now view / interpret your file at: \n" + filename);

            alert.showAndWait();

            //Once this file has been completely saved and finished, reset the tools
            //used to store, validate, and output the final serialized list of ProgNodes.
            tree.clear();
            position = 0;
            commandsSorted.clear();
        }
        catch (Exception e)
        {
            //Error with writing to the file, most likely: Give them a warning.
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Augusta Developer Warning");
            alert.setHeaderText("File Writing Error!");
            alert.setContentText("You must select a valid file in order to save your program!");

            alert.showAndWait();

            //Resets the tools used for storage, validation, and parsing
            tree.clear();
            position = 0;
            commandsSorted.clear();
        }
    }

    /**
     * Tells the view to open the filechooser window
     */
    private void updateValue()
    {
        //Telling the View to open a file choosing window and select a file
        super.setChanged();
        super.notifyObservers(fileChooser);
    }
}
