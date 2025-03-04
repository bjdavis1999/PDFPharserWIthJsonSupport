public class PriceData {

    double o;
    double h;
    double l;
    double c;


    /*
    * used to get price data from stock sources is the same format as the json they use ends up as a object inside a transaction in congress
    * */
    public PriceData( double o, double h,
                     double l, double c){


        // the price at several key times throughout the day
        // open
        this.o = o;
        // high
        this.h = h;
        // low
        this.l = l;
        // close
        this.c = c;


    }

}