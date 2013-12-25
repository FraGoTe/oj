package com.power.oj.user;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;

import com.jfinal.log.Logger;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.power.oj.core.OjConfig;
import com.power.oj.core.service.SessionService;
import com.power.oj.shiro.ShiroKit;

public class UserService
{
  private static final Logger log = Logger.getLogger(UserService.class);

  private static final UserService me = new UserService();
  private static final UserModel dao = UserModel.dao;
  
  private UserService()
  {
    
  }
  
  public static UserService me()
  {
    return me;
  }

  public boolean login(String name, String password, boolean rememberMe)
  {
    Subject currentUser = getSubject();
    UsernamePasswordToken token = new UsernamePasswordToken(name, password);
    token.setRememberMe(rememberMe);

    try
    {
      currentUser.login(token);

      updateLogin(name ,true);
      SessionService.me().updateLogin();
    } catch (AuthenticationException ae)
    {
      updateLogin(name, false);
      log.warn("User signin failed.");
      return false;
    }

    return true;
  }

  public boolean updateLogin(String name, boolean success)
  {
    boolean ret = true;
    UserModel userModel = dao.getUserByName(name);
    Record loginLog = new Record();
    loginLog.set("uid", userModel.getUid()).set("ctime", OjConfig.timeStamp);
    loginLog.set("ip", SessionService.me().getHost()).set("succeed", success);
    ret = Db.save("loginlog", loginLog);
    
    Session session = SessionService.me().getSession();
    log.info(session.getClass().getName());
    if (success)
      ret = userModel.updateLogin() && ret;
    
    return ret;
  }

  public void logout()
  {
    Subject currentUser = getSubject();
    UserModel userModel = getPrincipal();
    if (userModel != null)
    {
      userModel.set("token", null);
      userModel.update();
    }

    currentUser.logout();
  }
  
  public UserModel getUserByName(String name)
  {
    return dao.getUserByName(name);
  }
  
  public Page<UserModel> getUserRankList(int pageNumber, int pageSize)
  {
    return dao.getUserRankList(pageNumber, pageSize);
  }

  public Subject getSubject()
  {
    return SecurityUtils.getSubject();
  }

  public UserModel getPrincipal()
  {
    Subject currentUser = getSubject();
    if (currentUser == null)
      return null;

    Object principal = currentUser.getPrincipal();
    if (principal == null)
      return null;

    return dao.findById(principal);
  }

  public boolean isAuthenticated()
  {
    return ShiroKit.isAuthenticated();
  }
  
  public boolean isRemembered()
  {
    return ShiroKit.isRemembered();
  }
  
  public boolean isUser()
  {
    return ShiroKit.isUser();
  }

  public boolean isGuest()
  {
    return ShiroKit.isGuest();
  }

  public boolean hasRole(String roleName)
  {
    return ShiroKit.hasRole(roleName);
  }
  
  public boolean lacksRole(String roleName)
  {
    return ShiroKit.lacksRole(roleName);
  }
  
  public boolean hasAnyRoles(String roleNames)
  {
    return ShiroKit.hasAnyRoles(roleNames);
  }
  
  public boolean hasAllRoles(String roleNames)
  {
    return ShiroKit.hasAllRoles(roleNames);
  }
  
  public boolean hasPermission(String permission)
  {
    return ShiroKit.hasPermission(permission);
  }
  
  public boolean lacksPermission(String permission)
  {
    return ShiroKit.lacksPermission(permission);
  }
  
}
