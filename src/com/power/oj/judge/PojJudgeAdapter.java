package com.power.oj.judge;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import jodd.io.FileNameUtil;
import jodd.io.FileUtil;

import com.jfinal.log.Logger;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.power.oj.contest.ContestModel;
import com.power.oj.contest.ContestService;
import com.power.oj.core.OjConfig;
import com.power.oj.core.bean.ResultType;
import com.power.oj.core.model.LanguageModel;
import com.power.oj.problem.ProblemModel;
import com.power.oj.solution.SolutionModel;
import com.power.oj.util.Tool;

public class PojJudgeAdapter extends Thread implements JudgeAdapter
{
  private static final Logger log = Logger.getLogger(PojJudgeAdapter.class);
  private static final ContestService contestService = ContestService.me();
  public static ArrayList<SolutionModel> judgeList = new ArrayList<SolutionModel>();

  public PojJudgeAdapter()
  {
    start();
  }

  public void run()
  {
    while (true)
    {
      SolutionModel solutionModel = null;
      synchronized (judgeList)
      {
        if (judgeList.isEmpty())
        {
          return;
        }
        log.info("Judge threads: " + judgeList.size());
        solutionModel = judgeList.get(0);
        judgeList.remove(0);
      }
      synchronized (JudgeAdapter.class)
      {
        try
        {
          if (Compile(solutionModel))
          {
            RunProcess(solutionModel);
          }
          else
          {
            log.warn("Compile failed.");
          }
        } catch (Exception e)
        {
          solutionModel.set("result", ResultType.SE).set("system_error", e.getMessage());
          solutionModel.update();

          if (OjConfig.getDevMode())
            e.printStackTrace();
          log.error(e.getMessage());
        }
      }
    }
  }

  public boolean Compile(SolutionModel solutionModel) throws IOException
  {
    log.info(solutionModel.getInt("sid") + " Start compiling...");
    String workPath = new StringBuilder(2).append(FileNameUtil.normalizeNoEndSeparator(OjConfig.get("work_path"))).append(File.separator).toString();
    // workPath = FileNameUtil.separatorsToSystem(workPath); //Converts all
    // separators to the system separator.
    if (OjConfig.getBoolean("delete_tmp_file"))
    {
      File prevWorkDir = new File(new StringBuilder(2).append(workPath).append(solutionModel.getInt("sid") - 2).toString());
      if (prevWorkDir.isDirectory())
      {
        FileUtil.deleteDir(prevWorkDir);
        log.info("Delete previous work directory " + prevWorkDir.getAbsolutePath());
      }
    }
    log.info("workPath: " + workPath);

    File workDir = new File(new StringBuilder(2).append(workPath).append(solutionModel.getInt("sid")).toString());
    FileUtil.mkdirs(workDir);
    log.info("mkdirs workDir: " + workDir.getAbsolutePath());

    LanguageModel language = (LanguageModel) OjConfig.language_type.get(solutionModel.getInt("language"));
    File sourceFile = new File(new StringBuilder(4).append(workDir.getAbsolutePath()).append(File.separator).append("Main.").append(language.getStr("ext")).toString());
    FileUtil.touch(sourceFile);
    FileUtil.writeString(sourceFile, solutionModel.getStr("source"));

    String comShellName = OjConfig.get("compile_shell");
    String compileCmdName = Tool.getCompileCmd(language.getStr("compile_cmd"), workDir.getAbsolutePath(), "Main", language.getStr("ext"));
    log.info("compileCmd: " + compileCmdName);

    /*
     * execute compiler command
     */
    Process compileProcess = Runtime.getRuntime().exec(comShellName);
    OutputStream comShellOutputStream = compileProcess.getOutputStream();
    comShellOutputStream.write(compileCmdName.getBytes());
    comShellOutputStream.flush();

    BufferedReader compileErrorBufferedReader = new BufferedReader(new InputStreamReader(compileProcess.getErrorStream()));
    long startTime = System.currentTimeMillis();
    StringBuilder sb = new StringBuilder();
    String errorOutput = "";
    while ((startTime + 10000L > System.currentTimeMillis()) && ((errorOutput = compileErrorBufferedReader.readLine()) != null))
    {
      sb.append(errorOutput).append("\n");
    }
    compileErrorBufferedReader.close();

    int ret = 0;
    try
    {
      ret = compileProcess.waitFor();
    } catch (InterruptedException e)
    {
      if (OjConfig.getDevMode())
        e.printStackTrace();
      log.warn("Compile Process is interrupted: " + e.getLocalizedMessage());
    }

    File mainProgram = new File(new StringBuilder(3).append(workDir.getAbsolutePath()).append(File.separator).append("Main.").append(language.getStr("exe")).toString());
    log.info(mainProgram.getAbsolutePath());
    boolean success = mainProgram.isFile();

    if (!success)
    {
      synchronized (JudgeAdapter.class)
      {
        // update DataBase
        solutionModel.set("result", ResultType.CE).set("time", 0).set("memory", 0).set("error", sb.toString());
        solutionModel.update();
      }
    }
    else
    {
      log.warn("Compile failed, return value: " + ret);
    }

    return success;
  }

