import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javafx.application.Platform;

public class Player implements Runnable {
    // PLAYER DATA
    private BlockingQueue<Shot> shots = new ArrayBlockingQueue<>(10);
    private int score = 0;

    @Override
    public void run() {
        System.out.println("Player thread created");
        try {
            while (true) {
                // take out the next queued shot and shoot it
                executeShot();
                // update player's score 
                score += 10; // score increases by 10 per second
                updateScore();
                // sleep thread for 1 second
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            Platform.runLater(() -> {
                UIElements ui = UIElements.getInstance();
                ui.getLogger().appendText("You have died. Robots win!\n");
                // TODO: print player's score on GUI
            });
            System.out.println("Player thread has exited");
        }
    }

    public void queueShot(Shot newShot) {
        if (!shots.offer(newShot)) {
            System.out.println("Failed to add shot to queue (queue is full with max shots at 10)");
        }
    }

    /**
     * Takes a shot out from the blocking queue and attempts to fire
     * it. If the shot successfully hit a Robot, add to the player's
     * existing score
     * @throws InterruptedException
     */
    private void executeShot() throws InterruptedException {
        Grid grid = ThreadController.getInstance().getGrid();
        Shot nextShot = shots.poll();
        
        if (nextShot != null) {
            System.out.println("Shot was fired!");
            Robot killedRobot = grid.fireShot(nextShot);
            // if the shot killed a robot
            if (killedRobot != null) {
                long t = System.currentTimeMillis() - nextShot.getCreatedAt();
                score += 10 + 100 * (t / killedRobot.getMovementDelay()); // 10 + 100 * (t / d)
            }
        }
    }

    /**
     * Updates the player's score on the GUI
     */
    private void updateScore() {
        Platform.runLater(() -> {
            UIElements ui = UIElements.getInstance();
            ui.getScore().setText(String.format("Score: %d", score));
        });
    }
}
