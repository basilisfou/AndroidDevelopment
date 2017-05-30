package Model.Model_Image_Gallery;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Namespace;
import org.simpleframework.xml.Root;

/**
 * Created by vfour_000 on 4/11/2015.
 */

@Root(name = "item", strict = false)
public class imageGalleryItem {

    @Element(name = "title")
    private String title;

    @Element(data = true)
    @Namespace(prefix="content")
    private String encoded;

    public String getTitle() {
        return title;
    }

    public String getEncoded() {
        return encoded;
    }

}
