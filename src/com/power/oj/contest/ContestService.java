package com.power.oj.contest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jodd.util.StringUtil;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.power.oj.core.OjConfig;
import com.power.oj.core.bean.ResultType;
import com.power.oj.core.model.LanguageModel;
import com.power.oj.problem.ProblemModel;
import com.power.oj.user.UserService;

public class ContestService
{
  private static final ContestModel dao = ContestModel.dao;
  private static final ContestService me = new ContestService();
  
  private ContestService() {}
  public static ContestService me()
  {
    return me;
  }

  public Page<ContestModel> getPage(int pageNumber, int pageSize, int type, int status)
  {
    List<Object> paras = new ArrayList<Object>();
    String sql = "SELECT *,FROM_UNIXTIME(start_time, '%Y-%m-%d %H:%i:%s') AS start_time_t,FROM_UNIXTIME(end_time, '%Y-%m-%d %H:%i:%s') AS end_time_t";
    StringBuilder sb = new StringBuilder("FROM contest WHERE 1=1");
    if (type > -1)
    {
      sb.append(" AND type=?");
      paras.add(type);
    }

    if (status == 0)
    {
      sb.append(" AND start_time>UNIX_TIMESTAMP()");
    } else if (status == 1)
    {
      sb.append(" AND start_time<UNIX_TIMESTAMP() AND end_time>UNIX_TIMESTAMP()");
    } else if (status == 2)
    {
      sb.append(" AND end_time<UNIX_TIMESTAMP()");
    }

    sb.append(" ORDER BY cid DESC");

    Page<ContestModel> ContestList = dao.paginate(pageNumber, pageSize, sql, sb.toString(), paras.toArray());

    for (ContestModel contest : ContestList.getList())
    {
      long ctime = OjConfig.timeStamp;
      int start_time = contest.getInt("start_time");
      int end_time = contest.getInt("end_time");
      String cstatus = "Running";

      if (start_time > ctime)
        cstatus = "Pending";
      else if (end_time < ctime)
        cstatus = "Finished";

      contest.put("cstatus", cstatus);

      String ctype = "Public";
      if (contest.getInt("type") == 1)
      {
        ctype = "Private";
      } else if (contest.getInt("type") == 2)
      {
        ctype = "Strict Private";
      } else if (contest.getInt("type") == 3)
      {
        ctype = "Password";
      } else if (contest.getInt("type") == 4)
      {
        ctype = "Test";
      }
      contest.put("ctype", ctype);
    }

    return ContestList;
  }

  public Page<Record> getContestRank(int pageNumber, int pageSize, int cid)
  {
    String sql = "FROM board b LEFT JOIN user u ON u.uid=b.uid WHERE b.cid=? ORDER BY accepts DESC,penalty";
    Page<Record> userRank = Db.paginate(pageNumber, pageSize, "SELECT b.*,u.name,u.nick,u.realname", sql, cid);
    
    return userRank;
  }
  
  public ContestModel getContest(int cid)
  {
    ContestModel contestModle = dao.findFirst(
        "SELECT *,FROM_UNIXTIME(start_time, '%Y-%m-%d %H:%i:%s') AS start_time_t,FROM_UNIXTIME(end_time, '%Y-%m-%d %H:%i:%s') AS end_time_t FROM contest WHERE cid=? LIMIT 1",
        cid);
    return contestModle;
  }

  public String getContestTitle(int cid)
  {
    ContestModel contestModle = dao.findFirst("SELECT title FROM contest WHERE cid=? LIMIT 1", cid);
    if (contestModle != null)
      return contestModle.get("title");
    return null;
  }

  public List<Record> getContestProblems(int cid, int uid)
  {
    String sql = "SELECT * FROM contest_problem WHERE cid=? ORDER BY num";
    List<Record> contestProblems;
    if (uid > 0)
    {
      sql = "SELECT cp.pid,cp.num,cp.accept,cp.submit,title,status FROM contest_problem cp LEFT OUTER JOIN (SELECT pid,MIN(result) AS status FROM contest_solution WHERE uid=? AND cid=? GROUP BY pid)AS temp ON cp.pid=temp.pid WHERE cp.cid=? ORDER BY num";
      contestProblems = Db.find(sql, uid, cid, cid);
    } else
    {
      contestProblems = Db.find(sql, cid);
    }

    for (Record problem : contestProblems)
    {
      problem.set("id", (char) (problem.getInt("num") + 'A'));
    }

    return contestProblems;
  }

