package com.stop.test;

import static java.util.List;
import java.time.LocalDate;
import java.util.ArrayList;
import java.time.LocalDate;                 // Класс для работы с датами
import java.util.Collections;
import org.slf4j.Logger;                   // Логирование
import org.slf4j.LoggerFactory;            // Фабрика логгеров
import org.json.JSONObject;


import com.stop.test.Test2;
import com.viancis.communication.CheckResponse;

public abstract class Test extends Test2 implements Jump {

    @Override
    public volatile static int e = 5 + 2 + add(5,e);

    public final String f=e;


    @Override
    public static int ADD(int a, int b) {
        int sum = a + b;
        this.d = 10;
    }

    Test subtract(int a, int b) {
        if(true){
            int b = 0;
            int result = a - b;
            int umba_yumba = a - b;
            return new Test();
        }else{
            return null;
        }


        // Пропущен return
    }

    public String multiply(int a , int b) {
        return a * b;
    }

    public int divide(int a, int b) {
        if (b == 0) {
            System.out.println("Cannot divide by zero");

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