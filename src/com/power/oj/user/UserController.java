package com.power.oj.user;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import jodd.util.HtmlEncoder;
import jodd.util.StringUtil;

import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresGuest;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresUser;

import com.jfinal.aop.Before;
import com.jfinal.aop.ClearInterceptor;
import com.jfinal.aop.ClearLayer;
import com.jfinal.core.ActionKey;
import com.jfinal.ext.interceptor.POST;
import com.jfinal.ext.plugin.shiro.ClearShiro;
import com.jfinal.kit.PathKit;
import com.jfinal.upload.UploadFile;
import com.power.oj.core.OjConfig;
import com.power.oj.core.OjConstants;
import com.power.oj.core.OjController;
import com.power.oj.core.bean.FlashMessage;
import com.power.oj.core.bean.MessageType;
import com.power.oj.core.service.OjService;
import com.power.oj.core.service.SessionService;
import com.power.oj.shiro.ShiroKit;
import com.power.oj.user.validator.RecoverAccountValidator;
import com.power.oj.user.validator.ResetPasswordValidator;
import com.power.oj.user.validator.SignupValidator;
import com.power.oj.user.validator.UpdateUserValidator;

/**
 * 
 * @author power
 * 
 */
public class UserController extends OjController
{
  /*
   * If most controllers use these services, move them to OjController.
   */
  private static final UserService userService = UserService.me();
  private static final SessionService sessionService = SessionService.me();
  
  @RequiresUser
  public void index()
  {
    UserModel userModel = userService.getCurrentUserExt();
    userService.isCheckin(userModel);
    
    setAttr(OjConstants.USER, userModel);
    
    setTitle(getText("user.index.title"));
  }

  @ActionKey("/rank")
  public void rank()
  {
    int pageNumber = getParaToInt(0, 1);
    int pageSize = getParaToInt(1, OjConfig.userPageSize);
    
    setAttr("pageSize", OjConfig.userPageSize);
    setAttr(OjConstants.USER_LIST, userService.getUserRankList(pageNumber, pageSize));
    
    setTitle(getText("user.rank.title"));
  }

  public void profile()
  {
    String name = getPara(0);
    UserModel userModel = userService.getUserProfile(name);
    
    if (userModel == null)
    {
      FlashMessage msg = new FlashMessage(getText("user.error.none"), MessageType.WARN, getText("message.warn.title"));
      redirect("/", msg);
      return;
    }
   
    setAttr("userModel", userModel);
    
    setTitle(getText("user.profile.title"));
  }
  
  public void option()
  {
    
  }
  
  @RequiresUser
  public void loginlog()
  {
    int pageNumber = getParaToInt(0, 1);
    int pageSize = getParaToInt(1, OjConfig.userPageSize);
    
    setAttr("pageSize", OjConfig.userPageSize);
    setAttr("logs", userService.getLoginlog(pageNumber, pageSize));
    setTitle(getText("user.loginlog.title"));
  }

  public void search()
  {
    int pageNumber = getParaToInt(0, 1);
    int pageSize = getParaToInt(1, OjConfig.userPageSize);
    String word = HtmlEncoder.text(getPara("word", "").trim());
    String scope = getPara("scope");
    StringBuilder query = new StringBuilder();
    
    query.append("?word=").append(word);
    if (StringUtil.isNotBlank(scope) && "all".equals(scope) == false)
    {
      query.append("&scope=").append(scope);
    }
    
    setAttr(OjConstants.USER_LIST, userService.searchUser(pageNumber, pageSize, scope, word));
    setAttr("pageSize", OjConfig.userPageSize);
    setAttr("word", word);
    setAttr("scope", scope != null ? scope : "all");
    setAttr("query", query.toString());
    
    setTitle(new StringBuilder(2).append(getText("user.search.title")).append(word).toString());
  }

  @ActionKey("/login")
  public void login()
  {
    if (ShiroKit.isAuthenticated())
    {
      FlashMessage msg = new FlashMessage(getText("user.error.login"), MessageType.WARN, getText("message.warn.title"));
      redirect(sessionService.getLastAccessURL(), msg);
      return;
    }
    
    setTitle(getText("user.login.title"));
  }