  public long getProblemCount(int cid)
  {
    Record record = Db.findFirst("SELECT COUNT(pid) AS count FROM contest_problem WHERE cid=? LIMIT 1", cid);
    if (record != null)
      return record.getLong("count");
    return 0L;
  }

  public ProblemModel getProblem(int cid, int num)
  {
    Record record = Db.findFirst("SELECT pid,title,accept,submit FROM contest_problem WHERE cid=? AND num=? LIMIT 1", cid, num);
    if (record == null)
      return null;

    int pid = record.getInt("pid");
    ProblemModel problem = ProblemModel.dao.findById(pid);
    if (problem == null)
      return null;
    String title = record.getStr("title");
    if (StringUtil.isNotBlank(title))
      problem.set("title", title);

    problem.set("accept", record.get("accept"));
    problem.set("submit", record.get("submit"));
    problem.put("id", (char) (num + 'A'));
    problem.put("num", num);

    int sample_input_rows = 1;
    if (StringUtil.isNotBlank(problem.getStr("sample_input")))
      sample_input_rows = StringUtil.count(problem.getStr("sample_input"), '\n') + 1;
    problem.put("sample_input_rows", sample_input_rows);
    
    int sample_output_rows = 1;
    if (StringUtil.isNotBlank(problem.getStr("sample_output")))
      sample_output_rows = StringUtil.count(problem.getStr("sample_output"), '\n') + 1;
    problem.put("sample_output_rows", sample_output_rows);
    
    return problem;
  }

  public int getPid(int cid, int num)
  {
    Record record = Db.findFirst("SELECT pid,title FROM contest_problem WHERE cid=? AND num=? LIMIT 1", cid, num);
    if (record == null)
      return 0;

    return record.getInt("pid");
  }

  public String getProblemTitle(int cid, int num)
  {
    Record record = Db.findFirst("SELECT pid,title FROM contest_problem WHERE cid=? AND num=? LIMIT 1", cid, num);
    if (record == null)
      return null;

    return record.get("title");
  }

  public Integer getUserResult(Integer cid, Integer num)
  {
    Integer uid = UserService.me().getCurrentUid();
    if (uid == null)
      return null;
    
    return Db.queryInt("SELECT MIN(result) AS result FROM contest_solution WHERE cid=? AND uid=? AND num=? LIMIT 1", cid, uid, num);
  }
  
  public int getContestStatus(int cid)
  {
    ContestModel contestModle = dao.findFirst("SELECT start_time,end_time FROM contest WHERE cid=? LIMIT 1", cid);
    if (contestModle == null)
      return -1;
    long ctime = OjConfig.timeStamp;
    int start_time = contestModle.getInt("start_time");
    int end_time = contestModle.getInt("end_time");

    if (start_time > ctime)
      return ContestModel.Pending;
    else if (end_time < ctime)
      return  ContestModel.Finished;

    return  ContestModel.Running;
  }

  public List<Record> getContestStatistics(int cid)
  {
    StringBuilder sb = new StringBuilder("SELECT ");
    for (LanguageModel language : OjConfig.program_languages)
    {
      sb.append("COUNT(IF(language=").append(language.getInt("id")).append(",1,NULL)) AS ").append(language.getStr("ext")).append(",");
    }
    for (ResultType resultType : OjConfig.judge_result)
    {
      if (resultType.getId() > 9)
        break;
      sb.append("COUNT(IF(result=").append(resultType.getId()).append(",1,NULL)) AS ").append(resultType.getName()).append(",");
    }
    sb.append("pid,num,COUNT(IF(result>9,1,NULL)) AS Others,COUNT(*) AS total FROM contest_solution WHERE cid=? GROUP BY pid ORDER BY num");
    List<Record> statistics = Db.find(sb.toString(), cid);
    for (Record record : statistics)
    {
      record.set("id", (char) (record.getInt("num") + 'A'));
    }

    return statistics;
  }

