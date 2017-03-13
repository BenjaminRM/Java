package developer;

import javafx.application.Application;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.FileChooser;

/**
 * Augusta Developer tool
 *
 * Created by Benjamin on 11/17/2016.
 */
public class AugustaView extends Application implements Observer
{
    /** Holds the constants for each command label */
    private final String[] COMMANDS = new String[] {"FORWARD", "TURN (RIGHT)", "IF CRUMB",
                                                            "IF ACCESS (OPEN AHEAD)", "WHILE ACCESS (OPEN AHEAD)",
                                                            "REPEAT (2)", "EAT CRUMB", "DROP", "NOP",
                                                            "HALT", "BEGIN", "END", "ELSE"};
    /** Hashmap of all currently active commands (the ones the user will send for program finalization) */
    private HashMap<Button, Double> cmdList = new HashMap<>();
    /** Largest view containing other layout panes */
    private BorderPane bp;
    /** Being added to the borderpane's center (contains current command heirarchy)*/
    private StackPane sp1;
    /** Being added to the borderpane's left (contains command choices)*/
    private StackPane sp2;
    /** Used to drag/drop into a new position */
    private Point2D dragAnchor;
    /** Used to drag/drop into a new position */
    private double initX;
    /** Used to drag/drop into a new position */
    private double initY;
    /** Basic root used to add things to, then adding this to the scene */
    private Group root;
    /** Stackpane containing the choices for a TURN command*/
    private StackPane turnChoices;
    /** Stackpane containing the choices for an IFACCESS command*/
    private StackPane ifChoices;
    /** Stackpane containing the choices for a WHILEACCESS command*/
    private StackPane whileChoices;
    /** Stackpane containing the choices for a REPEAT command*/
    private StackPane repeatChoices;
    /** August Model object used in the MVC layout*/
    private AugustaModel model;
    /** Globalizing the stage */
    private Stage stage;

    /**
     *Init, happens before start()
     *
     * Creates the Model
     */
    public void init()
    {
        //Creating the model side
        this.model = new AugustaModel();
        //Adding the model as an observer of the view
        this.model.addObserver( this );
    }

    /**
     * Called directly after init()
     * This method starts the creation of the developer GUI, adding the scene to the stage,
     * and showing the stage.
     *
     * @param stage the highest piece of the hierarchy being displayed
     * @throws Exception
     */
    @Override
    public void start(Stage stage) throws Exception
    {
        this.stage = stage;
        //Initializing the root group
        root = new Group();
        //Creating the Borderpane, the two stackpanes, and an HBboxfor buttons
        //Plus adding special styles for each of these from the Styles.css file
        bp = new BorderPane();
        bp.getStyleClass().add("my_border");
        sp1 = new StackPane();
        sp1.getStyleClass().add("center_sp");
        sp2 = addCommands();
        sp2.getStyleClass().add("left_sp");
        HBox hb = this.addHBButtons();
        hb.getStyleClass().add("my_hori");

        //Creating the black border outline around the borderpane and stackpanes
        sp2.setBorder(new Border(new BorderStroke(Color.BLACK,
                BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(0, 3, 3, 0))));

        bp.setBorder(new Border(new BorderStroke(Color.BLACK,
                BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(3, 3, 3, 3))));

        sp1.setBorder(new Border(new BorderStroke(Color.BLACK,
                BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(0, 0, 3, 3))));

        //Sets the size of the Border's center pane (doesn't HAVE to be a stackpane)
        sp1.setMinSize(988, 722);

        //Adding stuff to the borderpane
        bp.setBottom(hb);
        bp.setCenter(sp1);
        bp.setLeft(sp2);

        //adding the borderpane to a group
        root.getChildren().addAll(bp);

        //adding the group to the scene
        Scene scene = new Scene(root);

        //scene styles
        scene.getStylesheets().add("developer/Styles.css");

        //Setting the scene of the stage
        stage.setScene(scene);

        //Setting up specifics of the stage itself
        stage.setTitle("Augusta Developer");
        stage.setResizable(false);
        stage.setHeight(800);
        stage.setWidth(1300);

        //Showing the stage
        stage.show();
    }

