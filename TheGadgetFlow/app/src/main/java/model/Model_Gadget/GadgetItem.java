package Model.Model_Gadget;



import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Namespace;
import org.simpleframework.xml.Root;

import java.io.Serializable;
import java.util.ArrayList;

@Root(name ="item",strict = false)
public class GadgetItem implements Serializable {

	private static final long serialVersionUID = 1L; //assigned a version identifier
	@Element(name = "title")
	public String gadget_title;
	@Element(name = "link")
	public String gadget_link;
	@Element(name = "pubDate")
	public String gadget_pubDate;
	@Element(name = "guid")
	public String guid;
	@ElementList ( entry = "category",name = "category",required = false, inline = true )
	public ArrayList<Cat> catList;
	@Element(name = "description",data = true, required = false)
	public String gadget_description;
	@Element(data = true)
	@Namespace(prefix="wfw")
	public String commentRss;
	/*@Element(name ="comments")
	@Namespace(prefix="slash")
	public String comments;*/
	@Element(data = true)
	@Namespace(prefix="content")
	public String encoded;
	//created inside the call
	public String gadget_image;
	//created inside the gadget
	public String gadget_buyLink;
	//created inside the gadget
	public String gadget_price;
	public boolean isSearchItem;
	public boolean isSaved;
	public String id;
}
