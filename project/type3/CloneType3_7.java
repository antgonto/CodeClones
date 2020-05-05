package type3;

public class CloneType3_7 {

    public void sumar() {
        int a = 1;
        int b = 2;
        int d = 3;
        int c = 4;
        if (a >= b) {
            c = d + b; // Comment1
            d = d + 1;
        } else {
            c = d - a; //Comment2 
        }
    }
}
