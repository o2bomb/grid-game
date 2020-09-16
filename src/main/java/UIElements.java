import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

/**
 * A thread-safe singleton class that contains the logger TextArea object and score
 * Label object. This object is frequently used by the GUI, player, grid, and robot
 * threads
 */
public class UIElements {
    // SINGLETON INSTANCE
    private static UIElements instance = null;

    // JAVAFX UI ELEMENTS
    private JFXArena arena;
    private TextArea logger;
    private Label score;

    private UIElements() {
        arena = new JFXArena();
        logger = new TextArea();
        score = new Label("Score: 0");
    }

    public static UIElements getInstance() {
        if (instance == null) {
            synchronized (UIElements.class) {
                if (instance == null) {
                    System.out.println("UIElements object initialized!");
                    instance = new UIElements();
                }
            }
        }

        return instance;
    }

    public JFXArena getArena() {
        return this.arena;
    }

    public TextArea getLogger() {
        return this.logger;
    }

    public Label getScore() {
        return this.score;
    }
}
