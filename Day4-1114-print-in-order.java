class Foo {
    boolean one;
    boolean two;

    public Foo() {

        one = false;
        two = false;
        
    }

    public synchronized void first(Runnable printFirst) throws InterruptedException {
        
        printFirst.run();
        one = true;
        notifyAll();
    }

    public synchronized void second(Runnable printSecond) throws InterruptedException {
        
        while(!one){
            wait();
        }
        printSecond.run();
        two=true;
        notifyAll();
        
    }

    public synchronized void third(Runnable printThird) throws InterruptedException {
        while(!two){
            wait();
        }
        printThird.run();
        
        
        

    

    }
}
