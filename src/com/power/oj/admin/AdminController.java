package com.power.oj.admin;

import org.apache.shiro.authz.annotation.RequiresPermissions;

import com.power.oj.core.OjController;

@RequiresPermissions("admin")
public class AdminController extends OjController
{
  private static final AdminService adminService = AdminService.me();
  public void index()
  {
    setAttrs(adminService.getSystemInfo());

    setTitle(getText("admin.index.title"));
  }
  
  public void info()
  {
    setAttrs(adminService.getOjInfo());
  }

}
