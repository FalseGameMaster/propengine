package dev.falsegamemaster.propengine.util;

import org.bukkit.scheduler.BukkitRunnable;

public class TimedTask extends BukkitRunnable {

    private final Runnable runnable;
    private final int durationTicks;
    private int ticks = 0;

    public TimedTask(Runnable runnable, int durationTicks) {
        this.runnable = runnable;
        this.durationTicks = durationTicks;
    }

    @Override
    public void run() {
        runnable.run();
        ticks ++;
        if (ticks >= durationTicks) cancel();
    }

}
