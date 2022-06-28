package so;

import java.awt.Color;
import java.io.File;

public final class Constants {
    private Constants() { }

    public static final String FILENAME_OVERRIDES       = "overrides.txt";
    public static final String FILENAME_CONFIG          = "config.obj";
    public static final String FILENAME_TEAMDATA        = "teamdata.obj";
    public static final String FILENAME_MATCHDATA       = "matchdata.obj";
    public static final String FILENAME_LEAGUEDATA      = "leaguedata.obj";
    public static final String FILENAME_FORMULAS        = "formulas.obj";
    public static final String FILENAME_DEFAULT_LANG    = "#default#.txt";
    public static final String FILENAME_EMBEDDED_LANG   = "default.res";
    public static final String FILENAME_COUNTRY_DATA          = "countrydata.txt";
    public static final String FILENAME_EMBEDDED_COUNTRY_DATA = "countrydata.res";
    public static final String FILENAME_LANGUAGE_LIST   = "langlist.txt";
    public static final String FILENAME_PLAFS_DATA      = "plafsdata.txt";
    public static final String FILENAME_NTDB_URLS       = "ntdb_urls.txt";
    public static final String FILENAME_EMBEDDED_NTDB   = "ntdb_urls.res";
    public static final String FILENAME_HTML_PARSE_TABLE = "html_parse.txt";
    public static final String DIRNAME_DATA             = "data";
    public static final String DIRNAME_XML              = "xml";
    public static final String DIRNAME_HTML             = "html";
    public static final String DIRNAME_LANG             = "languages";
    public static final String DIRNAME_CONFIGFILES      = "resources";
    public static final String DIRNAME_IMAGES           = "images";
    public static final String DIRNAME_PLAFJARS         = DIRNAME_CONFIGFILES + File.separator + "plaf";
    public static final String DIRNAME_FLAGS            = DIRNAME_IMAGES + "/flags";
    public static final String DIRNAME_FACES            = DIRNAME_IMAGES + "/faces";
    public static final String FILENAME_IMG_UNKNOWNFLAG = DIRNAME_FLAGS + "/unknown.png";
    public static final String FILENAME_IMG_SPLASH      = DIRNAME_IMAGES + "/splash.png";
    public static final String FILENAME_IMG_SPLASH_XMAS = DIRNAME_IMAGES + "/splash_nav.jpg";
    public static final String FILENAME_IMG_SPLASH_NYEAR = DIRNAME_IMAGES + "/splash_anuevo.jpg";
    public static final String FILENAME_IMG_SOICON      = DIRNAME_IMAGES + "/soicon.png";
    public static final String FILENAME_IMG_INVALID     = DIRNAME_IMAGES + "/invalid.gif";
    public static final String FILENAME_IMG_BLANK       = DIRNAME_IMAGES + "/blank.gif";
    public static final String FILENAME_IMG_YELLOWCARD  = DIRNAME_IMAGES + "/yellowcard.gif";
    public static final String FILENAME_IMG_YELLOWCARD2 = DIRNAME_IMAGES + "/yellowcard2.gif";
    public static final String FILENAME_IMG_REDCARD     = DIRNAME_IMAGES + "/redcard.gif";
    public static final String FILENAME_IMG_INJURY      = DIRNAME_IMAGES + "/injury.gif";
    public static final String FILENAME_IMG_BALL        = DIRNAME_IMAGES + "/ball.png";
    public static final String FILENAME_IMG_SHIRT_GK    = DIRNAME_IMAGES + "/shirtGK.gif";
    public static final String FILENAME_IMG_SHIRT_DEF   = DIRNAME_IMAGES + "/shirtDEF.gif";
    public static final String FILENAME_IMG_SHIRT_MID   = DIRNAME_IMAGES + "/shirtMID.gif";
    public static final String FILENAME_IMG_SHIRT_ATT   = DIRNAME_IMAGES + "/shirtATT.gif";
    public static final String FILENAME_IMG_NT_PLAYER   = DIRNAME_IMAGES + "/NTstar.gif";
    public static final String FILENAME_IMG_MANAGER_NOTES    = DIRNAME_IMAGES + "/notes.gif";
    public static final String FILENAME_IMG_MANAGER_NO_NOTES = DIRNAME_IMAGES + "/notes_blank.gif";

