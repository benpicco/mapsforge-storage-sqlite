package org.mapsforge.storage.util;

public class Tuple<A, B> {
    public A a;
    public B b;
    public Tuple(A a, B b) {
        this.a = a;
        this.b = b;
    }
    
    @Override
    public String toString() {
    	return "(" + a + ", " + b + ")";
    }
}