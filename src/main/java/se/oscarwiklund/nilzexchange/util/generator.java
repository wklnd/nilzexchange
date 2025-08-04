package se.oscarwiklund.nilzexchange.util;

import java.util.Random;

public class generator {

    public static Long generateId(){
        Random random = new Random();
        Long generatedId;

        int length = 20;
        generatedId = (long)(random.nextInt(length)); // change me
        return generatedId;


    }

    public static Long generateOrderId(){
        // Order IDs should be unique, and not sequential.
        // The orderID is an hash of the order details, like symbol, side, buyerid, sellerid.

        Long orderId = 0L;

        return orderId;

    }
}
