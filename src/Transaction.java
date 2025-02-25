public class Transaction {
    /*
    * used to store a stock transaction
    *
    * */
    // stores the name of the stock
    String Ticker;
    // stores the date the stock was purchused or sold
    String Date;
    // stores if it was a sale or purchase
    char SaleType;
    // stores the min value of the sale range
    int PurchaseMin;
    // stores the max value of the sale range
    int PurchaseMax;

    // stores the price data of the day in question
    // open close high low
    PriceData priceData;

    Transaction(String Ticker,String Date,int PurchaseMin,int PurchaseMax, PriceData priceData){
        this.Ticker = Ticker;
        this.Date = Date;
        this.PurchaseMin = PurchaseMin;
        this.PurchaseMax = PurchaseMax;
        this.priceData = priceData;
    }
    Transaction(){


    }

}
