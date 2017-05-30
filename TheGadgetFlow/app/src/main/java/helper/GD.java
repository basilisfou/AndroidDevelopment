package Helper;

import Model.Categories;
import Model.CategoryItem;

/**
 * Created by Vasilis Fouroulis on 18/4/2016.
 * Helper Method that Saves things to memory
 */
public class GD {

    private static GD instance;
    private String fragmentName = " ";
    private CategoryItem FragmentListItem;
    private String isClickedFilterSorting;
    private String valueStringSorting;
    private String searchKey;
    public String items;
    public boolean printLogs = true;


    private Categories Categories;


    public static GD get(){
        if ( instance == null )
            instance = new GD();

        return instance;
    }

    public String getFragmentName() {
        return fragmentName;
    }

    public void setFragmentName(String fragmentName) {
        this.fragmentName = fragmentName;
    }

    public CategoryItem getFragmentListItem() {
        return FragmentListItem;
    }

    public void setFragmentListItem(CategoryItem fragmentListItem) {
        FragmentListItem = fragmentListItem;
    }

    public Categories getCategories() {
        return Categories;
    }

    public void setCategories(Model.Categories categories) {
        Categories = categories;
    }

    public String getIsClickedFilterSorting() {
        return isClickedFilterSorting;
    }

    public void setIsClickedFilterSorting(String isClickedFilterSorting) {
        this.isClickedFilterSorting = isClickedFilterSorting;
    }

    public String getValueStringSorting() {
        return valueStringSorting;
    }

    public void setValueStringSorting(String valueStringSorting) {
        this.valueStringSorting = valueStringSorting;
    }

    public String getSearchKey() {
        return searchKey;
    }

    public void setSearchKey(String searchKey) {
        this.searchKey = searchKey;
    }

}
