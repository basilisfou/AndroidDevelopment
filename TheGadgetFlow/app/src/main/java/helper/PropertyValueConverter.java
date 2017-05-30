package Helper;

import org.simpleframework.xml.convert.Converter;
import org.simpleframework.xml.stream.InputNode;
import org.simpleframework.xml.stream.OutputNode;
import Model.Model_Gadget.Cat;

/**Created by Vasilis Fouroulis on 15/9/2016.*/
public class PropertyValueConverter implements Converter<Cat> {
    public static final String TAG = "PropertyValueConverter";
    @Override
    public Cat read(InputNode node) throws Exception {
        Cat cat = new Cat();
//        Log.d(TAG,node.toString());
        String value = node.getValue();
        if (value != null) {
            cat.numberId = value;
        }

        InputNode nodeB = node.getNext();
        if (nodeB != null) {
            value = nodeB.getValue();
            if (value != null) {
                cat.numberId += value;
            }
        }

        return cat;
    }

    @Override
    public void write(OutputNode node, Cat value) throws Exception {

    }
}
