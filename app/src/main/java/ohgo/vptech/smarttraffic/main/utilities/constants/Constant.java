package ohgo.vptech.smarttraffic.main.utilities.constants;

/**
 * Created by ypn on 7/25/2016.
 */
public class Constant {
    public static final String SERVER_REALTIME_URL ="http://139.59.247.138:3000/";
    public static final String SERVER_GET_API ="http://apitraffic.vptechgroup.com/";
    public static final String LANG_ENG  = "English";
    public static final String LANG_VI   = "Vietnamese";
    public static final String CODE_ENG  = "ENG";
    public static final String CODE_VI   = "VI";
    public static final String CODE_LANG ="LANG";
    public static final String CODE_RANGE = "RANGE_NOTICE";
    public static final String CODE_PREFERENCE_SETTING ="LANGS";
    public static final String CODE_PREFERENCE_PLACE ="setting_place";
    public static final String KEY_LIST_LANGS = "list_languages";

    public static final String CODE_FIRST_TIME ="FIRST";
    public static final String EXTRA_RESPONSE_RESULT_COMMENT_REPORT ="TAG_RESPONSE_RESUL_COMMENT_REPORT";
    public static final String EXTRA_CURRENT_COMMENT_REPORT ="TAG_CURRENT_COMMENT_REPORT";
    public static final String EXTRA_REPORT_COMPLETE ="TAG_REPORT_COMPLETE";
    public static final String EXTRA_LATLNG = "PREV_LATLNG";
    public static final String EXTRA_SOCKET_ID = "SOCKET_ID";
    public static final String EXTRA_FORGOT_EMAIL = "FORGOT_EMAIL";

    public static final String TAG_LISTEN_REPORT ="cordorate";


    public static final int DEFAULT_LENGTH_INPUT = 1;
    public static final int MAX_INPUT_LENGTH =255;
    public static final int ID_JOB_SERVICE = 1511;
    public static final int CODE_REQUEST_FINE_LOC = 1;
    public final static int CODE_ACTIVITY_REQUEST_LOCATION = 198;

    //State report
    public static final int POLICE_HIDE =3;
    public static final int POLICE_VISIBLE =1;
    public static final int POLICE_OTHESIDE =2;

    public static final int CAM_SPEED = 4;
    public static final int CAM_RED_LIGHT =5;
    public static final int CAM_FAKE =6;

    public static final int TRAFFIC_MODERATE =7;
    public static final int TRAFFIC_HEAVY =8;
    public static final int TRAFFIC_STANDTILL =9;

    public static final int ACCIDENT_MIRROR =10;
    public static final int ACCIDENT_MAJOR =11;
    public static final int ACCIDENT_OTHER_SIDE =12;
    public static final int MILIS = 1000;

    public static final int REPORT_TYPE_POLICE = 14;
    public static final int REPORT_TYPE_CAMERA = 17;
    public static final int REPORT_TYPE_TRAFFIC_JAM = 18;
    public static final int REPORT_TYPE_ACCIDENT =13;

    public static float FONT_SIZE_TITLE_BAR =20;



}
