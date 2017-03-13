import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Optional;

/**
 * The main program for the TentsAndTrees puzzle.
 * 
 * To run with a puzzle file and debugging enabled:<br>
 * <tt><br>
 *     java TentsAndTrees tents1.txt true<br>
 * </tt><br>
 * To run with a puzzle file and debugging disabled:<br>
 * <tt><br>
 *     java TentsAndTrees tents1.txt false<br>
 * </tt><br>
 * @author sps (Sean Strout @ RIT CS)
 *
 */
public class TentsAndTrees extends Application {
    /** List of logged Configurations paired with their respective debug messages */
    private static List<Pair<Configuration,String>> configLog;

    /** JavaFX Nodes that need to be updated with each config */
    private TextArea textView;
    private Text currentConfig;
    private Text isValid;
    private Text isGoal;

    /** GUI Constants */
    public static final int DEFAULT_PADDING = 15;
    public static final int DEFAULT_SPACING = 5;
    public static final Font DEFAULT_FONT = Font.font("Monospaced",14);

    /**
     * The main method.
     * 
     * @param args The command line arguments (name of input file)
     * @throws FileNotFoundException if file not found
     */
    public static void main(String[] args) throws FileNotFoundException {
        // check for file name and debug flag on command line
        if (args.length != 2) {
            System.err.println("Usage: java TentsAndTrees input-file debug");
        } else {
            // construct the initial configuration from the file
            Configuration init = new TentConfig(args[0]);

            // create the backtracker with the debug flag
            boolean debug = args[1].equals("true");
            Backtracker bt = new Backtracker(debug);

            // start the clock
            double start = System.currentTimeMillis();

            // attempt to solve the puzzle
            Optional<Configuration> sol = bt.solve(init);

            // compute the elapsed time
            System.out.println("Elapsed time: " +
                    (System.currentTimeMillis() - start)/1000.0 + " seconds.");

            // indicate whether there was a solution, or not
            if (sol.isPresent()) {
                System.out.println("Solution:\n" + sol.get());
            } else {
                System.out.println("No solution!");
            }

            // launch visualizer if debug enabled
            if (debug) {
                configLog = bt.getConfigLog();
                Application.launch(args);
            }
        }
    }