    public static final String DEFAULT_LANG  = "<Default>";
    public static final String URL_HOSTNAME   = "https://online.sokker.org/";
    public static final String URL_DATA_PHP   = "dataxml.php";
    public static final String URL_XML_INIT   = "start.php?session=xml";
//     public static final String URL_INIT   = "index.php";
//     public static final String URL_LOGIN  = "logon.php"; // no
//     public static final String URL_LOGOFF = "index.php?action=start";
//     public static final String URL_START_PHP  = "start.php";
//     public static final String URL_LEFT_FRAME = "lewa.php";
//     public static final String URL_NT_PAGE       = "national.php";
//     public static final String URL_TEAM_PAGE     = "glowna.php";
//     public static final String URL_MATCHES_PAGE  = "matches.php";
//     public static final String URL_TRAINING_PAGE = "training.php";
//     public static final String URL_ECONOMY_PAGE  = "economy.php";
//     public static final String URL_STADIUM_PAGE  = "arena.php";
//     public static final String URL_LEAGUE_PAGE   = "league.php";

    //DataPair
    public static final int DATA_COMPARABLE_CURRENCY    = 11;
    public static final int DATA_COMPARABLE_NUMBER      = 12;
    public static final int DATA_COMPARABLE_SKILL       = 13;
    public static final int DATA_COMPARABLE_NAMED_SKILL = 14;
    public static final int DATA_NAME                   = 21;
    public static final int DATA_NAME2                  = 22;
    public static final int DATA_POSITION               = 1;
    public static final int DATA_POSITION_LONGNAME      = 2;
    public static final int DATA_STATUS                 = 3;
    public static final int DATA_RATING                 = 4;

    public static final int PLAYERCOLUMNS_COUNT = 37;
    public static final int JUNIORCOLUMNS_COUNT = 12;
    public static final int TRAINERCOLUMNS_COUNT = 14;
    public static final int MATCHESCOLUMNS_COUNT = 9;
    public static final int LINEUPCOLUMNS_COUNT = 30;
    public static final int PLAYERRATINGSCOLUMNS_COUNT = 11;
    public static final int PLAYERTRAININGCOLUMNS_COUNT = 17;
    public static final int TEAMID_NO_TEAM = -9;

    public static final int NO_DATA = -2;
    public static final int ROUNDS_IN_SEASON = 14;
    public static final int WEEKS_IN_GRAPH = 31;
    public static final int MAX_WEEKS_IN_GRAPH = 81;
    //JuniorProfile
    public static final int JR_NO_SKILLUP_YET = -1;
    public static final int JR_NO_DATA        = -1;
    public static final double JR_GUESSED_SKILL   = -2.0;
    public static final double JR_ESTIMATED_SKILL = -1.0;
    public static final double JR_BEST_TALENT = 3.25;

    //Options changes
    public static final int OPT_NO_CHANGE     = 0;
    public static final int OPT_LANGUAGE      = 1;
    public static final int OPT_CUR_CONV_RATE = 2;
    public static final int OPT_CUR_SYMBOL    = 4;
    public static final int OPT_LOW_SKILL     = 8;
    public static final int OPT_HIGH_SKILL    = 16;
    public static final int OPT_SEASON_REF    = 32;
    public static final int OPT_PLAF          = 64;
    //PositionManager changes
    public static final int OPT_FORMULAS = 1;

    public static final int NO_COUNTRY_SET = -7;

    // Basic Prices and costs (in zloty)
    public static final int COST_JR_PLACE        = 4000;
    public static final int COST_STADIUM_PLACE   = 3;
    public static final int COST_SUPP_CARD_PRICE = 500;
    public static final int MAX_DEBIT       = -1000000;
    public static final int BANKRUPCY_LIMIT = -2000000;

    // Training types
    public static final int TRAINING_STAMINA    = 1;
    public static final int TRAINING_KEEPER     = 2;
    public static final int TRAINING_PLAYMAKING = 3;
    public static final int TRAINING_PASSING    = 4;
    public static final int TRAINING_TECHNIQUE  = 5;
    public static final int TRAINING_DEFENDING  = 6;
    public static final int TRAINING_STRIKER    = 7;
    public static final int TRAINING_PACE       = 8;
    public static final int TRAINING_GK  = 0;
    public static final int TRAINING_DEF = 1;
    public static final int TRAINING_MID = 2;
    public static final int TRAINING_ATT = 3;

