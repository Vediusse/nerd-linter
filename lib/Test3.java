package com.stop.test;




import com.viancis.communication.CheckResponse;

public abstract class Test3 implements Run {

    public static Prosto check(String[] args) {
        Test test = new Test();
        test.add(5, 10);
        test.divide(5, 0);
    }

}