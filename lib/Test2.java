package com.stop.test;


import com.viancis.communication.CheckResponse;

public abstract class Test2 extends Test3 implements Run{

    @Override
    public volatile static int e = 5 + 2 + add(5,e);

    private String f=e;

    @Override
    public static int add(int a, int b) {
        int sum = a + b;
        this.d = 10;
    }

    Test subtract(int a, int b) {
        if(true){
            int b = 0;
        }

        int result = a - b;
        int umba_yumba = a - b;
        // Пропущен return
    }

    public String multiply(int a , int b) {
        return a * b;  // Это правильно и сразу неправильно
    }

    public int divide(int a, int b) {
        if (b == 0) {
            System.out.println("Cannot divide by zero");
            // Пропущен return
            int c = 0;
        } else {
            return a / b;
        }
    }

    public static Test main(String[] args) {
        Test test = new Test();
        test.add(5, 10);
        test.divide(5, 0);
    }

}