    // parsing
    public static final int PARSE_FAILED   = 0;
    public static final int PARSE_TEAM     = 1;
    public static final int PARSE_PLAYERS  = 2;
    public static final int PARSE_JUNIORS  = 4;
    public static final int PARSE_TRAINING = 8;
    public static final int PARSE_STADIUM  = 16;
    public static final int PARSE_ECONOMY  = 32;
    public static final int PARSE_LEAGUE   = 128;
    public static final int PARSE_MATCH    = 256;

    // matches
    public static final int PLAYERS_IN_MATCH_LINEUP = 16;
    public static final short MT_OFFICIAL  = 1;
    public static final short MT_FRIENDLY  = 4;
    public static final short MT_LEAGUE    = 8;
    public static final short MT_CUP       = 16;
    public static final short MT_QUALI     = 32;
    public static final short MT_NT        = 64;
    public static final short MT_LOCAL         = 256;
    public static final short MT_VISITOR       = 512;
    public static final short MT_NATIONAL      = 1024;
    public static final short MT_INTERNATIONAL = 2048;
    public static final short MT_VICTORY       = 4096;
    public static final short MT_DRAW          = 8192;
    public static final short MT_DEFEAT        = 16384;
    public static final short MATCH_LEAGUE            = MT_OFFICIAL | MT_LEAGUE;
    public static final short MATCH_CUP               = MT_OFFICIAL | MT_CUP;
    public static final short MATCH_QUALIFICATION     = MT_OFFICIAL | MT_CUP | MT_QUALI;
    public static final short MATCH_FRIENDLY_NORMAL   = MT_FRIENDLY;
    public static final short MATCH_FRIENDLY_CUPRULES = MT_FRIENDLY | MT_CUP;
    public static final short MATCH_FRIENDLY_LEAGUE   = MT_FRIENDLY | MT_LEAGUE;
    public static final short MATCH_NT_QUALIFICATION  = MT_NT | MT_OFFICIAL | MT_QUALI;
    public static final short MATCH_NT_WORLDCUP       = MT_NT | MT_OFFICIAL;
    public static final short MATCH_NT_FRIENDLY       = MT_NT | MT_FRIENDLY;

    // trainer jobs
    public static final int JOB_IDLE      = 0;
    public static final int JOB_HEAD      = 1;
    public static final int JOB_ASSISTANT = 2;
    public static final int JOB_JUNIORS   = 3;

    // ---------------------------------------------------------------
    // ---------------------------------------------------------------
    // ---------------------------------------------------------------
    //labels
    public static final class Labels {
        private Labels() { }