  @Before(POST.class)
  @ClearShiro
  public void signin()
  {
    if (ShiroKit.isAuthenticated())
    {
      FlashMessage msg = new FlashMessage(getText("user.error.login"), MessageType.WARN, getText("message.warn.title"));
      redirect(sessionService.getLastAccessURL(), msg);
      return;
    }
    
    String name = getPara("name").trim();
    String password = getPara("password");
    boolean rememberMe = getParaToBoolean("rememberMe", false);

    if (userService.login(this, name, password, rememberMe))
    {
      redirect(sessionService.getLastAccessURL());
      return;
    }
    // TODO: recodr login fail times

    FlashMessage msg = new FlashMessage(getText("user.signin.error"), MessageType.ERROR, getText("message.error.title"));
    setAttrMessage(msg);
    keepPara("name");
    
    setTitle(getText("user.login.title"));
    render("login.html");
  }

  @ClearInterceptor(ClearLayer.ALL)
  @ActionKey("/logout")
  public void logout()
  {
    String lastAccessURL = sessionService.getLastAccessURL();
    if (isParaExists("t"))
      lastAccessURL = getPara("t");
    
    userService.logout(this);

    redirect(lastAccessURL);
  }
  
  @RequiresUser
  public void archive()
  {
    try
    {
      File file = userService.archiveCode();
      renderFile(file);
    } catch (IOException e)
    {
      log.warn(e.getLocalizedMessage());
      if (OjConfig.getDevMode())
        e.printStackTrace();
      redirect(sessionService.getLastAccessURL());
    }
  }

  @RequiresPermissions("user:upload:avatar")
  public void avatar()
  {
    setTitle(getText("user.avatar.title"));
  }

  @Before(POST.class)
  @RequiresPermissions("user:upload:avatar")
  public void uploadAvatar()
  {
    UploadFile uploadFile = getFile("Filedata", "", 10 * 1024 * 1024, "UTF-8");
    File file = uploadFile.getFile();
    String rootPath = PathKit.getWebRootPath() + File.separator;
    String fileName = file.getAbsolutePath().replace(rootPath, "");
    int width = 400;
    int height = 400;

    log.info(file.getAbsolutePath());
    try
    {
      userService.uploadAvatar(file, width, height, this);
      setAttr("error", "false");
    } catch (Exception e)
    {
      setAttr("error", "true");
      if (OjConfig.getDevMode())
        e.printStackTrace();
      log.error(e.getLocalizedMessage());
    }

    setAttr("src", fileName);
    
    renderJson(new String[]{ "error", "src", "width", "height" });
  }

  @Before(POST.class)
  @RequiresPermissions("user:upload:avatar")
  public void saveAvatar() throws IOException
  {
    int x1 = getParaToInt("x1", 0);
    int y1 = getParaToInt("y1", 0);
    int x2 = getParaToInt("x2", OjConstants.AVATAR_WIDTH);
    int y2 = getParaToInt("y2", OjConstants.AVATAR_HEIGHT);
    FlashMessage msg = new FlashMessage(getText("user.avatar.success"));
   
    try
    {
      userService.saveAvatar(getPara("imageSource"), x1, y1, x2, y2);
    } catch (Exception e)
    {
      if (OjConfig.getDevMode())
        e.printStackTrace();
      log.error(e.getLocalizedMessage());
      msg = new FlashMessage(getText("user.avatar.error"), MessageType.ERROR, getText("message.error.title"));
    }

    String redirectURL = new StringBuilder(2).append("/user/profile/").append(getAttr(OjConstants.USER_NAME)).toString();
    redirect(redirectURL, msg);
  }

  @RequiresGuest
  public void forget()
  {
    if (!OjService.me().checkEmailConf())
    {
      FlashMessage msg = new FlashMessage(getText("conf.email.error"), MessageType.ERROR, getText("message.error.title"));
      redirect(sessionService.getLastAccessURL(), msg);
    }
    setTitle(getText("user.forget.title"));
  }
  