  public boolean buildRank(int cid)
  {
    Db.update("DELETE FROM board WHERE cid=?", cid);
    ContestModel contestModle = getContest(cid);
    int contestStartTime = contestModle.getInt("start_time");
    List<Record> solutions = Db.find("SELECT uid,pid,num,result,ctime FROM contest_solution WHERE cid=? ORDER BY sid", cid);
    HashMap<Object, UserInfo> userRank = new HashMap<Object, UserInfo>();
    UserInfo userInfo = null;
    int uid = 0;
    int num = 0;
    int result = 0;
    int ctime = 0;
    int penalty = 0;
    int firstBoold[] = new int[26];
    int firstBooldTime[] = new int[26];
    for (int i = 0; i < 26; ++i)
      firstBooldTime[i] = -1;
    int accept[] = new int[26];
    int submit[] = new int[26];
    for (Record solution : solutions)
    {
      ++submit[num];
      uid = solution.getInt("uid");
      num = solution.getInt("num");
      result = solution.getInt("result");
      ctime = solution.getInt("ctime");
      userInfo = (UserInfo) userRank.get(uid);
      penalty = (ctime - contestStartTime) / 60;
      if (userInfo == null)
      {
        userInfo = new UserInfo(uid);
        if (result == ResultType.AC)
        {
          ++userInfo.accepts;
          ++accept[num];
          userInfo.acTime[num] = penalty;
          userInfo.penalty += penalty;
        } else
        {
          ++userInfo.waTime[num];
        }
        userRank.put(uid, userInfo);
      } else if (userInfo.acTime[num] == 0)
      {
        if (result == ResultType.AC)
        {
          ++userInfo.accepts;
          ++accept[num];
          userInfo.acTime[num] = penalty;
          penalty += userInfo.waTime[num] * 20;
          userInfo.penalty += penalty;
        } else
        {
          ++userInfo.waTime[num];
        }
      }
      if (result == ResultType.AC && firstBoold[num] == 0)
      {
        firstBoold[num] = uid;
        firstBooldTime[num] = penalty;
      }
    }

    for (Map.Entry<Object, UserInfo> entry : userRank.entrySet())
    {
      userInfo = entry.getValue();
      List<Object> paras = new ArrayList<Object>();
      paras.add(cid);
      paras.add(userInfo.uid);
      paras.add(userInfo.accepts);
      paras.add(userInfo.penalty);
      StringBuilder fields = new StringBuilder();
      StringBuilder values = new StringBuilder();
      for (int i = 0; i < 26; ++i)
      {
        char c = (char) (i + 'A');
        if (userInfo.acTime[i] > 0)
        {
          fields.append(",").append(c).append("_time");
          values.append(",?");
          paras.add(userInfo.acTime[i]);
        }

        if (userInfo.waTime[i] > 0)
        {
          fields.append(",").append(c).append("_WrongSubmits");
          paras.add(userInfo.waTime[i]);
          values.append(",?");
        }
      }
      StringBuilder sb = new StringBuilder(5).append("INSERT INTO board (cid,uid,accepts,penalty").append(fields.toString()).append(") VALUES (?,?,?,?")
          .append(values.toString()).append(")");
      Db.update(sb.toString(), paras.toArray());
    }
    for (int i = 0; i < 26; ++i)
    {
      Db.update("UPDATE contest_problem SET first_blood=?,first_blood_time=?,accept=?,submit=? WHERE cid=? AND num=?", firstBoold[i], firstBooldTime[i],
          accept[i], submit[i], cid, i);
    }

    return true;
  }

  public boolean isContestFinished(int cid)
  {
    return dao.findFirst("SELECT 1 FROM contest WHERE cid=? AND end_time<UNIX_TIMESTAMP() LIMIT 1", cid) != null;
  }

  public boolean isContestHasPassword(int cid)
  {
    return dao.findFirst("SELECT 1 FROM contest WHERE cid=? AND type=3 LIMIT 1", cid) != null;
  }

  public boolean checkContestPassword(int cid, String password)
  {
    return dao.findFirst("SELECT 1 FROM contest WHERE cid=? AND pass=? AND type=3 LIMIT 1", cid, password) != null;
  }

  public class UserInfo
  {
    public int cid;
    public int uid;
    public int accepts = 0;
    public int penalty = 0;
    public int acTime[] = new int[26];
    public int waTime[] = new int[26];

    UserInfo()
    {
    }

    UserInfo(int uid)
    {
      this.uid = uid;
    }
  }
  
}
