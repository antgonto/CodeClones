package type3;

public class CloneType3_8 {

    public void sumar() {
        int a = 1;
        int b = 2;
        int d = 3;
        int c = 4;
        int e = 5;
        int f = 5;
        if (a >= b) {
            c = d + b; // Comment1
            e = 1; // This statement is added
            f = 1 + 2;
            d = d + 1;
        } else {
            c = d - a; //Comment2 
        }
    }
}