        public static final String TT                  = "tt_";
        public static final String TXT_DATA            = "data";
        public static final String TXT_CONFIG          = "config";
        public static final String TXT_DOWNLOAD_ALL    = "downloadall";
        public static final String TXT_DOWNLOAD_BASIC  = "downloadbasic";
        public static final String TXT_DOWNLOAD_L_M    = "downloadleaguem";
        public static final String TXT_DOWNLOAD_HISTM  = "downloadhistoric";
        public static final String TXT_IMPORT          = "import";
        public static final String TXT_IMPORT_XML      = "importxml";
        public static final String TXT_IMPORT_HTML     = "importhtml";
        public static final String TXT_IMPORT_EXTERNAL = "importext";
        public static final String TXT_SEND_TO_NTDB    = "sendtontdb";
        public static final String TXT_OPTIONS         = "options";
        public static final String TXT_EXIT            = "exit";
        public static final String TXT_FORMULAS        = "formulas";
        public static final String TXT_CREDITS         = "credits";
        public static final String TXT_INFO            = "info";
        public static final String TXT_REPORTS         = "reports";
        public static final String TXT_UTILS           = "utils";
        public static final String TXT_CCONVERT        = "curconvert";
        public static final String TXT_CHECKNEWVERSION = "checknewversion";
        public static final String TXT_AUTHOR          = "author";
        public static final String TXT_TRANSLATOR      = "translator";
        public static final String TXT_TRANSLATOR_NAME = "translator_name";
        public static final String TXT_SQUAD          = "squad";
        public static final String TXT_LINEUP         = "lineup";
        public static final String TXT_PLAYER_STATS   = "playerstats";
        public static final String TXT_DATA_MANAGER   = "datamanager";
        public static final String TXT_SETTINGS       = "settings";
        public static final String TXT_JUNIOR_SCHOOL  = "juniorschool";
        public static final String TXT_COACH_OFFICE   = "coachoffice";
        public static final String TXT_STADIUM        = "stadium";
        public static final String TXT_LEAGUETABLE    = "leaguetable";
        public static final String TXT_TRAINING       = "training";
        public static final String TXT_MATCHES        = "matches";
        public static final String TXT_FLAGCOLLECTION = "flagcollection";
        public static final String TXT_CH_NAME          = "ch_name";
        public static final String TXT_CH_AGE           = "ch_age";
        public static final String TXT_CH_BEST_POSITION = "ch_bestposition";
        public static final String TXT_CH_FORM          = "ch_form";
        public static final String TXT_CH_TACTDISCIP    = "ch_tactdiscip";
        public static final String TXT_CH_EXPERIENCE    = "ch_experience";
        public static final String TXT_CH_TEAMWORK      = "ch_teamwork";
        public static final String TXT_CH_STAMINA       = "ch_stamina";
        public static final String TXT_CH_PACE          = "ch_pace";
        public static final String TXT_CH_TECHNIQUE     = "ch_technique";
        public static final String TXT_CH_PASSING       = "ch_passing";
        public static final String TXT_CH_KEEPER        = "ch_keeper";
        public static final String TXT_CH_DEFENDER      = "ch_defender";
        public static final String TXT_CH_PLAYMAKER     = "ch_playmaker";
        public static final String TXT_CH_SCORER        = "ch_scorer";
        public static final String TXT_CH_MATCHES       = "ch_matches";
        public static final String TXT_CH_GOALS         = "ch_goals";
        public static final String TXT_CH_ASSISTS       = "ch_assists";
        public static final String TXT_CH_STATE         = "ch_state";
        public static final String TXT_CH_VALUE         = "ch_value";
        public static final String TXT_CH_SALARY        = "ch_salary";
        public static final String TXT_CH_ID            = "ch_id";
        public static final String TXT_CH_WEEKS         = "ch_weeks";
        public static final String TXT_CH_SKILL         = "ch_skill";
        public static final String TXT_CH_SKILLUP_COUNT = "ch_skillup_count";
        public static final String TXT_CH_AVG_WEEKS     = "ch_average_weeks_per_skillup";
        public static final String TXT_CH_PROJECTED_LEVEL = "ch_projected_level";
        public static final String TXT_CH_INITIAL_WEEKS = "ch_initial_weeks";
        public static final String TXT_CH_INITIAL_SKILL = "ch_initial_skill";
        public static final String TXT_CH_WEEKS_SINCE_POP = "ch_weeks_since_pop";
        public static final String TXT_CH_MONEY_SPENT     = "ch_money_spent";
        public static final String TXT_CH_TEAM             = "ch_team";
        public static final String TXT_CH_MATCHESPLAYED    = "ch_matchesplayed";
        public static final String TXT_CH_MATCHWINS        = "ch_matchwins";
        public static final String TXT_CH_MATCHDRAWS       = "ch_matchdraws";
        public static final String TXT_CH_MATCHLOSSES      = "ch_matchlosses";
        public static final String TXT_CH_GOALS_FAVOUR     = "ch_goalsfavour";
        public static final String TXT_CH_GOALS_AGAINST    = "ch_goalsagainst";
        public static final String TXT_CH_GOAL_DIFFERENCE  = "ch_goaldiff";
        public static final String TXT_CH_POINTS           = "ch_points";
        public static final String TXT_CH_DATE            = "ch_date";
        public static final String TXT_CH_RATING          = "ch_rating";
        public static final String TXT_CH_POSITION        = "ch_position";
        public static final String TXT_CH_ORDER           = "ch_order";
        public static final String TXT_CH_TIME            = "ch_time";
        public static final String TXT_CH_SHOTS           = "ch_shots";
        public static final String TXT_CH_FOULS           = "ch_fouls";
        public static final String TXT_CH_OFF             = "ch_offpercent";
        public static final String TXT_CH_DEF             = "ch_defpercent";
        public static final String TXT_CH_SQUAD           = "ch_squad";
        public static final String TXT_CH_PLAYEDLASTMATCH = "ch_playedinlastmastch";
        public static final String TXT_CH_SEND_TO_NTDB    = "ch_sendtontdb";
        public static final String TXT_CH_HEIGHT          = "ch_height";

