package Model.Model_Image_Gallery;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by vassilis on 3/11/2015.
 */

@Root(name="channel", strict = false)
public class Channel {

    @Element
    private imageGalleryItem item;

    public imageGalleryItem getItem(){
        return item;
    }

}
