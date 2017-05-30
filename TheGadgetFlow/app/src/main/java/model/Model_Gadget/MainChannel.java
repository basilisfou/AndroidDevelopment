package Model.Model_Gadget;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.ArrayList;


/**
 * Created by Vasilis Fouroulis on 3/11/2015.
 */
/**
******************************
*  8         8    888888888  *
*   8       8     8          *
*    8     8      888888888  *
*     8   8       8          *
*      8 8        8          *
*       8     .   8         .*
******************************
 */
@Root(name="channel", strict = false)
public class MainChannel {

    @ElementList(name="item",inline = true)
    private ArrayList<GadgetItem> items;

    public ArrayList<GadgetItem> getItems(){
        return items;
    }

}
