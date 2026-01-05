package com.example.demo;

import org.springframework.stereotype.Service;

@Service
public class TimerService {

    private int timeRemaining;
    private boolean isTimeUp;
    private Thread timerThread;

    public void startTimer(int seconds) {
        this.timeRemaining = seconds;
        this.isTimeUp = false;

     
        if (timerThread != null && timerThread.isAlive()) {
            timerThread.interrupt();
        }

        timerThread = new Thread(() -> {
            try {
                while (timeRemaining > 0) {
                    Thread.sleep(1000); 
                    timeRemaining--;
                }
                isTimeUp = true;
            } catch (InterruptedException e) {
                
            }
        });

        timerThread.start();
    }

    public int getTimeRemaining() {
        return timeRemaining;
    }

    public boolean isTimeUp() {
        return isTimeUp;
    }
    

    public void stopTimer() {
        if (timerThread != null) {
            timerThread.interrupt();
        }
    }
}