    /**
     * JavaFX start method.  Builds and displays the debugger GUI.
     */
    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("Tents Debugger");
        Scene scene = new Scene(buildRoot());
        setCurrentConfig(0);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Build the root node for the debugger GUI.
     * @return root Node (BorderPane)
     */
    private BorderPane buildRoot() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(DEFAULT_PADDING));
        root.setCenter(buildPuzzleView());
        root.setRight(buildInfoPanel());
        root.setBottom(buildSlider());
        return root;
    }

    /**
     * Build the text area containing the string representation of the puzzle.
     * Display's the results of the user's toString method.
     * @return text area for puzzle string representation
     */
    private TextArea buildPuzzleView() {
        textView = new TextArea();
        textView.setFont(DEFAULT_FONT);
        textView.setEditable(false);
        return textView;
    }

    /**
     * Build the info panel containing debugging info about the current Configuration on display.
     * Displays the results of the user's isValid and isGoal methods.
     * @return info panel
     */
    private VBox buildInfoPanel() {
        // Parent VBox, containing title text and the rest of the debug info
        VBox infoPanelBox = new VBox();
        infoPanelBox.setSpacing(DEFAULT_SPACING * 2);
        infoPanelBox.setPadding(new Insets(DEFAULT_PADDING));

        // Title text
        Text titleText = new Text("- Tents Debugger - ");
        titleText.setFont(DEFAULT_FONT);

        // Debug info VBox
        VBox debugInfoBox = new VBox();
        debugInfoBox.setSpacing(DEFAULT_SPACING);

        // Current Configuration index Label
        Label currentConfigLabel = new Label(String.format("%-8s","config:"));
        currentConfigLabel.setTextAlignment(TextAlignment.RIGHT);
        currentConfigLabel.setFont(DEFAULT_FONT);

        // Current Configuration index
        currentConfig = new Text(String.format("%18s","0"));
        currentConfig.setFont(DEFAULT_FONT);

        debugInfoBox.getChildren().add(new HBox(currentConfigLabel, currentConfig));

        // isValid label
        Label isValidLabel = new Label(String.format("%-8s","isValid:"));
        isValidLabel.setTextAlignment(TextAlignment.RIGHT);
        isValidLabel.setFont(DEFAULT_FONT);

        // isValid result
        isValid = new Text(String.format("%18s","-"));
        isValid.setFont(DEFAULT_FONT);

        debugInfoBox.getChildren().add(new HBox(isValidLabel, isValid));

        // isGoal label
        Label isGoalLabel = new Label(String.format("%-8s","isGoal:"));
        isGoalLabel.setTextAlignment(TextAlignment.RIGHT);
        isGoalLabel.setFont(DEFAULT_FONT);

        // isGoal result
        isGoal = new Text(String.format("%18s","-"));
        isGoal.setFont(DEFAULT_FONT);

        debugInfoBox.getChildren().add(new HBox(isGoalLabel, isGoal));

        infoPanelBox.getChildren().addAll(titleText,debugInfoBox);
        return infoPanelBox;
    }

    /**
     * Build the Configuration control Slider for moving between individual Configurations.
     * @return control Slider
     */
    private Slider buildSlider() {
        // Build the Slider with a range of 0 to the total number of configs minus one
        Slider slider = new Slider(0, configLog.size() - 1, 0);
        slider.setPadding(new Insets(DEFAULT_PADDING / 5));
        slider.setBlockIncrement(1);
        slider.setMajorTickUnit(10);
        slider.setMinorTickCount(10);
        slider.setSnapToTicks(true);

        // Add a listener to update the debug info when the Slider's value changes
        slider.valueProperty().addListener(onSliderChanged());

        // Request focus so the Slider is selected automatically
        Platform.runLater(slider::requestFocus);

        return slider;
    }

    /**
     * ChangeListener for the Configuration control Slider.
     * Sets the current Configuration on display when the Slider's value changes.
     * @return ChangeListener for control Slider
     */
    private ChangeListener<Number> onSliderChanged() {
        return (observable, oldValue, newValue) -> setCurrentConfig(newValue.intValue());
    }

    /**
     * Set the current Configuration on display to the Configuration at the given index in the configLog.
     * @param configIndex index of the Configuration to display
     */
    private void setCurrentConfig(int configIndex) {
        Pair<Configuration,String> entry = configLog.get(configIndex);
        Configuration config = entry.getKey();
        if (config != null) {
            // Update the values in the debugger GUI
            currentConfig.setText(String.format("%18s",entry.getValue()));
            try {
                isValid.setText(String.format("%18s",Boolean.toString(config.isValid())));
                isGoal.setText(String.format("%18s",Boolean.toString(config.isGoal())));
            } catch (ArrayIndexOutOfBoundsException ignored){} // Catches exceptions from cursor being out of bounds

            String configString = config.toString();

            // Update the Configuration in the puzzle view, setting the pref size to fit the whole string
            textView.setText(configString);
            textView.setPrefColumnCount(computeWidth(configString));
            textView.setPrefRowCount(computeHeight(configString));
        }
    }

    /**
     * Compute the width of the config string in number of chars
     * @param configString string representing the Configuration to print
     *                     (generated with the user's toString() method)
     * @return width of string
     */
    private int computeWidth(String configString) {
        int width = 0;
        int count = 0;
        char c;
        for (int i = 0; i < configString.length(); i++) {
            c = configString.charAt(i);
            if ( c == '\n' ) {
                if (count > width) width = count;
                count = 0;
            } else count++;
        }
        return width;
    }

    /**
     * Compute the height of the config string in number of chars
     *
     * @param configString string representing the Configuration to print
     *                     (generated with the user's toString() method)
     * @return height of string
     */
    private int computeHeight(String configString) {
        int height = 0;
        char c;
        for (int i = 0; i < configString.length(); i++) {
            c = configString.charAt(i);
            if ( c == '\n' ) height++;
        }
        return height;
    }
}
