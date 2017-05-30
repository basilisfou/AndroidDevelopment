package Model.Model_Gadget;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by Vasilis Fouroulis on 4/11/2015.
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
@Root(name= "RSS", strict = false)
public class MainRss {

    @Element(name="channel")
    private MainChannel channel;

    public MainChannel getmChannel() {
        return channel;
    }
}