        public static final String TXT_CONNECTING    = "connecting";
        public static final String TXT_LOGIN         = "login";
        public static final String TXT_USERNAME      = "username";
        public static final String TXT_PASSWORD      = "password";
        public static final String TXT_USE_PROXY      = "useproxy";
        public static final String TXT_PROXY_HOST     = "proxyhost";
        public static final String TXT_PROXY_PORT     = "proxyport";
        public static final String TXT_USE_PROXY_AUTH = "useproxyauth";
        public static final String TXT_PROXY_USERNAME = "proxyusername";
        public static final String TXT_PROXY_PASSWORD = "proxypassword";
        public static final String TXT_DOWNLOADING   = "downloadingxml";
        public static final String TXT_BEGIN_SESSION = "beginsession";
        public static final String TXT_LOGOFF        = "logoff";
        public static final String TXT_DL_LEAGUE     = "dl_league";
        public static final String TXT_DL_TRAINING   = "dl_training";
        public static final String TXT_DL_ECONOMY    = "dl_economy";
        public static final String TXT_DL_MATCHES    = "dl_matches";
        public static final String TXT_DL_MATCHID    = "dl_matchid";
        public static final String TXT_DL_TEAM       = "dl_team";
        public static final String TXT_DL_PLAYERS    = "dl_players";
        public static final String TXT_DL_JUNIORS    = "dl_juniors";
        public static final String TXT_ERROR       = "error";
        public static final String TXT_ERROR_UNKNOWN_HOST = "err_unknownhost";
        public static final String TXT_ERROR_INVALID_DATE = "err_invaliddate";
        public static final String TXT_ERROR_PARSE        = "err_parse";
        public static final String TXT_ERROR_IO           = "err_ioerror";
        public static final String TXT_ERROR_LOGIN_FAILED = "err_loginfailed";
        public static final String TXT_ERROR_NO_COUNTRY_SET = "err_nocountryset";
        public static final String TXT_ERROR_NO_RATE        = "err_nocurrencyrate";

        public static final String TXT_INPUT_DATE = "inputdate";
        public static final String TXT_TT_TABLE_HEADER = "tt_tableheader";
        public static final String TXT_AVG_AGE        = "averageage";
        public static final String TXT_AVG_FORM       = "averageform";
        public static final String TXT_TOTAL_PLAYERS  = "totalplayers";
        public static final String TXT_NAT_PLAYERS    = "natplayers";
        public static final String TXT_FGN_PLAYERS    = "fgnplayers";
        public static final String TXT_TOTAL_SALARY   = "totalsalary";
        public static final String TXT_AVG_SALARY     = "averagesalary";
        public static final String TXT_TOTAL_VALUE    = "totalvalue";
        public static final String TXT_AVG_VALUE      = "averagevalue";
        public static final String TXT_RANK           = "rank";
        public static final String TXT_MONEY          = "money";
        public static final String TXT_FANS           = "fans";
        public static final String TXT_FANCLUBMOOD    = "fanclubmood";
        public static final String TXT_LANGUAGE           = "language";
        public static final String TXT_COUNTRY            = "country";
        public static final String TXT_CURRENCY_CONV_RATE = "currencyrate";
        public static final String TXT_CURRENCY_SYMBOL    = "currencysymbol";
        public static final String TXT_LOW_SKILL       = "lowskill";
        public static final String TXT_HIGH_SKILL      = "highskill";
        //public static final String TT+TXT_LOW_SKILL  = "tt_lowskill";
        //public static final String TT+TXT_HIGH_SKILL = "tt_highskill";
        public static final String TXT_SEASON       = "season";
        public static final String TXT_COMPARE_DATA = "comparedata";
        public static final String TXT_SEASON_REFERENCE = "seasonreference";
        public static final String TXT_SEASON_REFERENCE_START = "seasonreferencestart";
        //public static final String TT+TXT_SEASON_REFERENCE = "tt_seasonreference";
        //public static final String TT+TXT_SEASON_REFERENCE_START = "tt_seasonreferencestart";
        public static final String TXT_AUTOSEND_TO_NTDB      = "autosendtontdb";
        public static final String TXT_AUTOCHECK_NEW_VERSION = "autochecknewversion";