  @Before({POST.class, RecoverAccountValidator.class})
  @RequiresGuest
  public void recover()
  {
    String name = getPara("name");
    String email = getPara("email");
    String token = UUID.randomUUID().toString();
    UserModel userModel = userService.getUserByName(name);
    FlashMessage msg = new FlashMessage(getText("user.recover.success"));
    
    userModel.set("token", token).set("mtime", OjConfig.timeStamp).update();
    try
    {
      // TODO: create new thread to send mail
      OjService.me().sendResetPasswordEmail(name, email, token);
    } catch (Exception e)
    {
      if (OjConfig.getDevMode())
        e.printStackTrace();
      log.error(e.getLocalizedMessage());
      msg = new FlashMessage(getText("user.recover.error"), MessageType.ERROR, getText("message.error.title"));
    }
    
    redirect(sessionService.getLastAccessURL(), msg);
  }
  
  @RequiresGuest
  public void reset()
  {
    String name = getPara("name");
    String token = getPara("token");
   
    if (userService.checkResetToken(name, token))
    {
      setSessionAttr("name", name);
      setTitle(getText("user.reset.title"));
      return;
    }
    
    renderError(404);
  }
  
  @Before({POST.class, ResetPasswordValidator.class})
  @RequiresGuest
  public void resetPassword()
  {
    String name = getSessionAttr("name");
    String password = getPara("pass");
    
    if (userService.resetPassword(name, password))
    {
      removeSessionAttr("name");
    }
    
    FlashMessage msg = new FlashMessage(getText("user.resetPassword.success"));
    redirect("/login", msg);
  }
  
  public void verify()
  {
    String name = getPara("name");
    String token = getPara("token");
    
    if (userService.verifyEmail(name, token))
    {
      String redirectUrl = ShiroKit.isUser() ? sessionService.getLastAccessURL() : "/login";
      setFlashMessage(new FlashMessage(getText("user.verify.success")));
      redirect(redirectUrl);
      return;
    }
    
    renderError(404);
  }

  @RequiresGuest
  public void bind()
  {
    render("bind.html");
  }

  @ActionKey("/signup")
  @RequiresGuest
  public void signup()
  {
    boolean ajax = getParaToBoolean("ajax", false);
    
    setTitle(getText("user.signup.title"));
    
    if (ajax)
      render("ajax/signup.html");
    else
      render("signup.html");
  }
  
  @Before({POST.class, SignupValidator.class})
  @RequiresGuest
  public void save()
  {
    UserModel userModel = getModel(UserModel.class, "user");
    
    try
    {
      if (userService.signup(userModel))
      {
        setCookie("oj_username", userModel.getStr("name"), OjConstants.COOKIE_AGE);
        setFlashMessage(new FlashMessage(getText("user.save.success")));
      }
      else
      {
        // TODO
      }
    } catch (Exception e)
    {
      if (OjConfig.getDevMode())
        e.printStackTrace();
      log.error(e.getLocalizedMessage());
      setFlashMessage(new FlashMessage(getText("user.save.error")));
    }

    redirect("/login");
  }

  @RequiresAuthentication
  public void edit()
  {
    setAttr(OjConstants.PROGRAM_LANGUAGES, OjConfig.program_languages);
    
    setTitle(getText("user.edit.title"));
  }

  @Before({POST.class, UpdateUserValidator.class})
  @RequiresAuthentication
  public void update()
  {
    UserModel userModel = getModel(UserModel.class, "user");
    
    userService.updateUser(userModel);

    String redirectURL = new StringBuilder(2).append("/user/profile/").append(getAttr(OjConstants.USER_NAME)).toString();
    redirect(redirectURL, new FlashMessage(getText("user.update.success")));
  }

  /******************** admin methods ********************/
  @RequiresPermissions("user:delete")
  public void delete()
  {
    renderText("TODO");
  }

  @RequiresPermissions("user:online")
  public void online()
  {
    setAttr("loginUserNum", sessionService.getUserNumber());
    setAttr(OjConstants.USER_LIST, sessionService.getAccessLog());
    
    setTitle(getText("user.online.title"));
  }

  @RequiresPermissions("user:build")
  public void build()
  {
    String name = getPara(0);
    UserModel userModel = userService.getUserByName(name);
    String redirectURL = new StringBuilder(2).append("/user/profile/").append(name).toString();
    FlashMessage msg = new FlashMessage(getText("user.build.success"));
    
    if (!userService.build(userModel))
    {
      msg = new FlashMessage(getText("user.build.error"), MessageType.ERROR, getText("message.error.title"));
      log.error(msg.getContent());
    }

    redirect(redirectURL, msg);
  }
  
}
