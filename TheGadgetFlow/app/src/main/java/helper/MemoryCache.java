package Helper;

import java.util.ArrayList;

import Model.Model_Gadget.GadgetItem;

/**
 * Created by vasilis fouroulis  on 24/4/2016.
 */
public class MemoryCache {
    private static MemoryCache instance;
    public ArrayList<GadgetItem> wishList;
    public  ArrayList<GadgetItem> latest;
    public ArrayList<GadgetItem> trendings;


    public static MemoryCache get(){
        if ( instance == null )
            instance = new MemoryCache();

        return instance;
    }

}
