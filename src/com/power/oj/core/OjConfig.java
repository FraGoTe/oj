package com.power.oj.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import jodd.util.collection.IntHashMap;

import com.power.oj.core.bean.ResultType;
import com.power.oj.core.model.LanguageModel;
import com.power.oj.core.model.VariableModel;
import com.power.oj.util.FileKit;

/**
 * Configure the system.
 * 
 * @author power
 * 
 */
public class OjConfig
{
  public static String baseUrl = null;
  public static String siteTitle = null;
  public static String userAvatarPath = null;
  public static String problemImagePath = null;
  public static String uploadPath = null;
  public static String downloadPath = null;

  public static int contestPageSize = 20;
  public static int contestRankPageSize = 50;
  public static int problemPageSize = 50;
  public static int userPageSize = 20;
  public static int statusPageSize = 20;

  public static long timeStamp;
  public static long startGlobalInterceptorTime;
  public static long startGlobalHandlerTime;

  public static List<LanguageModel> program_languages;
  public static IntHashMap language_type = new IntHashMap();
  public static IntHashMap language_name = new IntHashMap();
  public static IntHashMap result_type = new IntHashMap();
  public static List<ResultType> judge_result;

  public static HashMap<String, VariableModel> variable = new HashMap<String, VariableModel>();

  public static void loadVariable()
  {
    variable = new HashMap<String, VariableModel>();
    for (VariableModel variableModel : VariableModel.dao.find("SELECT * FROM variable"))
    {
      variable.put(variableModel.getStr("name"), variableModel);
    }
    
    siteTitle = get("siteTitle", "Power OJ");

    String uploadPath = get("uploadPath", "upload/");
    uploadPath = FileKit.parsePath(uploadPath);

    String downloadPath = get("downloadPath", "download/");
    downloadPath = FileKit.parsePath(downloadPath);
    
    String userAvatarPath = get("userAvatarPath", "assets/images/user/");
    userAvatarPath = FileKit.parsePath(userAvatarPath);
    
    String problemImagePath = get("problemImagePath", "assets/images/problem/");
    problemImagePath = FileKit.parsePath(problemImagePath);
    
    contestPageSize = getInt("contestPageSize", 20);
    contestRankPageSize = getInt("contestRankPageSize", 50);
    problemPageSize = getInt("problemPageSize", 50);
    userPageSize = getInt("userPageSize", 20);
    statusPageSize = getInt("statusPageSize", 20);
  }
  
  public static void loadLanguage()
  {
    language_type = new IntHashMap();
    language_name = new IntHashMap();
    program_languages = LanguageModel.dao.find("SELECT * FROM program_language WHERE status=1");
    for (LanguageModel Language : program_languages)
    {
      language_type.put(Language.getInt("id"), Language);
      language_name.put(Language.getInt("id"), Language.getStr("name"));
    }
  }
  
  public static void initJudgeResult()
  {
    judge_result = new ArrayList<ResultType>();
    judge_result.add(new ResultType(ResultType.AC, "AC", "Accepted"));
    judge_result.add(new ResultType(ResultType.PE, "PE", "Presentation Error"));
    judge_result.add(new ResultType(ResultType.TLE, "TLE", "Time Limit Exceed"));
    judge_result.add(new ResultType(ResultType.MLE, "MLE", "Memory Limit Exceed"));
    judge_result.add(new ResultType(ResultType.WA, "WA", "Wrong Answer"));
    judge_result.add(new ResultType(ResultType.RE, "RE", "Runtime Error"));
    judge_result.add(new ResultType(ResultType.OLE, "OLE", "Output Limit Exceed"));
    judge_result.add(new ResultType(ResultType.CE, "CE", "Compile Error"));
    judge_result.add(new ResultType(ResultType.SE, "SE", "System Error"));
    judge_result.add(new ResultType(ResultType.VE, "VE", "Validate Error"));
    judge_result.add(new ResultType(ResultType.Wait, "Wait", "Waiting"));

    result_type = new IntHashMap();
    for (Iterator<ResultType> it = judge_result.iterator(); it.hasNext();)
    {
      ResultType resultType = it.next();
      result_type.put(resultType.getId(), resultType);
    }
  }

  /*
   * get OJ variable from DB cache
   */
  public static String get(String name)
  {
    return get(name, null);
  }
  
  public static String get(String name, String defaultValue)
  {
    VariableModel model = variable.get(name);
    if (model != null)
    {
      return model.getStr("value");
    }
    
    return defaultValue;
  }
  
  public static Integer getInt(String name)
  {
    return getInt(name, null);
  }

  public static Integer getInt(String name, Integer defaultValue)
  {
    VariableModel model = variable.get(name);
    if (model != null)
    {
      return model.getInt("int_value");
    }
    
    return defaultValue;
  }

  public static Boolean getBoolean(String name)
  {
    return getBoolean(name ,null);
  }

  public static Boolean getBoolean(String name, Boolean defaultValue)
  {
    VariableModel model = variable.get(name);
    if (model != null)
    {
      return model.getBoolean("boolean_value");
    }
    
    return defaultValue;
  }

  public static String getText(String name)
  {
    return variable.get(name).getStr("text_value");
  }

  public static String getType(String name)
  {
    return variable.get(name).getStr("type");
  }

}