    /**
     * Adds all the dragable commands to the left stackpane
     *
     * @return the entire left stackpane, now fulling filled with the commands
     */
    private StackPane addCommands()
    {
        //Creating the stackpane on the left side of the borderpane that will
        // hold all the command buttons
        sp2 = new StackPane();
        sp2.setMinSize(300, 722);
        sp2.setAlignment(Pos.TOP_CENTER);

        //Adding each button to the stackpane using a method to create the buttons themselves
        //I did this so I could reuse these same methods to re-create the copies of the buttons
        //after I began to drag the originals.
        sp2.getChildren().addAll(createForward(), createTurn(),
                createIfCrumb(), createIfAccess(), createWhileAccess(),
                createRepeat(), createEatCrumb(), createDrop(), createNop(),
                createHalt(), createBegin(), createEnd(), createElse());

        return sp2;
    }

    /**
     * Adding two buttons to the bottom part of the GUI (Inside an HBox)
     *
     * @return the HBox completed with both it's buttons
     */
    private HBox addHBButtons()
    {
        //Creating the HBox that the "save" and "reset" buttons will be in
        //in the bottom of the borderpane
        HBox h = new HBox(5);
        h.setAlignment(Pos.BASELINE_CENTER);

        //Creating the save and reset buttons, adding their styles from the css Styles sheet
        Button save = new Button("Save");
        save.setMinWidth(100);
        save.getStyleClass().add("save_button");
        Button reset = new Button("Reset");
        reset.setMinWidth(100);
        reset.getStyleClass().add("reset_button");

        //resetting the fields of view + hashmap containing in-play commands
        reset.setOnMouseClicked(e->
        {
            //For each of the buttons in the Hashmap's keyset, remove the child from the stackpane.
            //This removes them from view.
            for(Button b: cmdList.keySet())
            {
                sp2.getChildren().remove(b);
            }

            //Clear the in=play command list so that the back-end matches the front-end view.
            cmdList.clear();

            //Resetting the Model's stores
            model.tree.clear();
            model.position = 0;
            model.commandsSorted.clear();

            //Remove all stackpanes, if any, of the choice buttons.  This means any choice buttons open
            //for REPEAT, IFACCESS, WHILEACCESS, or TURN will be removed when the reset button is pressed.
            root.getChildren().remove(turnChoices);
            root.getChildren().remove(ifChoices);
            root.getChildren().remove(whileChoices);
            root.getChildren().remove(repeatChoices);
        });

        save.setOnMouseClicked(e ->
        {
            //Throws an alert if there are no commands in play
            if(cmdList.isEmpty())
            {
                Alert alert = new Alert(AlertType.WARNING);
                alert.setTitle("Augusta Developer Warning");
                alert.setHeaderText("Invalid Command Chain");
                alert.setContentText("You must have at least one (valid) command to save!");

                alert.showAndWait();
            }
            //Otherwise, begins the saving process
            else
            {
                model.process(cmdList);
            }
        });

        //Adds the buttons to the HBox, returns the HBox
        h.getChildren().addAll(save, reset);

        return h;
    }

    /**
     * Creates a FORWARD command
     * This is used to create a duplicate button once it's dragged over to the right stackpane
     *
     * @return a new button with the FORWARD command details
     */
    private Button createForward()
    {
        Button btn = new Button(this.COMMANDS[0]);
        btn.setMinSize(200, 25);
        btn.setCursor(Cursor.HAND);
        btn.setTranslateY(40);
        btn.setTranslateY(45);
        btn.getStyleClass().add("btn_Forward");

        dragAndDrop(btn);

        return btn;
    }

