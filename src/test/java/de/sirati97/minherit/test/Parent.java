package de.sirati97.minherit.test;

public class Parent extends Base {


    public Parent(String s, int num) {
        super(s);
        System.out.println("Parent<ctor> s=" + s + " and num=" + num);
    }

    @Override
    public void foo() {
        System.out.println("foo of Parent");
        foo2();
        super.foo();
    }


    private void foo2() {
        System.out.println("foo2 of Parent");
    }
}
