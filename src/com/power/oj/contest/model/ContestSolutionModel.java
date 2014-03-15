package com.power.oj.contest.model;

import com.jfinal.plugin.activerecord.Model;

public class ContestSolutionModel extends Model<ContestSolutionModel>
{
  private static final long serialVersionUID = 1L;
  
  public static final ContestSolutionModel dao = new ContestSolutionModel();
  
  public static final String TABLE_NAME = "contest_solution";
  public static final String SID = "sid";
  public static final String UID = "uid";
  public static final String PID = "pid";
  public static final String CID = "cid";
  public static final String NUM = "num";
  public static final String TIME = "time";
  public static final String MEMORY = "memory";
  public static final String RESULT = "result";
  public static final String LANGUAGE = "language";
  public static final String CTIME = "ctime";
  public static final String ERROR = "error";
  public static final String SOURCE = "source";
  public static final String CODE_LEN = "codeLen";
  public static final String SYSTEM_ERROR = "systemError";

  public Integer getSid()
  {
    return getInt(SID);
  }
  
  public ContestSolutionModel setSid(Integer value)
  {
    return set(SID, value);
  }
  
  public Integer getUid()
  {
    return getInt(UID);
  }
  
  public ContestSolutionModel setUid(Integer value)
  {
    return set(UID, value);
  }
  
  public Integer getPid()
  {
    return getInt(PID);
  }
  
  public ContestSolutionModel setPid(Integer value)
  {
    return set(PID, value);
  }
  
  public Integer getCid()
  {
    return getInt(CID);
  }
  
  public ContestSolutionModel setCid(Integer value)
  {
    return set(CID, value);
  }
  
  public Integer getNum()
  {
    return getInt(NUM);
  }
  
  public ContestSolutionModel setNum(Integer value)
  {
    return set(NUM, value);
  }
  
  public Integer getTime()
  {
    return getInt(TIME);
  }
  
  public ContestSolutionModel setTime(Integer value)
  {
    return set(TIME, value);
  }
  
  public Integer getMemory()
  {
    return getInt(MEMORY);
  }
  
  public ContestSolutionModel setMemory(Integer value)
  {
    return set(MEMORY, value);
  }
  
  public Integer getResult()
  {
    return getInt(RESULT);
  }
  
  public ContestSolutionModel setResult(Integer value)
  {
    return set(RESULT, value);
  }
  
  public Integer getLanguage()
  {
    return getInt(LANGUAGE);
  }
  
  public ContestSolutionModel setLanguage(Integer value)
  {
    return set(LANGUAGE, value);
  }
  
  public Integer getCtime()
  {
    return getInt(CTIME);
  }
  
  public ContestSolutionModel setCtime(Integer value)
  {
    return set(CTIME, value);
  }
  
  public String getError()
  {
    return getStr(ERROR);
  }
  
  public ContestSolutionModel setError(String value)
  {
    return set(ERROR, value);
  }
  
  public String getSource()
  {
    return getStr(SOURCE);
  }
  
  public ContestSolutionModel setSource(String value)
  {
    return set(SOURCE, value);
  }
  
  public Integer getCodeLen()
  {
    return getInt(CODE_LEN);
  }
  
  public ContestSolutionModel setCodeLen(Integer value)
  {
    return set(CODE_LEN, value);
  }
  
  public String getSystemError()
  {
    return getStr(SYSTEM_ERROR);
  }
  
  public ContestSolutionModel setSystemError(String value)
  {
    return set(SYSTEM_ERROR, value);
  }
  
}