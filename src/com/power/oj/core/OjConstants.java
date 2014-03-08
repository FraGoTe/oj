package com.power.oj.core;

/**
 * Some constant objects.
 * 
 * @author power
 * 
 */
public interface OjConstants
{
  /*
   * These contants are used in general.
   */
  long MINUTE_IN_MILLISECONDS = 60 * 1000;
  long SESSION_EXPIRES_TIME = 15 * 60 * 1000;
  long RESET_PASSWORD_EXPIRES_TIME = 30 * 60 * 1000;
  long VERIFY_EMAIL_EXPIRES_TIME = 60 * 60 * 1000;
  long DAY_TIMESTAMP = 24 * 60 * 60 * 1000;
  int MAX_PROBLEMS_IN_CONTEST = 26;
  int PENALTY_FOR_WRONG_SUBMIT = 20;
  int AVATAR_WIDTH = 100;
  int AVATAR_HEIGHT = 100;
  int USERNAME_MIN_LENGTH = 5;
  int USERNAME_MAX_LENGTH = 15;
  int PASSWORD_MIN_LENGTH = 6;
  int PASSWORD_MAX_LENGTH = 20;
  
  /*
   * These contants are used in view(attr name) and session.
   */
  String SITE_TITLE = "siteTitle";
  String PAGE_TITLE = "pageTitle";
  String BASE_URL = "baseUrl";
  
  String CONTROLLER_KEY = "controllerKey";
  String ACTION_KEY = "actionKey";
  String METHOD_NAME = "methodName";

  String MSG = "msg";
  String MSG_TYPE = "msgType";
  String MSG_TITLE = "msgTitle";

  String USER = "user";
  String USER_ID = "userID";
  String USER_NAME = "userName";
  String USER_EMAIL = "userEmail";
  String USER_LIST = "userList";
  String ADMIN_USER = "adminUser";

  String PROGRAM_LANGUAGES = "program_languages";
  String LANGUAGE_NAME = "language_name";
  String JUDGE_RESULT = "judge_result";
  String RESULT_TYPE = "result_type";
  
  String LAST_ACCESS_URL = "lastAccessURL";
  
  /*
   * These contants are used in cookie.
   */
  String RANDOM_CODE_KEY = "PowerOJCaptcha";
  
  int COOKIE_AGE = 3600 * 24 * 7;
  int TOKEN_AGE = 3600 * 24 * 7;
  String TOKEN_NAME = "PowerOjName";
  String TOKEN_TOKEN = "PowerOjToken";

}
