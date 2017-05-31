package app;

/**
 * Created by Dawid on 2017-03-29.
 */

public class AppConfig {

    // Server user login url
    public static String Static_URL = "http://192.168.0.115";
    // Server user register url
    public static final String URL_REGISTER = Static_URL + "/android_login_api/register.php";
    // Server user register by facebook url
    public static final String URL_REGISTERBYFB = Static_URL + "/android_login_api/registerbyFB.php";
    // Url to check is user with this fb_id is stored in MySQL
    public static final String URL_CHECK = Static_URL + "/android_login_api/Check.php";
    // Url to check credentials
    public static final String URL_LOGIN = Static_URL + "/android_login_api/login.php";
    // Url to check credentials
    public static final String URL_LOGINFB = Static_URL + "/android_login_api/loginFB.php";

    public static final String URL_update_user_null= Static_URL + "/android_login_api/usersinfoemailnull.php";
    public static final String URL_update_user = Static_URL + "/android_login_api/update_user.php";
    public static final String URL_update_userfb = Static_URL + "/android_login_api/connectwithfacebook.php";
    public static final String url_user_details = Static_URL + "/android_login_api/usersinfo.php";
    public static final String url_book_details = Static_URL + "/android_connect/get_product_details.php";
    public static String url_all_books = Static_URL + "/android_connect/get_all_products.php";
    // url to update book
    public static final String url_update_book = Static_URL + "/android_connect/update_product.php";
    // url to delete book
    public static final String url_delete_product = Static_URL + "/android_connect/delete_product.php";
    public static String url_create_product = Static_URL + "/android_connect/create_product.php";

}