    /**
     * Creates a TURN command
     * This is used to create a duplicate button once it's dragged over to the right stackpane
     *
     * @return a new button with the TURN command details
     */
    private Button createTurn()
    {
        Button btn = new Button(this.COMMANDS[1]);
        btn.setMinSize(200, 25);
        btn.setCursor(Cursor.HAND);
        btn.setTranslateY(80);
        btn.getStyleClass().add("btn_Turn");

        dragAndDrop(btn);

        //Special double click functionality that opens options to change it's parameters
        btn.setOnMouseClicked(e ->
        {
            if(e.getButton().equals(MouseButton.PRIMARY))
            {
                if(e.getClickCount() == 2)
                {
                    //Removes any previously opened similar stackpanes so they don't
                    //fill the screen up uneccesarily
                    root.getChildren().remove(turnChoices);
                    //Creates a new stackpane for the option buttons to rest in
                    turnChoices = new StackPane();
                    turnChoices.setTranslateX(initX+110);
                    turnChoices.setTranslateY(initY+25);
                    turnChoices.setAlignment(Pos.TOP_CENTER);

                    //turn right option
                    Button right = createChoiceButton();
                    right.setTranslateY(5);
                    right.setText("Right");

                    right.setOnMouseClicked(ev ->
                    {
                        btn.setText("TURN (RIGHT)");
                        root.getChildren().remove(turnChoices);
                    });

                    //turn left option
                    Button left = createChoiceButton();
                    left.setTranslateY(35);
                    left.setText("Left");

                    left.setOnMouseClicked(ev ->
                    {
                        btn.setText("TURN (LEFT)");
                        root.getChildren().remove(turnChoices);
                    });

                    Button ahead = createChoiceButton();
                    ahead.setTranslateY(65);
                    ahead.setText("Ahead");

                    ahead.setOnMouseClicked(ev ->
                    {
                        btn.setText("TURN (AHEAD)");
                        root.getChildren().remove(turnChoices);
                    });

                    Button behind = createChoiceButton();
                    behind.setTranslateY(95);
                    behind.setText("Behind");

                    behind.setOnMouseClicked(ev ->
                    {
                        btn.setText("TURN (BEHIND)");
                        root.getChildren().remove(turnChoices);
                    });

                    //adds the created buttons to the turnChoices stackpane
                    turnChoices.getChildren().addAll(right, left, ahead, behind);
                    //adds the newly created and populated turnChoices stackpane to the root group
                    root.getChildren().add(turnChoices);
                }
            }
        });

        //Returns the button itself
        return btn;
    }

    /**
     * Creates a if crumb command
     * This is used to create a duplicate button once it's dragged over to the right stackpane
     *
     * @return a new button with the if crumb command details
     */
    private Button createIfCrumb()
    {
        Button btn = new Button(this.COMMANDS[2]);
        btn.setMinSize(200, 25);
        btn.setCursor(Cursor.HAND);
        btn.setTranslateY(115);
        btn.getStyleClass().add("btn_IfCrumb");

        dragAndDrop(btn);

        return btn;
    }

    /**
     ** Creates a if access command
     * This is used to create a duplicate button once it's dragged over to the right stackpane
     *
     * @return a new button with the if access command details
     */
    private Button createIfAccess()
    {
        Button btn = new Button(this.COMMANDS[3]);
        btn.setMinSize(200, 25);
        btn.setCursor(Cursor.HAND);
        btn.setTranslateY(150);
        btn.getStyleClass().add("btn_IfAccess");

        dragAndDrop(btn);

        //Special double click functionality to open the list of button options
        btn.setOnMouseClicked(e ->
        {
            if(e.getButton().equals(MouseButton.PRIMARY))
            {
                if(e.getClickCount() == 2)
                {
                    //removes any previously if access choice buttons
                    root.getChildren().remove(ifChoices);
                    ifChoices = new StackPane();
                    ifChoices.setTranslateX(initX+110);
                    ifChoices.setTranslateY(initY+25);
                    ifChoices.setAlignment(Pos.TOP_CENTER);

                    //*****************************ACCESS AHEAD************************************

                    Button openAhead = createChoiceButton();
                    openAhead.setTranslateY(5);
                    openAhead.setText("Open Ahead");

                    openAhead.setOnMouseClicked(ev ->
                    {
                        btn.setText("IF ACCESS (OPEN AHEAD)");
                        root.getChildren().remove(ifChoices);
                    });

                    Button blockedAhead = createChoiceButton();
                    blockedAhead.setTranslateY(35);
                    blockedAhead.setText("Blocked Ahead");

                    blockedAhead.setOnMouseClicked(ev ->
                    {
                        btn.setText("IF ACCESS (BLOCKED AHEAD)");
                        root.getChildren().remove(ifChoices);
                    });

                    //*****************************ACCESS RIGHT************************************

                    Button openRight = createChoiceButton();
                    openRight.setTranslateY(65);
                    openRight.setText("Open Right");

                    openRight.setOnMouseClicked(ev ->
                    {
                        btn.setText("IF ACCESS (OPEN RIGHT)");
                        root.getChildren().remove(ifChoices);
                    });

                    Button blockedRight = createChoiceButton();
                    blockedRight.setTranslateY(95);
                    blockedRight.setText("Blocked Right");

                    blockedRight.setOnMouseClicked(ev ->
                    {
                        btn.setText("IF ACCESS (BLOCKED RIGHT)");
                        root.getChildren().remove(ifChoices);
                    });

                    //******************************ACCESS BEHIND***********************************

                    Button openBehind = createChoiceButton();
                    openBehind.setTranslateY(125);
                    openBehind.setText("Open Behind");

                    openBehind.setOnMouseClicked(ev ->
                    {
                        btn.setText("IF ACCESS (OPEN BEHIND)");
                        root.getChildren().remove(ifChoices);
                    });

                    Button blockedBehind = createChoiceButton();
                    blockedBehind.setTranslateY(155);
                    blockedBehind.setText("Blocked Behind");

                    blockedBehind.setOnMouseClicked(ev ->
                    {
                        btn.setText("IF ACCESS (BLOCKED BEHIND)");
                        root.getChildren().remove(ifChoices);
                    });

                    //******************************ACCESS LEFT***********************************

                    Button openLeft = createChoiceButton();
                    openLeft.setTranslateY(185);
                    openLeft.setText("Open Left");

                    openLeft.setOnMouseClicked(ev ->
                    {
                        btn.setText("IF ACCESS (OPEN LEFT)");
                        root.getChildren().remove(ifChoices);
                    });

                    Button blockedLeft = createChoiceButton();
                    blockedLeft.setTranslateY(215);
                    blockedLeft.setText("Blocked Left");

                    blockedLeft.setOnMouseClicked(ev ->
                    {
                        btn.setText("IF ACCESS (BLOCKED LEFT)");
                        root.getChildren().remove(ifChoices);
                    });
                    //adds all these buttons to the ifChoices stackpane
                    ifChoices.getChildren().addAll(openAhead, blockedAhead, openRight, blockedRight, openBehind,
                            blockedBehind, openLeft, blockedLeft);
                    //adds the ifChoices stackpane to the root group
                    root.getChildren().add(ifChoices);
                }
            }
        });

        return btn;
    }

