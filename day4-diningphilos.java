class DiningPhilosophers {

    public DiningPhilosophers() {
        
    }

    // call the run() method of any runnable to execute its code
    public synchronized void wantsToEat(int philosopher,
                           Runnable pickLeftFork,
                           Runnable pickRightFork,
                           Runnable eat,
                           Runnable putLeftFork,
                           Runnable putRightFork) throws InterruptedException {
        
        pickRightFork.run();
        pickLeftFork.run();
        eat.run();
        putRightFork.run();
        putLeftFork.run();
    }
}
