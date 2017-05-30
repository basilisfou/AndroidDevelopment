package Model.Model_Gadget;

import org.simpleframework.xml.Root;
import org.simpleframework.xml.Text;
import org.simpleframework.xml.convert.Convert;

import java.io.Serializable;

import Helper.PropertyValueConverter;

/**
 * Created by Vasilis Fouroulis on 8/9/2016. Save All Categories from the XML parsing of FEEDs
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
@Root(name = "category", strict = false)
@Convert(PropertyValueConverter.class)
public class Cat implements Serializable {
    @Text
    public String numberId;

}