        public static final String TXT_INPUT_MATCH_IDS = "inputmatchids";
        public static final String TXT_TT_TYPE_MATCHIDS_HERE = "typematchidshere";
        public static final String TXT_TT_DRAG_MATCHES_HERE  = "dragmatcheshere";

        public static final String TXT_FORM_MODIFIER  = "formmodifier";
        public static final String TXT_POSITION       = "position";
        public static final String TXT_PLAYER         = "player";
        public static final String TXT_RATING_FOR_POS = "ratingforpos";
        public static final String TXT_WITH_FORM      = "withform";
        public static final String TXT_WITHOUT_FORM   = "withoutform";
        public static final String TXT_SET        = "set";
        public static final String TXT_UNDO       = "undo";
        public static final String TXT_RESET      = "reset";
        //public static final String TT+TXT_SET   = "tt_set";
        //public static final String TT+TXT_UNDO  = "tt_undo";
        //public static final String TT+TXT_RESET = "tt_reset";

        public static final String TXT_FORM      = "skn_form";
        public static final String TXT_STAMINA   = "skn_stamina";
        public static final String TXT_PACE      = "skn_pace";
        public static final String TXT_TECHNIQUE = "skn_technique";
        public static final String TXT_PASSING   = "skn_passing";
        public static final String TXT_KEEPER    = "skn_keeper";
        public static final String TXT_DEFENDER  = "skn_defender";
        public static final String TXT_PLAYMAKER = "skn_playmaker";
        public static final String TXT_SCORER    = "skn_scorer";
        public static final String TXT_EXPERIENCE = "skn_experience";
        public static final String TXT_TEAMWORK   = "skn_teamwork";
        public static final String TXT_TACTICAL_DISCIPLINE = "skn_tactdiscip";

        public static final String TXT_TT_ROUND = "tt_round";
        public static final String TXT_TT_PLACE = "tt_place";
        public static final String TXT_GRAPH    = "graph";
        public static final String TXT_FIXTURE  = "fixture";

        public static final String TXT_TRAINING_REPORT = "trainingreport";
        public static final String TXT_JUNIOR_REPORT   = "juniorreport";
        public static final String TXT_SCHOOL_REPORT   = "schoolreport";
        public static final String TXT_FLAGS_REPORT    = "flagsreport";
        public static final String TXT_PLAYER_REPORT   = "playerreport";
        public static final String TXT_GENERATE        = "generate";

        public static final String TXT_UNRECOGNIZED_SKILL = "unrecogskill";
        public static final String TXT_WEEKS_IN_GRAPH = "weeksingraph";
        public static final String TXT_MANAGER_NOTES  = "managernotes";
        public static final String TXT_DELETE_FOREVER = "deleteforever";
        public static final String TXT_CONFIRM_DELETE = "confirmdelete";

        public static final String TXT_FP_FLAG        = "fp_flag";
        public static final String TXT_FP_PLAYER      = "fp_player";
        public static final String TXT_FP_VISITED     = "fp_visited";
        public static final String TXT_FP_HOSTED      = "fp_hosted";
        public static final String TXT_FP_TRIPTO      = "fp_tripto";
        public static final String TXT_FP_VISITFROM   = "fp_visitsfrom";
        public static final String TXT_FP_CUR_P_FLAGS = "fp_currentpflags";
        public static final String TXT_FP_FOR_P_FLAGS = "fp_formerpflags";
        public static final String TXT_FP_CUR_P_FROM  = "fp_currentpfrom";
        public static final String TXT_FP_FOR_P_FROM  = "fp_formerpfrom";
        public static final String TXT_FP_NEW_FLAGS   = "fp_newflags";
        public static final String TXT_FP_NEW_VISITED = "fp_newvisited";
        public static final String TXT_FP_NEW_HOSTED  = "fp_newhosted";
        public static final String TXT_FP_NOT_VISITED = "fp_notvisitedyet";
        public static final String TXT_FP_NOT_HOSTED  = "fp_nothostedyet";
        public static final String TXT_FP_ADD_FLAGS   = "fp_addflags";
        public static final String TXT_FP_REMOVE_FLAGS = "fp_removeflags";
        public static final String TXT_FP_UPDATE_FLAGS = "fp_updateflags";

