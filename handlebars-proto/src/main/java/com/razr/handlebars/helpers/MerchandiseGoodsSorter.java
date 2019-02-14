package com.razr.handlebars.helpers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class MerchandiseGoodsSorter {

    public static Object sortGoodsList(Object goodsList) {

        //If the list doesn't look like a Merchandise UserGoods list, then just return unchanged
        //Also, only worth sorting if there's more than 1 item in the list
        if (goodsList instanceof List && ((List) goodsList).size() > 1) {

            Map<MerchandiseSortCriteria, Object> sortedUserGoods = new TreeMap<>();
            List<Object> sortedList = new ArrayList<>();
            for (Object userGoods : (List) goodsList) {
                if (userGoods instanceof Map && ((Map) userGoods).containsKey("OrderDetails")) {
                    MerchandiseSortCriteria sortCriteria = new MerchandiseSortCriteria((Map) userGoods);
                    sortedUserGoods.put(sortCriteria, userGoods);
                } else {
                    //Can't recognize for sorting, so will just add to the list
                    sortedList.add(userGoods);
                }
            }

            //Add each of the sorted goods to the list in the right order
            for (Object userGoods : sortedUserGoods.values()) {
                sortedList.add(userGoods);
            }
            return sortedList;
        }
        return goodsList;
    }

}