    /**
     ** Creates a while access command
     * This is used to create a duplicate button once it's dragged over to the right stackpane
     *
     * @return a new button with the while access command details
     */
    private Button createWhileAccess()
    {
        Button btn = new Button(this.COMMANDS[4]);
        btn.setMinSize(200, 25);
        btn.setCursor(Cursor.HAND);
        btn.setTranslateY(185);
        btn.getStyleClass().add("btn_WhileAccess");

        dragAndDrop(btn);

        //If double clicked this button will open a list of options
        btn.setOnMouseClicked(e ->
        {
            if(e.getButton().equals(MouseButton.PRIMARY))
            {
                if(e.getClickCount() == 2)
                {
                    //removing any previously opened choice boxes
                    root.getChildren().remove(whileChoices);

                    whileChoices = new StackPane();
                    whileChoices.setTranslateX(initX+110);
                    whileChoices.setTranslateY(initY+25);
                    whileChoices.setAlignment(Pos.TOP_CENTER);

                    //*****************************************************************

                    Button openAhead = createChoiceButton();
                    openAhead.setTranslateY(5);
                    openAhead.setText("Open Ahead");

                    openAhead.setOnMouseClicked(ev ->
                    {
                        btn.setText("WHILE ACCESS (OPEN AHEAD)");
                        root.getChildren().remove(whileChoices);
                    });

                    Button blockedAhead = createChoiceButton();
                    blockedAhead.setTranslateY(35);
                    blockedAhead.setText("Blocked Ahead");

                    blockedAhead.setOnMouseClicked(ev ->
                    {
                        btn.setText("WHILE ACCESS (BLOCKED AHEAD)");
                        root.getChildren().remove(whileChoices);
                    });

                    //*****************************************************************

                    Button openRight = createChoiceButton();
                    openRight.setTranslateY(65);
                    openRight.setText("Open Right");

                    openRight.setOnMouseClicked(ev ->
                    {
                        btn.setText("WHILE ACCESS (OPEN RIGHT)");
                        root.getChildren().remove(whileChoices);
                    });

                    Button blockedRight = createChoiceButton();
                    blockedRight.setTranslateY(95);
                    blockedRight.setText("Blocked Right");

                    blockedRight.setOnMouseClicked(ev ->
                    {
                        btn.setText("WHILE ACCESS (BLOCKED RIGHT)");
                        root.getChildren().remove(whileChoices);
                    });

                    //*****************************************************************

                    Button openBehind = createChoiceButton();
                    openBehind.setTranslateY(125);
                    openBehind.setText("Open Behind");

                    openBehind.setOnMouseClicked(ev ->
                    {
                        btn.setText("WHILE ACCESS (OPEN BEHIND)");
                        root.getChildren().remove(whileChoices);
                    });

                    Button blockedBehind = createChoiceButton();
                    blockedBehind.setTranslateY(155);
                    blockedBehind.setText("Blocked Behind");

                    blockedBehind.setOnMouseClicked(ev ->
                    {
                        btn.setText("WHILE ACCESS (BLOCKED BEHIND)");
                        root.getChildren().remove(whileChoices);
                    });

                    //*****************************************************************

                    Button openLeft = createChoiceButton();
                    openLeft.setTranslateY(185);
                    openLeft.setText("Open Left");

                    openLeft.setOnMouseClicked(ev ->
                    {
                        btn.setText("WHILE ACCESS (OPEN LEFT)");
                        root.getChildren().remove(whileChoices);
                    });

                    Button blockedLeft = createChoiceButton();
                    blockedLeft.setTranslateY(215);
                    blockedLeft.setText("Blocked Left");

                    blockedLeft.setOnMouseClicked(ev ->
                    {
                        btn.setText("WHILE ACCESS (BLOCKED LEFT)");
                        root.getChildren().remove(whileChoices);
                    });

                    //*****************************************************************

                    //Adds all these buttons to the whileChoices stackpane
                    whileChoices.getChildren().addAll(openAhead, blockedAhead, openRight, blockedRight, openBehind,
                            blockedBehind, openLeft, blockedLeft);
                    //Adds the whileChoices stackpane to the root group
                    root.getChildren().add(whileChoices);
                }
            }
        });

        return btn;
    }

