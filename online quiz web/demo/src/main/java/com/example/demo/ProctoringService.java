package com.example.demo;

import org.springframework.stereotype.Service;

@Service
public class ProctoringService {

    public void startProctoring(String studentName) {
        Thread proctorThread = new Thread(() -> {
            try {
                for (int i = 0; i < 10; i++) { 
                    System.out.println("Monitoring student: " + studentName + " | Checking webcam...");
                    Thread.sleep(5000); 
                }
            } catch (InterruptedException e) {
                System.out.println("Proctoring stopped.");
            }
        });
        proctorThread.start();
    }
}