  public boolean RunProcess(SolutionModel solutionModel) throws IOException, InterruptedException
  {
    log.info(solutionModel.getInt("sid") + " RunProcess...");
    /*
     * execute run command
     */
    Process runProcess = Runtime.getRuntime().exec(OjConfig.get("run_shell"));
    OutputStream runProcessOutputStream = runProcess.getOutputStream();
    log.info("runProcess: " + OjConfig.get("run_shell"));
    File dataDir = new File(new StringBuilder(3).append(OjConfig.get("data_path")).append(File.separator).append(solutionModel.getInt("pid")).toString());
    if (!dataDir.isDirectory())
    {
      String system_error = new StringBuilder(3).append("Data directory ").append(dataDir).append(" does not exist.").toString();
      solutionModel.set("result", ResultType.SE).set("system_error", system_error);
      solutionModel.update();
      log.error(system_error);
      return false;
    }
    ProblemModel problemModel = ProblemModel.dao.findById(solutionModel.getInt("pid"), "time_limit,memory_limit");

    List<String> inFiles = new ArrayList<String>();
    List<String> outFiles = new ArrayList<String>();
    File[] arrayOfFile = dataDir.listFiles();

    for (int i = 0; i < arrayOfFile.length; i++)
    {
      File in_file = arrayOfFile[i];
      if (!in_file.getName().toLowerCase().endsWith(DATA_EXT_IN))
        continue;
      File out_file = new File(new StringBuilder().append(dataDir.getAbsolutePath()).append(File.separator)
          .append(in_file.getName().substring(0, in_file.getName().length() - DATA_EXT_IN.length())).append(DATA_EXT_OUT).toString());
      if (!out_file.isFile())
        continue;
      inFiles.add(in_file.getAbsolutePath());
      outFiles.add(out_file.getAbsolutePath());
    }
    int numOfData = inFiles.size();

    LanguageModel language = (LanguageModel) OjConfig.language_type.get(solutionModel.getInt("language"));
    long timeLimit = problemModel.getInt("time_limit") * language.getInt("time_factor") + numOfData * language.getInt("ext_time");
    long caseTimeLimit = problemModel.getInt("time_limit") * language.getInt("time_factor") + language.getInt("ext_time");
    runProcessOutputStream.write((timeLimit + "\n").getBytes());
    runProcessOutputStream.write((caseTimeLimit + "\n").getBytes());

    long memoryLimit = (problemModel.getInt("memory_limit") + language.getInt("ext_memory")) * 1024L;
    runProcessOutputStream.write((memoryLimit + "\n").getBytes());
    log.info("timeLimit: " + timeLimit);
    log.info("caseTimeLimit: " + caseTimeLimit);
    log.info("memoryLimit: " + memoryLimit);

    File workDir = new File(new StringBuilder(3).append(OjConfig.get("work_path")).append(File.separator).append(solutionModel.getInt("sid")).toString());
    String mainProgram = new StringBuilder(4).append(workDir.getAbsolutePath()).append("\\Main.").append(language.getStr("exe")).append("\n").toString();
    runProcessOutputStream.write(mainProgram.getBytes());
    runProcessOutputStream.write((workDir.getAbsolutePath() + "\n").getBytes());
    log.info("mainProgram: " + mainProgram);
    log.info("dataDir: " + dataDir.getAbsolutePath());

    runProcessOutputStream.write((numOfData + "\n").getBytes());
    log.info("data files: " + numOfData);
    if (numOfData < 1)
    {
      log.warn("No data file for problem " + solutionModel.getInt("pid"));
    }
    for (int i = 0; i < numOfData; ++i)
    {
      runProcessOutputStream.write((inFiles.get(i) + "\n").getBytes());
      String userOutFile = new StringBuilder(4).append(workDir.getAbsolutePath()).append(File.separator).append(new File(outFiles.get(i)).getName()).append("\n").toString();
      runProcessOutputStream.write(userOutFile.getBytes());
      runProcessOutputStream.write((outFiles.get(i) + "\n").getBytes());
      log.info(inFiles.get(i));
      log.info(userOutFile);
      log.info(outFiles.get(i));
    }
    runProcessOutputStream.flush();

    BufferedReader inputStreamBufferedReader = new BufferedReader(new InputStreamReader(runProcess.getInputStream()));

    String buff = inputStreamBufferedReader.readLine();
    log.info("original time: " + buff);
    int time = Integer.parseInt(buff);
    buff = inputStreamBufferedReader.readLine();
    log.info("original memory: " + buff);
    int memory = Integer.parseInt(buff);
    if (memory > 0)
      memory -= language.getInt("ext_memory");
    StringBuilder sb = new StringBuilder();

    InputStream errorStream = runProcess.getErrorStream();
    while (errorStream.available() > 0)
    {
      Character c = new Character((char) errorStream.read());
      sb.append(c);
    }
    String errorOut = sb.toString();
    log.info("errorOut: " + errorOut);

    runProcessOutputStream.close();
    inputStreamBufferedReader.close();
    errorStream.close();

    int result = runProcess.waitFor();
    result = runProcess.exitValue();
    log.info("original result: " + result);
    if (result != ResultType.AC && (errorOut.indexOf("Exception") != -1 || errorOut.indexOf("Traceback") != -1))
      result = ResultType.RE;
    if (result < 0)
    {
      result = ResultType.SE;
    }
    log.info("result: " + result + "  time: " + time + "  memory: " + memory);

    synchronized (JudgeAdapter.class)
    {
      Integer cid = solutionModel.getInt("cid");
      int uid = solutionModel.getUid();
      if (cid != null && cid > 0)
      {
        // TODO move to contestService
        int num = solutionModel.getInt("num");
        
        Record contestProblem = Db.findFirst("SELECT * FROM contest_problem WHERE cid=? AND num=?", cid, num);
        if (contestProblem.getInt("first_blood") == 0)
        {
          ContestModel contestModle = contestService.getContest(cid);
          int contestStartTime = contestModle.getInt("start_time");
          contestProblem.set("first_blood", uid);
          contestProblem.set("first_blood_time", (solutionModel.getInt("ctime") - contestStartTime) / 60);
        }
        contestProblem.set("accept", contestProblem.getInt("accept")+1);
        /*char c = (char) (num + 'A');
        StringBuilder message = new StringBuilder().append("UID: ").append(uid).append(" Problem: ").append(c).append(" result: ").append(result).append("  time: ")
            .append(time).append("  memory: ").append(memory);
        // OjMessageInbound.broadcast(message.toString());
        ContestRankWebSocket.broadcast(cid, message.toString());*/
      }
      // update DataBase
      Db.update("UPDATE user SET accept=accept+1 WHERE uid=?", uid);
      // TODO update problem solved
      solutionModel.set("result", result).set("time", time).set("memory", memory);
      return solutionModel.update();
    }
  }

}