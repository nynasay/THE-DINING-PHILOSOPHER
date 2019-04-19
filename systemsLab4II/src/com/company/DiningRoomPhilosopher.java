package com.company;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
//http://www.java2s.com/Tutorials/Java/Java_Thread_How_to/Concurrent/Solve_dining_philosophers_monitors.htm

public class DiningRoomPhilosopher {
    static class Fork{
        private boolean availabile;
        public Fork (){
            availabile = true;
        }
        public boolean getAvailability(){
            return availabile;
        }
        public void setAvailabile(boolean x){
            availabile = x;
        }
    }

    static class State{
        Lock mutex = new ReentrantLock();
        //Conditions for the 5 philosophers to prevent starvation
        Condition [] conditions = new Condition[5];
        String [] currentState = new String[5];
        int [] philosopherNum = new int [5];

        public State (){
            for ( int i = 0 ; i < 5; i ++){
                philosopherNum[i] = i;
                currentState[i] = "thinking";
                conditions[i] = mutex.newCondition();
            }
        }
        public void setState (int philosphoer, String state){
            currentState[philosphoer] = state;
        }

        public void outputState (int philosopherIndex){

        }

        public void grabFork ( int philosopherIndex, Fork left, Fork right){
            mutex.lock();
            try{
                setState(philosopherIndex, "hungry");
                System.out.println("Philosopher " + (philosopherIndex +1) +" is hungry");
                //The philospher has to wait until the forks become available
                while (!left.getAvailability() || !right.getAvailability()){
                    conditions[philosopherIndex].await();
                }
                //Once both left and right has become availabile, they have to be
                //set to false since the philosopher is using them
                left.setAvailabile(false);
                right.setAvailabile(false);
                setState(philosopherIndex, "eating");
                int leftFork = (((philosopherIndex+1) + 4) % 5);
                System.out.println("Philosopher " + (philosopherIndex+1) + " takes fork " + (leftFork) + " and " + (philosopherIndex+1));
                System.out.println("Philosopher " + (philosopherIndex+1) + " is " + currentState[philosopherIndex]);

            }
            catch(Exception e){
                e.printStackTrace();
            }
            mutex.unlock();
        }
        public void putForkDown ( int philosopherIndex, Fork left, Fork right){
            mutex.lock();
            setState(philosopherIndex, "thinking");
            left.setAvailabile(true);
            right.setAvailabile(true);
            conditions[(philosopherIndex+1) % 5].signal();
            conditions[(philosopherIndex+4) % 5].signal();
            int leftFork = (((philosopherIndex+1) + 4) % 5);
            System.out.println("Philosopher " + (philosopherIndex+1) + " puts fork " + (leftFork) + " and " + (philosopherIndex+1) + " down.");
            System.out.println("Philosopher " + (philosopherIndex+1) + " is " + currentState[philosopherIndex]);
            mutex.unlock();
        }

    }

    static class Philosopher implements Runnable{
        State state;
        Fork left, right;
        int philosopherIndex;
        public Philosopher( int philosopherIndex, Fork left, Fork right, State i){
            this.state = i;
            this.left = left;
            this.right= right;
            this.philosopherIndex = philosopherIndex;
        }
        private void thinking(){
            int sleepTime = (int)(Math.random()*2000);
            try{
                Thread.sleep(sleepTime);
                System.out.println("Philosopher " + (philosopherIndex +1) +" is thinking");
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }
        private void eating(){
            int sleepTime = (int)(Math.random()*2000);
            try{
                Thread.sleep(sleepTime);
                System.out.println("Philosopher " + (philosopherIndex +1) +" is eating");
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }
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
    public static void main (String[] args){
        Fork[] fork = new Fork[5];
        Philosopher[] philosophers = new Philosopher[5];
        State state = new State();
        for (int i = 0; i < 5; i++){
            fork[i] = new Fork();
        }
        for ( int i = 0; i < 5; i++){
            philosophers[i] = new Philosopher(i, fork[i], fork[(i+4) % 5], state);
            Thread philnum = new Thread(philosophers[i]);
            philnum.start();
        }

    }
}