    /**
     ** Creates a repeat command
     * This is used to create a duplicate button once it's dragged over to the right stackpane
     *
     * @return a new button with the repeat command details
     */
    private Button createRepeat()
    {
        Button btn = new Button(this.COMMANDS[5]);
        btn.setMinSize(200, 25);
        btn.setCursor(Cursor.HAND);
        btn.setTranslateY(220);
        btn.getStyleClass().add("btn_Repeat");

        //adding the drag and drop mouse action events
        dragAndDrop(btn);

        //on double click
        btn.setOnMouseClicked(e ->
        {
            if(e.getButton().equals(MouseButton.PRIMARY))
            {
                if(e.getClickCount() == 2)
                {
                    //removes any previously opened repeatChoices button lists
                    root.getChildren().remove(repeatChoices);

                    repeatChoices = new StackPane();
                    repeatChoices.setTranslateX(initX+110);
                    repeatChoices.setTranslateY(initY+25);
                    repeatChoices.setAlignment(Pos.TOP_CENTER);

                    //************************************************
                    Button two = createChoiceButton();
                    two.setTranslateY(5);
                    two.setText("2");
                    addRepeatChoiceEvent(btn, two);

                    Button three = createChoiceButton();
                    three.setTranslateY(35);
                    three.setText("3");
                    addRepeatChoiceEvent(btn, three);

                    Button four = createChoiceButton();
                    four.setTranslateY(65);
                    four.setText("4");
                    addRepeatChoiceEvent(btn, four);

                    Button five = createChoiceButton();
                    five.setTranslateY(95);
                    five.setText("5");
                    addRepeatChoiceEvent(btn, five);

                    Button six = createChoiceButton();
                    six.setTranslateY(125);
                    six.setText("6");
                    addRepeatChoiceEvent(btn, six);

                    Button seven = createChoiceButton();
                    seven.setTranslateY(155);
                    seven.setText("7");
                    addRepeatChoiceEvent(btn, seven);

                    Button eight = createChoiceButton();
                    eight.setTranslateY(185);
                    eight.setText("8");
                    addRepeatChoiceEvent(btn, eight);

                    Button nine = createChoiceButton();
                    nine.setTranslateY(215);
                    nine.setText("9");
                    addRepeatChoiceEvent(btn, nine);

                    //***********************************************

                    //adding all the buttons to the repeatChoices stackpane
                    repeatChoices.getChildren().addAll(two, three, four, five, six, seven, eight, nine);
                    //adding the stackpane to the root group
                    root.getChildren().add(repeatChoices);
                }
            }
        });

        return btn;
    }