        public static final String TXT_CUT       = "cut";
        public static final String TXT_COPY      = "copy";
        public static final String TXT_PASTE     = "paste";
        public static final String TXT_BOLD      = "bold";
        public static final String TXT_ITALIC    = "italic";
        public static final String TXT_UNDERLINE = "underline";
        public static final String TXT_SAVE_SNAPSHOT = "savesnapshot";
        public static final String TXT_FILTER_ALL_SKILLS = "filter_allskills";
        public static final String TXT_FILTER_OVERALL    = "filter_overall";

        public static final String TXT_NEW_VERSION    = "newversion";
        public static final String TXT_LANG_UPDATES   = "langupdates";
        public static final String TXT_UPDATE         = "update";
        public static final String TXT_DL_UPDATING_SO = "dl_updatingso";
        public static final String TXT_RESTART        = "restart";
        public static final String TXT_RESTART_MSG    = "restartmsg";

        public static final String TXT_JOB = "trainerjob";
        public static final String TXT_TRAINERJOB_ = "job";
        public static final String TXT_CH_TRAINER_SKILL = "ch_trainerskill";
        public static final String TXT_CHANGE_TRAINING  = "changetraining";
        public static final String TXT_DATA_STATUS       = "datastatus";
        public static final String TXT_DATA_SET_MANUALLY = "datasetmanually";
        public static final String TXT_WEEK = "week";
        public static final String TXT_ORDER = "order";
        public static final String TXT_TRAINING_LEVEL  = "trainlevel";
        public static final String TXT_RESIDUAL_LEVEL  = "residuallevel";
        public static final String TXT_ASSITANTS_TOTAL = "assistantstotal";
        public static final String TXT_MINUTES_TRAINED = "minutestrained";
        public static final String TXT_ORDER_PLAYED    = "orderplayed";

        public static final String TXT_COMPETITION = "competition";
        public static final String TXT_WEATHER    = "weather";
        public static final String TXT_OPPONENT   = "opponent";
        public static final String TXT_SPECTATORS = "spectators";
        public static final String TXT_SCORE      = "score";
        public static final String TXT_VICTORY    = "victory";
        public static final String TXT_DRAW       = "draw";
        public static final String TXT_DEFEAT     = "defeat";
        public static final String TXT_NATIONAL      = "national";
        public static final String TXT_INTERNATIONAL = "internat";
        public static final String TXT_LOCAL      = "local";
        public static final String TXT_VISITOR    = "visitor";
        public static final String TXT_OFFICIAL   = "official";
        public static final String TXT_FRIENDLY   = "friendly";
        public static final String TXT_LEAGUE     = "league";
        public static final String TXT_CUP        = "cup";
        public static final String TXT_QUALI      = "quali";
        public static final String TXT_MATCHES_TOTAL    = "totalmatches";
        public static final String TXT_MATCHES_WON      = "matcheswon";
        public static final String TXT_MATCHES_DRAWN    = "matchesdrawn";
        public static final String TXT_MATCHES_LOST     = "matcheslost";
        public static final String TXT_GOALS_FAVOUR     = "goalsfavour";
        public static final String TXT_GOALS_AGAINST    = "goalsagainst";
        public static final String TXT_GOAL_DIFFERENCE  = "goaldiff";
        public static final String TXT_SHOTS            = "shots";
        public static final String TXT_FOULS            = "fouls";
        public static final String TXT_YELLOW_CARDS     = "yellowcards";
        public static final String TXT_RED_CARDS        = "redcards";
        public static final String TXT_SHOOT_EFFICIENCY = "shootefficiency";
        public static final String TXT_AVG_ATTENDANCE   = "avgattendance";
        public static final String TXT_AVG_POSSESSION   = "avgpossession";
        public static final String TXT_AVG_PLAYINHALF   = "avgplayinhalf";
        public static final String TXT_AVG_SCORING      = "avgscoring";
        public static final String TXT_AVG_PASSING      = "avgpassing";
        public static final String TXT_AVG_DEFENDING    = "avgdefending";
        public static final String TXT_DATE_BEGIN = "datebegin";
        public static final String TXT_DATE_END   = "dateend";
//         public static final String TXT_ = "";
//         public static final String TXT_ = "";
//         public static final String TXT_ = "";
//         public static final String TXT_ = "";
//         public static final String TXT_ = "";
//         public static final String TXT_ = "";

    }
    // ---------------------------------------------------------------
    public static final class Colors {
        private Colors() {}

