import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javafx.application.Platform;

public class Player implements Runnable {
    // PLAYER DATA
    private BlockingQueue<Shot> shots = new ArrayBlockingQueue<>(10);

    @Override
    public void run() {
        System.out.println("Player thread created");
        try {
            while (true) {
                // take out the next queued shot and shoot it
                executeShot();
                // sleep thread for 1 second
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            Platform.runLater(() -> {
                UIElements ui = UIElements.getInstance();
                ui.getLogger().appendText("You have died. Robots win!\n");
            });
            System.out.println("Player thread has exited");
        }
    }

    public void queueShot(Shot newShot) {
        shots.add(newShot);
    }

    private void executeShot() throws InterruptedException {
        shots.take();
        
    }
}
