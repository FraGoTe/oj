package com.power.oj.api;

import com.jfinal.aop.Before;
import com.power.oj.core.OjConfig;
import com.power.oj.core.OjController;
import com.power.oj.social.SocialService;
import com.power.oj.user.UserService;

@Before(GuestInterceptor.class)
public class FriendApiController extends OjController
{
  private static final UserService userService = UserService.me();
  private static final SocialService socialService = SocialService.me();
  
  public void getGroupList()
  {
    Integer uid = userService.getCurrentUid();
    
    setAttr("groupList", socialService.getGroupList(uid));
    setAttr("success", true);
    renderJson(new String[]{"groupList", "success"});
  }
  
  public void getFollowingList()
  {
    Integer uid = userService.getCurrentUid();
    int pageNumber = getParaToInt("page", 1);
    int pageSize = getParaToInt("size", OjConfig.friendPageSize);
    //boolean isGroup = getParaToBoolean("isGroup", false);
    Integer gid = getParaToInt("gid", -1);
    
    renderJson(socialService.getFollowingList(pageNumber, pageSize, uid, gid));
  }
  
  public void getFollowedList()
  {
    Integer uid = userService.getCurrentUid();
    int pageNumber = getParaToInt("page", 1);
    int pageSize = getParaToInt("size", OjConfig.friendPageSize);
    
    renderJson(socialService.getFollowedList(pageNumber, pageSize, uid));
  }
  
  public void addGroup()
  {
    Integer uid = userService.getCurrentUid();
    String groupName = getPara("groupName");
    
    if (socialService.addGroup(uid, groupName))
    {
      renderJson("{\"success\":true, \"status\":200,\"result\":\"\"}");
    }
    else
    {
      renderJson("{\"success\":false, \"status\":-200,\"result\":\"Save friend group failed.\"}");
    }
  }
  
}