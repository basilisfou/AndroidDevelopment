package Helper;

import Fragments.FragmentListItemHome;

/**
 * Created by vfour_000 on 28/2/2016.
 */
public class Constants  {

    public static final String HOME_FRAGMENT = FragmentListItemHome.class.getSimpleName();
    public static final String LIST_FRAGMENT = "home";
    public static final String CATEGORIES_FRAGMENT = "categories";
    public static final String TRENDINGS_FRAGMENT = "trendings";
    public static final String WISHLIST_FRAGMENT = "wishlist";
    public static final String SETTINGS_FRAGMENT = "settings";
    public static final String OLDEST_PRODUCTS = "oldest";
    public static final String HIGHEST_PRICE = "highest_price";
    public static final String LOWEST_PRICE = "Lowest";
    public static final String RECONMENDED = "Reconmended";
    public static final String VALUE_SORTING = "value"; // bundle

    //--------------------------
    //Algolia
    //--------------------------
    public static final String ALGOLIA_APPLICATION_ID = "1UFC7YURSK";
    public static final String ALGOLIA_APY_KEY        = "6b294dc02634698047cdc3b1e66f2148";

    public static final String ERROR_MESSAGE = "An error Occurred. Please try again later or check your network connection.";

    //Intents
    public static final String LOG_IN_INTENT_TO_MAIN ="log_in_intenet";

    public static final String NONCE ="nonce";  //server nonce

    //--------------------------
    //Shared preferences profile
    //--------------------------
    public static final String FULL_NAME             = "fullName";
    public static final String USERS_EMAIL           = "usersEmail";
    public static final String WISH_LIST_SETTINGS    = "wishListSettings";//boolean
    public static final String COOKIE                = "cookie";
    public static final String USER_NAME             = "username";
    public static final String USERS_ID              = "id";
    public static final String NICE_NAME             = "nicename";
    public static final String FACEBOOK_URL          = "facebook_url";
    public static final String TWITTER_URL           = "twitter_url";

    //--------------------------
    //Local Broadcast Receiver
    //--------------------------
    public static final String LOCAL_BROADCAST_RECEIVER = "com.example.vasilis.TheGadgetFlow.onSaveStateChanged";

    //--------------------------------------------------------------------------------------------------
    //BASE URL
    //--------------------------------------------------------------------------------------------------
    public static final String BASE_URL = "http://thegadgetflow.com/";
    public static final String BASE_URL_JSON_API = "http://thegadgetflow.com/jsonapi/";
//    public static final String BASE_URL = "http://thegadgetflow.staging.wpengine.com/";
//    public static final String BASE_URL_JSON_API = "http://thegadgetflow.staging.wpengine.com/jsonapi/";

    //--------------------------------------------------------------------------------------------------
    //END - POINTS FEED
    //--------------------------------------------------------------------------------------------------
    public static final String DISCOUNTS_FEED     = "?feed=full_feed_discounts";
    public static final String FULL_FEED          = "?feed=full_feed";
    public static final String SEARCH_FEED        = "?feed=search_feed";
    public static final String WISH_LIST_FEED     = "user/{user}/?feed=author_feed";
    public static final String GET_SINGLE_PRODUCT = "?feed=single_product_feed";

    //--------------------------------------------------------------------------------------------------
    //END - POINTS JSON API
    //--------------------------------------------------------------------------------------------------
    public static final String RESET_PASSWORD               = BASE_URL_JSON_API + "user/retrieve_password/?user_login=@email@";
    public static final String GET_NONCE                    = BASE_URL_JSON_API + "get_nonce/?controller=user&method=register";
    public static final String LOG_IN                       = BASE_URL_JSON_API + "user/generate_auth_cookie/?nonce=@nonce@&username=@username_string@&password=@password_string@";
    public static final String FACEBOOK_GET_TOKEN           = BASE_URL_JSON_API + "user/fb_connect/?access_token=@token@";
    public static final String REGISTER_DEVICE              = BASE_URL_JSON_API + "sgwl/gf_set_android_token/?cookie=@cookie@&devicetoken=@devicetoken@";
    public static final String REGISTER_DEVICE_GUEST        = BASE_URL_JSON_API + "sgwl/gf_set_guest_android_token/?verification_code=fg6TG12XC0A&devicetoken=@devicetoken@";
    public static final String REGISTER                     = BASE_URL_JSON_API + "user/register/?username=@username_string@&email=@email_string@&nonce=@nonce@&display_name=@display_name@&user_pass=@password_string@";
    public static final String ADD_ITEM_TO_WISH_LIST        = BASE_URL_JSON_API + "sgwl/sgwl_add/?post_id=@itemid@&cookie=@usercok@";
    public static final String REMOVE_ITEM_FROM_WISH_LIST   = BASE_URL_JSON_API + "sgwl/sgwl_remove/?post_id=@itemid@&cookie=@usercok@";
    public static final String GET_WISH_LIST                = BASE_URL_JSON_API + "sgwl/sgwl_get_wishlist/?cookie=";
    public static final String SET_USER_PREFERENCE          = BASE_URL_JSON_API + "sgwl/gf_set_user_info/?cookie=@cookie@";
    public static final String GET_USER_PREFERENCE          = BASE_URL_JSON_API + "sgwl/gf_get_user_info/?cookie=@cookie@";
    public static final String GET_CATEGORIES               ="sgwl/get_category_index_full/";

}