    /**
     ** Creates a eat crumb command
     * This is used to create a duplicate button once it's dragged over to the right stackpane
     *
     * @return a new button with the eat crumb command details
     */
    private Button createEatCrumb()
    {
        Button btn = new Button(this.COMMANDS[6]);
        btn.setMinSize(200, 25);
        btn.setCursor(Cursor.HAND);
        btn.setTranslateY(255);
        btn.getStyleClass().add("btn_EatCrumb");

        dragAndDrop(btn);

        return btn;
    }

    /**
     ** Creates a drop command
     * This is used to create a duplicate button once it's dragged over to the right stackpane
     *
     * @return a new button with the drop command details
     */
    private Button createDrop()
    {
        Button btn = new Button(this.COMMANDS[7]);
        btn.setMinSize(200, 25);
        btn.setCursor(Cursor.HAND);
        btn.setTranslateY(290);
        btn.getStyleClass().add("btn_Drop");

        dragAndDrop(btn);

        return btn;
    }

    /**
     ** Creates a No Operation command
     * This is used to create a duplicate button once it's dragged over to the right stackpane
     *
     * @return a new button with the No Operation command details
     */
    private Button createNop()
    {
        Button btn = new Button(this.COMMANDS[8]);
        btn.setMinSize(200, 25);
        btn.setCursor(Cursor.HAND);
        btn.setTranslateY(325);
        btn.getStyleClass().add("btn_NOP");

        dragAndDrop(btn);

        return btn;
    }

    /**
     ** Creates a HALT command
     * This is used to create a duplicate button once it's dragged over to the right stackpane
     *
     * @return a new button with the HALT command details
     */
    private Button createHalt()
    {
        Button btn = new Button(this.COMMANDS[9]);
        btn.setMinSize(200, 25);
        btn.setCursor(Cursor.HAND);
        btn.setTranslateY(360);
        btn.getStyleClass().add("btn_Halt");

        dragAndDrop(btn);

        return btn;
    }

    /**
     ** Creates a begin command
     * This is used to create a duplicate button once it's dragged over to the right stackpane
     *
     * @return a new button with the begin command details
     */
    private Button createBegin()
    {
        Button btn = new Button(this.COMMANDS[10]);
        btn.setMinSize(200, 25);
        btn.setCursor(Cursor.HAND);
        btn.setTranslateY(395);
        btn.getStyleClass().add("btn_Begin");

        dragAndDrop(btn);

        return btn;
    }

    /**
     ** Creates a end command
     * This is used to create a duplicate button once it's dragged over to the right stackpane
     *
     * @return a new button with the end command details
     */
    private Button createEnd()
    {
        Button btn = new Button(this.COMMANDS[11]);
        btn.setMinSize(200, 25);
        btn.setCursor(Cursor.HAND);
        btn.setTranslateY(430);
        btn.getStyleClass().add("btn_End");

        dragAndDrop(btn);
        return btn;
    }

    /**
     ** Creates a else command
     * This is used to create a duplicate button once it's dragged over to the right stackpane
     *
     * @return a new button with the else command details
     */
    private Button createElse()
    {
        Button btn = new Button(this.COMMANDS[12]);
        btn.setMinSize(200, 25);
        btn.setCursor(Cursor.HAND);
        btn.setTranslateY(465);
        btn.getStyleClass().add("btn_Else");

        dragAndDrop(btn);
        return btn;
    }

    /**
     * Used to add all the drag and drop mouse action events to the buttons that are passed into it
     *
     * @param btn a button that requires the drag and drop functionality
     */
    private void dragAndDrop(Button btn)
    {
        //When the mouse button is pressed
        btn.setOnMousePressed(e ->
        {
            //remove any choice stackpanes that are on the screen so they don't crowd the screen
            root.getChildren().remove(turnChoices);
            root.getChildren().remove(ifChoices);
            root.getChildren().remove(whileChoices);
            root.getChildren().remove(repeatChoices);

            //when mouse is pressed, store initial position
            initX = btn.getTranslateX();
            initY = btn.getTranslateY();
            dragAnchor = new Point2D(e.getSceneX(), e.getSceneY());

            //if the button that was picked up was initially on the left side of the screen, create a replacement button
            if(initX < 257)
            {
                replaceButton(btn.getText());
            }
        });

        //When the mouse is dragged
        btn.setOnMouseDragged(e ->
        {
            //Happens rapidly... grabs new X and Y, and sets the button's new position
            double dragX = e.getSceneX() - dragAnchor.getX();
            double dragY = e.getSceneY() - dragAnchor.getY();
            //calculate new position of the circle
            double newXPosition = initX + dragX;
            double newYPosition = initY + dragY;
            //translate to this position
            btn.setTranslateX(newXPosition);
            btn.setTranslateY(newYPosition);
        });

        //When the mouse is released
        btn.setOnMouseReleased(e->
        {
            //Places the button into the Hashmap if it is not already present
            //and will update the Y value if it is already present
            cmdList.put(btn, btn.getTranslateY());

            //If the button was dropped on the left side of the screen, remove it from the running hashmap
            //of commands, and remove it from the stackpane (from sight)
            if(btn.getTranslateX() <= 257)
            {
                cmdList.remove(btn);
                sp2.getChildren().remove(btn);
            }
        });
    }

