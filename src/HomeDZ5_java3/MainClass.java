package HomeDZ5_java3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;

public class MainClass {
    public static final int CARS_COUNT = 4;
    public static void main(String[] args) {


        System.out.println("ВАЖНОЕ ОБЪЯВЛЕНИЕ >>> Подготовка!!!");
        Race race = new Race(new Road(60), new Tunnel(), new Road(40));
        Car[] cars = new Car[CARS_COUNT];


        CyclicBarrier cb = new CyclicBarrier(cars.length); //TODO создаем объект для блокировки потоков

        for (int i = 0; i < cars.length; i++) {
            cars[i] = new Car(race, 20 + (int) (Math.random() * 10), cb); //TODO передали объект в метод CAR
        }

        for (int i = 0; i < cars.length; i++) {
            new Thread(cars[i]).start();

        }
    }
}


class Car implements Runnable {
    private static int CARS_COUNT;
    static {
        CARS_COUNT = 0;
    }
    private Race race;
    private int speed;
    private String name;
    public String getName() {
        return name;
    }
    public int getSpeed() {
        return speed;
    }
    public CyclicBarrier cyclicBarrier;
    public Car(Race race, int speed, CyclicBarrier cyclicBarrier) {  //TODO Приняли объект cb для блокировки
        this.race = race;
        this.speed = speed;
        CARS_COUNT++;
        this.name = "Участник #" + CARS_COUNT;
        this.cyclicBarrier = cyclicBarrier;

    }
    @Override
    public void run() {
        try {
            System.out.println(this.name + " готовится");
            Thread.sleep(500 + (int)(Math.random() * 800));
            System.out.println(this.name + " готов");
            cyclicBarrier.await(); //TODO блокировка потоков (запуск режима ожидания готовности машин)
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (cyclicBarrier.getParties() == 4) {
            System.out.println("ВАЖНОЕ ОБЪЯВЛЕНИЕ >>> Гонка началась! <<<");
        }

        for (int i = 0; i < race.getStages().size(); i++) {
            race.getStages().get(i).go(this);
        }
    }

}

abstract class Stage {
    protected int length;
    protected String description;
    public String getDescription() {
        return description;
    }
    public abstract void go(Car c);
}
class Road extends Stage {
    public Road(int length) {
        this.length = length;
        this.description = "Дорога " + length + " метров";
    }
    @Override
    public void go(Car c) {
        try {
            System.out.println(c.getName() + " начал этап: " + description);
            Thread.sleep(length / c.getSpeed() * 1000);
            System.out.println(c.getName() + " закончил этап: " + description);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
class Tunnel extends Stage {
    public Tunnel() {
        this.length = 80;
        this.description = "Тоннель " + length + " метров";
    }


    Semaphore smp = new Semaphore(2); //TODO ограничиваем потоки при работе с ресурсами (тоннелем, ставим 2 потока)


    @Override
    public void go(Car c) {
        try {

            smp.acquire(); //TODO захватываем тоннель 2 потоками

            try {
                System.out.println(c.getName() + " готовится к этапу(ждет): " + description);
                System.out.println(c.getName() + " начал этап: " + description);
                Thread.sleep(length / c.getSpeed() * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                System.out.println(c.getName() + " закончил этап: " + description);
                smp.release(); //TODO Освобождаем тоннель для остальных потоков (гонщиков)
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
    }
}
class Race {
    private ArrayList<Stage> stages;
    public ArrayList<Stage> getStages() { return stages; }
    public Race(Stage... stages) {
        this.stages = new ArrayList<>(Arrays.asList(stages));
    }
}
