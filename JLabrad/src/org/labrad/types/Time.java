package org.labrad.types;

public final class Time extends Type {
    private static Time instance = new Time();

    // private constructor to prevent instantiation
    private Time() {}
    
    public static Time getInstance() { return instance; }

    public Type.Code getCode() { return Type.Code.TIME; }
    public char getChar() { return 't'; }

    public boolean isFixedWidth() { return true; }

    public int dataWidth() { return 16; }

    public String toString() { return "t"; }

    public String pretty() { return "time"; }
}