    /**
     * Used to replace buttons once one is picked up from the left side of the screen
     *
     * @param s the text of the button, so we know which type of button to create a duplicate of
     */
    private void replaceButton(String s)
    {
        if(s.equals(COMMANDS[0]))
        {
            Button btn = createForward();
            sp2.getChildren().add(btn);
        }
        else if(s.equals(COMMANDS[1]))
        {
            Button btn = createTurn();
            sp2.getChildren().add(btn);
        }
        else if(s.equals(COMMANDS[2]))
        {
            Button btn = createIfCrumb();
            sp2.getChildren().add(btn);
        }
        else if(s.equals(COMMANDS[3]))
        {
            Button btn = createIfAccess();
            sp2.getChildren().add(btn);
        }
        else if(s.equals(COMMANDS[4]))
        {
            Button btn = createWhileAccess();
            sp2.getChildren().add(btn);
        }
        else if(s.equals(COMMANDS[5]))
        {
            Button btn = createRepeat();
            sp2.getChildren().add(btn);
        }
        else if(s.equals(COMMANDS[6]))
        {
            Button btn = createEatCrumb();
            sp2.getChildren().add(btn);
        }
        else if(s.equals(COMMANDS[7]))
        {
            Button btn = createDrop();
            sp2.getChildren().add(btn);
        }
        else if(s.equals(COMMANDS[8]))
        {
            Button btn = createNop();
            sp2.getChildren().add(btn);
        }
        else if(s.equals(COMMANDS[9]))
        {
            Button btn = createHalt();
            sp2.getChildren().add(btn);
        }
        else if(s.equals(COMMANDS[10]))
        {
            Button btn = createBegin();
            sp2.getChildren().add(btn);
        }
        else if(s.equals(COMMANDS[11]))
        {
            Button btn = createEnd();
            sp2.getChildren().add(btn);
        }
        else if(s.equals(COMMANDS[12]))
        {
            Button btn = createElse();
            sp2.getChildren().add(btn);
        }
    }

    /**
     * Short method used to shorten the code within button creation methods that require parameter choice buttons
     *
     * @return a basic button with universally required attributes of a choice button
     */
    private Button createChoiceButton()
    {
        Button btn = new Button();

        btn.setMinSize(80, 25);
        btn.getStyleClass().add("choice_buttons");
        btn.setCursor(Cursor.HAND);

        return btn;
    }

    /**
     * A short method used to add clicked mouse events to the repeat choice events
     *
     * @param btn The actually repeat command node button
     * @param btn2 The button being created in the repeatChoices stackpane
     */
    private void addRepeatChoiceEvent(Button btn, Button btn2)
    {
        //if clicked
        btn2.setOnMouseClicked(e ->
        {
            //set the button's text
            btn.setText("REPEAT " + "(" + btn2.getText() + ")" );
            //remove the repeatChoices stackpane from visibility
            root.getChildren().remove(repeatChoices);
        });
    }

    /**
     * During the saving sequence, this communicates with the model to know when the filechooser window needs to open
     *
     * @param o This
     * @param arg The FileChooser object
     */
    @Override
    public void update(Observable o, Object arg)
    {
        FileChooser fileChooser = (FileChooser) arg;

        fileChooser.setTitle("Open Augusta File");

        //Calls the final save() method within the model to save the file (sending it the
        // filename that was chose by the user)
        this.model.save(fileChooser.showOpenDialog(stage));
    }
}