        public static final Color COLOR_LIGHT_YELLOW = new Color(255, 255, 192);
        public static final Color COLOR_LIGHT_PINK   = new Color(255, 192, 192);
        public static final Color COLOR_LIGHT_GREEN  = new Color(192, 255, 192);
        public static final Color COLOR_LIGHT_ORANGE = new Color(255, 224, 192);
        public static final Color COLOR_LIGHT_BLUE   = new Color(192, 224, 255);

        public static final Color CELLCOLOR_NAME         = COLOR_LIGHT_GREEN;
        public static final Color CELLCOLOR_FORM         = COLOR_LIGHT_PINK;
        public static final Color CELLCOLOR_PRIMARIES    = COLOR_LIGHT_ORANGE;
        public static final Color CELLCOLOR_SECONDARIES  = COLOR_LIGHT_YELLOW;
        public static final Color FONTCOLOR_NEWJUNIOR    = new Color(0, 128, 0);  // green

        public static final Color CELLCOLOR_GK  = Color.BLUE;
        public static final Color CELLCOLOR_DEF = Color.GREEN.darker();
        public static final Color CELLCOLOR_MID = Color.ORANGE.darker();
        public static final Color CELLCOLOR_ATT = Color.RED;
        public static final Color CELLCOLOR_PREF_POS = new Color(192, 255, 255);
    }
    // ---------------------------------------------------------------
    public static final class Positions {
        private Positions() { }

        public static final int POSITIONS_COUNT = 10;

        public static final int NO_POSITION = -7;
        public static final int DEF = 20;
        public static final int MID = 30;
        public static final int ATT = 40;

        public static final int GK  = 10;
        public static final int WB  = 21;
        public static final int CB  = 22;
        public static final int SW  = 23;
        public static final int DM  = 31;
        public static final int CM  = 32;
        public static final int AM  = 33;
        public static final int WM  = 35;
        public static final int FW  = 41;
        public static final int ST  = 42;
        public static final int [] RATEABLE_POSITIONS   = { GK, WB, CB, SW, DM, CM, AM, WM, FW, ST };

        // Modifiers
        public static final int P_LEFT    = 128;
        public static final int P_RIGHT   = 256;
        public static final int RESERVE = 512;
        // Subpositions
        public static final int LWB = P_LEFT  | WB;
        public static final int RWB = P_RIGHT | WB;
        public static final int LCB = P_LEFT  | CB;
        public static final int RCB = P_RIGHT | CB;
        public static final int LDM = P_LEFT  | DM;
        public static final int RDM = P_RIGHT | DM;
        public static final int LCM = P_LEFT  | CM;
        public static final int RCM = P_RIGHT | CM;
        public static final int LAM = P_LEFT  | AM;
        public static final int RAM = P_RIGHT | AM;
        public static final int LWM = P_LEFT  | WM;
        public static final int RWM = P_RIGHT | WM;
        public static final int LFW = P_LEFT  | FW;
        public static final int RFW = P_RIGHT | FW;
        public static final int [] SELECTABLE_POSITIONS = { GK, LWB, WB, RWB, LCB, CB, RCB, SW,
                                                            LDM, DM, RDM, LCM, CM, RCM, LAM, AM, RAM, LWM, WM, RWM,
                                                            LFW, FW, RFW, ST };

    }

}
