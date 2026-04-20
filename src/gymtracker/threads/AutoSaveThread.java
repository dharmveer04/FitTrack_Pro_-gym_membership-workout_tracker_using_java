package gymtracker.threads;

import gymtracker.filehandling.DataManager;
import gymtracker.services.MemberService;
import gymtracker.services.WorkoutService;

import java.io.IOException;
import java.time.LocalTime;

/**
 * Background daemon thread that periodically persists all data.
 * Demonstrates: Multithreading — custom Thread subclass, daemon thread,
 *               volatile flag for graceful shutdown, callback notification.
 */
public class AutoSaveThread extends Thread {

    private final DataManager   dataManager;
    private final MemberService memberService;
    private final WorkoutService workoutService;
    private final int            intervalSeconds;
    private volatile boolean     running = true;
    private Runnable             onSaveCallback;

    public AutoSaveThread(DataManager dataManager,
                          MemberService memberService,
                          WorkoutService workoutService,
                          int intervalSeconds) {
        this.dataManager     = dataManager;
        this.memberService   = memberService;
        this.workoutService  = workoutService;
        this.intervalSeconds = intervalSeconds;
        setDaemon(true);            // dies automatically when the JVM exits
        setName("AutoSave-Thread");
    }

    /** Optional callback executed on the calling thread after each successful save. */
    public void setOnSaveCallback(Runnable callback) {
        this.onSaveCallback = callback;
    }

    /** Signal the thread to stop after its current sleep. */
    public void stopSaving() {
        running = false;
        interrupt();
    }

    @Override
    public void run() {
        System.out.println("[AutoSave] Started — saving every " + intervalSeconds + " seconds.");
        while (running) {
            try {
                Thread.sleep(intervalSeconds * 1000L);
                if (running) save();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        System.out.println("[AutoSave] Thread stopped.");
    }

    /** Perform an immediate save (safe to call from any thread). */
    public synchronized void save() {
        try {
            dataManager.saveMembers(memberService.getAllMembers());
            dataManager.saveWorkouts(workoutService.getAllWorkouts());
            System.out.println("[AutoSave] Saved at " + LocalTime.now().toString().substring(0, 8));
            if (onSaveCallback != null) onSaveCallback.run();
        } catch (IOException e) {
            System.err.println("[AutoSave] Save failed: " + e.getMessage());
        }
    }
}
