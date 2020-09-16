import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class App extends Application 
{
    public static void main(String[] args) 
    {
        launch();        
    }
    
    @Override
    public void start(Stage stage) 
    {
        stage.setTitle("Grid Game by Yong Yang Tan (19154970)");
        
        ThreadController.getInstance().start();   // start the grid and player thread
        UIElements guiStuff = UIElements.getInstance(); // reference to various UI elements
        guiStuff.getArena().addListener((x, y) ->
        {
            // queue's a shot when the player clicks on a grid square
            System.out.println("Arena click at (" + x + "," + y + ")");
            Player player = ThreadController.getInstance().getPlayer();
            Shot newShot = new Shot(x, y);
            player.queueShot(newShot);
        });
        
        ToolBar toolbar = new ToolBar();
        toolbar.getItems().addAll(guiStuff.getScore());
                
        SplitPane splitPane = new SplitPane();
        splitPane.getItems().addAll(guiStuff.getArena(), guiStuff.getLogger());
        guiStuff.getArena().setMinWidth(300.0);
        
        BorderPane contentPane = new BorderPane();
        contentPane.setTop(toolbar);
        contentPane.setCenter(splitPane);
        
        Scene scene = new Scene(contentPane, 800, 800);
        stage.setScene(scene);
        stage.show();
    }
}
