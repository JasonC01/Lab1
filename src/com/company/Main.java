package com.company;
import java.util.Scanner;
import java.util.PriorityQueue;

public class Main{
    public static class Shop{
        private Counter[] Counters;
        public Shop(int numOfCounters){
            Counters = new Counter[numOfCounters];
            for(int i = 0; i < numOfCounters; i ++){
                Counters[i] = new Counter(true);
            }
        }
        public int getAvailableCounter(){
            int counterId = -1;
            for(int i = 0; i < Counters.length; i++){
                if(Counters[i].isAvailable()){
                    counterId = i;
                    break;
                }
            }
            return counterId;
        }
        public Counter getCounter(int i){
            return Counters[i];
        }
    }
    public static class Counter{
        public static int lastId;
        private int CounterId;
        private boolean Available;
        public Counter(boolean Available){
            this.CounterId = lastId++;
            this.Available = Available;
        }
        public boolean isAvailable(){
            return this.Available;
        }
        public void makeOccupied(){
            this.Available = false;
        }
        public void makeFree(){
            this.Available = true;
        }
        @Override
        public String toString(){
            return ": Counter " + CounterId;
        }
    }
    public static class Customer{
        private static int lastId;
        private int CustomerId;
        private double ArrivalTime;
        private double ServiceTime;
        public Customer(double ArrivalTime, double ServiceTime){
            this.CustomerId = lastId++;
            this.ArrivalTime = ArrivalTime;
            this.ServiceTime = ServiceTime;
        }
        public double getArrivaltime(){
            return this.ArrivalTime;
        }
        public double getServiceTime(){
            return this.ServiceTime;
        }
        @Override
        public String toString(){
            return ": Customer " + this.CustomerId;
        }
    }
    public static class ArrivalEvent extends Event{
        Customer customer;
        Shop shop;
        public ArrivalEvent(Customer c, Shop s){
            super(c.getArrivaltime());
            this.customer = c;
            this.shop = s;
        }
        @Override
        public String toString(){
            return super.toString() + customer.toString() + " arrives";
        }
        @Override
        public Event[] simulate() {
            if(shop.getAvailableCounter() == -1){
                return new Event[]
                        {new DepartureEvent(this.getTime(), customer)};
            }else{
                return new Event[]
                        {new ServiceBeginEvent(customer, shop.getCounter(shop.getAvailableCounter()))};
            }
        }
    }
    public static class ServiceBeginEvent extends Event{
        Customer customer;
        Counter counter;
        public ServiceBeginEvent(Customer c, Counter cc){
            super(c.getArrivaltime());
            this.customer = c;
            this.counter = cc;
        }
        @Override
        public String toString(){
            return super.toString() + customer.toString() + String.format(" service begin (by %s)", counter.toString());
        }
        @Override
        public Event[] simulate(){
            counter.makeOccupied();
            double endTime = customer.getArrivaltime() + customer.getServiceTime();
            return new Event[]
                    {new ServiceEndEvent(customer, counter, endTime)};
        }

    }
    public static class ServiceEndEvent extends Event{
        Customer customer;
        Counter counter;
        public ServiceEndEvent(Customer c, Counter cc, double endTime){
            super(endTime);
            this.customer = c;
            this.counter = cc;
        }
        @Override
        public String toString(){
            return super.toString() + customer.toString() + String.format(" service done (by %s)", counter.toString());
        }
        @Override
        public Event[] simulate(){
            counter.makeFree();
            return new Event[]
                    {new DepartureEvent(this.getTime(), customer)};
        }
    }
    public static class DepartureEvent extends Event{
        Customer customer;
        public DepartureEvent(double time, Customer c){
            super(time);
            customer = c;
        }
        @Override
        public String toString(){
            return super.toString() + customer.toString() + " departed";
        }
        @Override
        public Event[] simulate(){return new Event[]{};}
    }
    static class ShopSimulation extends Simulation {
        public Event[] initEvents;
        public ShopSimulation(Scanner sc) {
            initEvents = new Event[sc.nextInt()];
            int numOfCounters = sc.nextInt();
            Shop myshop = new Shop(numOfCounters);
            int id = 0;
            while (sc.hasNextDouble()) {
                double arrivalTime = sc.nextDouble();
                double serviceTime = sc.nextDouble();
                Customer customer = new Customer(arrivalTime, serviceTime);
                initEvents[id] = new ArrivalEvent(customer, myshop);
                id += 1;
            }
        }
        @Override
        public Event[] getInitialEvents() {
            return initEvents;
        }
    }
    static abstract class Event implements Comparable<Event> {
        private final double time;
        public Event(double time) {
            this.time = time;
        }
        public double getTime() {
            return this.time;
        }
        @Override
        public int compareTo(Event e) {
            if (this.time > e.time) {
                return 1;
            } else if (this.time == e.time) {
                return 0;
            } else {
                return -1;
            }
        }
        @Override
        public String toString() {
            return String.format("%.3f", this.time);
        }
        public abstract Event[] simulate();
    }
    static abstract class Simulation {
        public abstract Event[] getInitialEvents();
    }
    public static class Simulator {
        private final PriorityQueue<Event> events;
        public Simulator(Simulation simulation) {
            this.events = new PriorityQueue<Event>();
            for (Event e : simulation.getInitialEvents()) {
                this.events.add(e);
            }
        }
        public void run() {
            Event event = this.events.poll();
            while (event != null) {
                System.out.println(event);
                Event[] newEvents = event.simulate();
                for (Event e : newEvents) {
                    this.events.add(e);
                }
                event = this.events.poll();
            }
            return;
        }
    }
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        Simulation simulation = new ShopSimulation(sc);
        new Simulator(simulation).run();

        // Clean up the scanner.
        sc.close();
    }
}
