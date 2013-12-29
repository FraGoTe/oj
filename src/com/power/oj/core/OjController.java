package com.power.oj.core;

import javax.servlet.http.Cookie;

import com.jfinal.core.Controller;
import com.jfinal.log.Logger;
import com.power.oj.core.bean.Message;
import com.power.oj.user.UserModel;

/**
 * Base Controller provides some common methods.
 * public void <method_name>() { ... } will reagrd as a action!
 * 
 * @author power
 * 
 */
public class OjController extends Controller
{
  protected final Logger log = Logger.getLogger(getClass());

  /**
   * Redirect to url with flash message。
   * 
   * @param url
   *          string of destination url.
   * @param msg
   *          message with content, type and title.
   */
  protected void redirect(String url, Message msg)
  {
    setMessage(msg);
    super.redirect(url);
  }

  /**
   * Redirect to url with flash message。
   * 
   * @param url
   *          string of destination url.
   * @param withQueryString
   *          whether contains query string.
   * @param msg
   *          message with content, type and title.
   */
  protected void redirect(String url, boolean withQueryString, Message msg)
  {
    setMessage(msg);
    super.redirect(url, withQueryString);
  }

  /**
   * Set flash message in session.
   * 
   * @param message
   *          message with content, type and title.
   */
  protected void setMessage(Message message)
  {
    setSessionAttr(OjConstants.MSG, message.getContent());
    setSessionAttr(OjConstants.MSG_TYPE, message.getType());
    setSessionAttr(OjConstants.MSG_TITLE, message.getTitle());
  }

  /**
   * The the page title of the view.
   * 
   * @param title
   *          string of the page title.
   */
  protected void setTitle(String title)
  {
    setAttr(OjConstants.PAGE_TITLE, title);
  }

  /**
   * Set the cookie in safe way.
   * 
   * @param name
   * @param value
   * @param maxAgeInSeconds
   * @return this OjController.
   */
  protected OjController setCookieHttpOnly(String name, String value, int maxAgeInSeconds)
  {
    Cookie cookie = new Cookie(name, value);
    cookie.setMaxAge(maxAgeInSeconds);
    cookie.setPath("/");
    cookie.setHttpOnly(true);
    setCookie(cookie);
    return this;
  }
  
  protected UserModel getCurrentUser()
  {
    return getAttr(OjConstants.USER);
  }
  
}
