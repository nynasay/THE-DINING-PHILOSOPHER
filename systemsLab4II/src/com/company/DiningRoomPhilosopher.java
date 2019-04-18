package com.company;//import java.util.concurrent.locks.Lock;
//import java.util.concurrent.locks.Condition;
//import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.*;
import java.util.concurrent.Semaphore;

public class DiningRoomPhilosopher {

    //MUST DELETE!!!!
    //public static int philosopherNum;

    //INITIALIZING GLOBAL VARIABLES
    public static int PHILTOTAL = 5;
    public static boolean THINKING, HUNGRY, EATING;
    //public static int LEFT = (philosopherNum + 4) % PHILTOTAL;
    //public static int RIGHT = (philosopherNum+ 1) % PHILTOTAL;

    public static boolean thinkingState[] = new boolean[PHILTOTAL];
    public static boolean hungryState[] = new boolean[PHILTOTAL];
    public static boolean eatingState[] = new boolean[PHILTOTAL];
    public static TimeUnit time = TimeUnit.SECONDS;

    //INITIALIZING SEMAPHORES
    public static Semaphore mutex = new Semaphore(1);
    public static Semaphore[] PHILSEMP = new Semaphore[PHILTOTAL];

    //CHECKING IF THE PHILOSPHER IS HUNGRY AND ABLE TO EAT
    public static void readyToEat(int philosopherNum){
        int LEFT = (philosopherNum + 4) % PHILTOTAL;
        int RIGHT = (philosopherNum+ 1) % PHILTOTAL;
        if(hungryState[philosopherNum] && !eatingState[LEFT] && !eatingState[RIGHT]){
            eatingState[philosopherNum] = true;

            try {
                time.sleep(2); //not sure what this is?
            } catch (InterruptedException e){
                System.out.println("Interrupted while trying to sleep");
            }

            System.out.println("Philosopher " + (philosopherNum+ 1) + " takes fork " + (LEFT + 1) + " and " + (philosopherNum +1));
            System.out.println("Philosopher " + (philosopherNum+ 1) + " is eating");

            PHILSEMP[philosopherNum].release();
            //USED TO WAKE UP HUNGRY PHILOSOPHERS DURING RETURNFORKS
            //sem_post(&S[phnum]);
            /*try{
                // GET A PERMIT FOR THE SEMAPHORE
                System.out.println("Philosopher " + philNum + " is waiting for a permit.");

                mutex.acquire();

                System.out.println("Philosopher " + philNum + " gets a permit.");
            }catch (InterruptedException exc) {
                System.out.println(exc);
            }*/
        }
    }

    //CALLED BY A PHILOSOPHER WHEN THEY WISH TO EAT
    public static void takeForks(int philosopherNumber){
        int LEFT = (philosopherNumber + 4) % PHILTOTAL;
        int RIGHT = (philosopherNumber+ 1) % PHILTOTAL;
        mutex.tryAcquire();

        //Set the state of philosopher to hungry
        hungryState[philosopherNumber] = true;
        System.out.println("Philosopher " +(philosopherNumber + 1)+ " is hungry");

        readyToEat(philosopherNumber);

        // GET A PERMIT FOR THE SEMAPHORE
        mutex.release();

        PHILSEMP[philosopherNumber].tryAcquire();

        try {
            time.sleep(1); //not sure what this is?
        } catch (InterruptedException e){
            System.out.println("Interrupted while trying to sleep");
        }

        /*try{
            // GET A PERMIT FOR THE SEMAPHORE
            PHILSEMP[philosopherNumber].acquire();
        }catch (InterruptedException exc) {
            System.out.println(exc);
        }*/
    }

    //CALLED BY A PHILOSPHER  WHEN THEY'RE DONE EATING
    public static void returnForks(int philosopherNumber){
        int LEFT = (philosopherNumber + 4) % PHILTOTAL;
        int RIGHT = (philosopherNumber + 1) % PHILTOTAL;
        //sem_wait(&mutex);
        //time.sleep(mutex);?
        PHILSEMP[philosopherNumber].tryAcquire();
        //SAY THAT THE PHILOSPHER IS THINKING
        thinkingState[philosopherNumber] = true;
        System.out.println("Philospher " + (philosopherNumber + 1) + " is putting fork " + (LEFT + 1) + " and " + (philosopherNumber + 1) + " down");
        System.out.println("Philospher " + (philosopherNumber + 1) + " is thinking");
        readyToEat(LEFT);
        readyToEat(RIGHT);
        //sem_wait(&mutex);
        PHILSEMP[philosopherNumber].release();
    }
    public static void startPhilosopher (int n){
        while(true){
            int i = n;
            try{
                time.sleep(1);
            } catch (InterruptedException e){
                System.out.println("Interrupted while trying to sleep");
            }
            takeForks(i);
            try{
                time.sleep(0);
            } catch (InterruptedException e){
                System.out.println("Interrupted while trying to sleep");
            }
            returnForks(i);

        }

    }
    public static void main (String[] args){
        for( int i = 0 ; i < PHILTOTAL; i++){
            PHILSEMP[i] = new Semaphore(0);
        }
        //Create threads
        Thread philThreads[] = new Thread[PHILTOTAL];
        for (int j = 0; j < PHILTOTAL; j++) {
            philThreads[j] = new Thread();
            philThreads[j].start();
            System.out.println("Philosopher " + (j+1) + " is thinking.");

        }
        for (int i = 0; i< PHILTOTAL; i++){
            startPhilosopher(i);
            try {
                philThreads[i].join();
                startPhilosopher(i);
            }
            catch (InterruptedException e){
                e.printStackTrace();
            }
        }

    }

}
