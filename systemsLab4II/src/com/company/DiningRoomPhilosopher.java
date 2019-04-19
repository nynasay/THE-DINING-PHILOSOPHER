package com.company;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
//http://www.java2s.com/Tutorials/Java/Java_Thread_How_to/Concurrent/Solve_dining_philosophers_monitors.htm

public class DiningRoomPhilosopher {

    //CLASS THAT HOLDS THE FORK SEMAPHORE OBJECT
    static class Fork{
        //WHETHER THE FORK IS AVAILABLE
        private boolean availabile;
        public Fork (){
            availabile = true;
        }

        //RETURNS A BOOLEAN THAT SHOWS IF THE FORK SEMAPHORE IS AVAILABLE
        public boolean getAvailability(){
            return availabile;
        }

        //SET THE SEMAPHORE'S AVAILABILITY
        public void setAvailable(boolean x){
            availabile = x;
        }
    }

    //CLASS THAT HOLDS AND DETERMINES THE CONDITION OF THE PHILOSOPHER
    static class State{
        //CREATING A NEW MUTEX THAT WILL BE MODIFIED BASED ON THE PHILOSOPHER'S CONDITION
        Lock mutex = new ReentrantLock();

        //Conditions for the 5 philosophers to prevent starvation
        Condition [] conditions = new Condition[5];
        String [] currentState = new String[5];
        int [] philosopherNum = new int [5];

        //THE STATE OF THE PHILOSOPHER IS INITIALIZED AS THINKING
        public State (){
            for ( int i = 0 ; i < 5; i ++){
                philosopherNum[i] = i;
                currentState[i] = "thinking";
                conditions[i] = mutex.newCondition();
            }
        }

        //SET THE GIVEN PHILOSOPHER TO A PARTICULAR STATE
        public void setState (int philosphoer, String state){
            currentState[philosphoer] = state;
        }

        //ALLOWS THE PHILOSOPHER TO GRAD A FORK ONCE THEY ARE AVAILABLE
        public void grabFork ( int philosopherIndex, Fork left, Fork right){
            mutex.lock();
            try{
                setState(philosopherIndex, "hungry");
                System.out.println("Philosopher " + (philosopherIndex +1) +" is hungry");

                //The philospher has to wait until the forks become available
                while (!left.getAvailability() || !right.getAvailability()){
                    conditions[philosopherIndex].await();
                }

                //Once both left and right has become available, they have to be
                //set to false since the philosopher is using them
                left.setAvailable(false);
                right.setAvailable(false);

                //CHANGE THE PHILOSOPHER'S STATE TO EATING AND SHOW WHAT FORKS ARE BEING USED
                //setState(philosopherIndex, "eating");
                int leftFork = (((philosopherIndex+1) + 4) % 5);
                int rightFork = philosopherIndex + 1;
                System.out.println("Philosopher " + (philosopherIndex+1) + " takes fork " + (leftFork) + " and " + rightFork);
                System.out.println("Philosopher " + (philosopherIndex+1) + " is " + currentState[philosopherIndex]);

            }catch(Exception e){
                e.printStackTrace();
            }
            mutex.unlock();
        }

        //ALLOWS THE PHILOSOPHER TO PUT THE FORK DOWN ONCE THEY'RE DONE WITH THE FORK
        public void putForkDown ( int philosopherIndex, Fork left, Fork right){
            mutex.lock();

            //CHANGE THE PHILOSOPHER'S STATE TO THINKING AND SET THEIR LEFT AND RIGHT FORKS AS AVAILABLE
            setState(philosopherIndex, "thinking");
            left.setAvailable(true);
            right.setAvailable(true);
            conditions[(philosopherIndex+1) % 5].signal();
            conditions[(philosopherIndex+4) % 5].signal();

            //DISPLAY THE FORKS THE PHILOSOPHER PUT DOWN AND THE STATE THEY'RE IN
            int leftFork = (((philosopherIndex+1) + 4) % 5);
            int rightFork = philosopherIndex + 1;
            System.out.println("Philosopher " + (philosopherIndex+1) + " puts fork " + (leftFork) + " and " + rightFork + " down.");
            System.out.println("Philosopher " + (philosopherIndex+1) + " is " + currentState[philosopherIndex]);
            mutex.unlock();
        }

    }

    //
    static class Philosopher implements Runnable{
        State state;
        Fork left, right;
        int philosopherIndex;

        //INITIALIZES THE PHILOSOPHER'S STATE, LEFT FORK, RIGHT FORK, AND THEY'RE NAME (INDEX NUMBER)
        public Philosopher( int philosopherIndex, Fork left, Fork right, State i){
            this.state = i;
            this.left = left;
            this.right= right;
            this.philosopherIndex = philosopherIndex;
        }

        //WHEN THE PHILOSOPHER IS THINKING HAVE THEM SLEEP FOR A RANDOM PERIOD OF TIME
        private void thinking(){
            int sleepTime = (int)(Math.random()*2000);
            try{
                Thread.sleep(sleepTime);
                System.out.println("Philosopher " + (philosopherIndex +1) +" is thinking");
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }

        //WHEN THE PHILOSOPHER IS EATING HAVE THEM SLEEP FOR A RANDOM PERIOD OF TIME
        private void eating(){
            int sleepTime = (int)(Math.random()*2000);
            try{
                Thread.sleep(sleepTime);
                System.out.println("Philosopher " + (philosopherIndex +1) +" is eating");
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }

        //INITIALIZES THE PHILOSOPHER TO START THINKING AND EATING
        public void run(){
            while(true){
                thinking();
                state.grabFork(philosopherIndex,left,right);
                eating();
                state.putForkDown(philosopherIndex,left,right);
                thinking();
            }
        }

    }

    //MAIN METHOD WHERE THE PHILOSOPHER, THEIR STATE, AND FORKS ARE INITIALIZED
    public static void main (String[] args){
        Fork[] fork = new Fork[5];
        Philosopher[] philosophers = new Philosopher[5];
        State state = new State();

        //CREATING FORKS FOR EVERY PHILOSOPHER
        for (int i = 0; i < 5; i++){
            fork[i] = new Fork();
        }

        //CREATING A PHILOSOPHER AS WELL AS THEIR THREAD
        for ( int i = 0; i < 5; i++){
            philosophers[i] = new Philosopher(i, fork[i], fork[(i+4) % 5], state);
            Thread philnum = new Thread(philosophers[i]);
            philnum.start();
        }

    }
}