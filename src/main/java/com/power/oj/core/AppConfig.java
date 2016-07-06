package com.power.oj.core;

import com.alibaba.druid.filter.stat.StatFilter;
import com.alibaba.druid.wall.WallFilter;
import com.jfinal.config.Constants;
import com.jfinal.config.Handlers;
import com.jfinal.config.Interceptors;
import com.jfinal.config.JFinalConfig;
import com.jfinal.config.Plugins;
import com.jfinal.config.Routes;
import com.jfinal.core.JFinal;
import com.jfinal.ext.plugin.shiro.ShiroInterceptor;
import com.jfinal.ext.plugin.shiro.ShiroPlugin;
import com.jfinal.i18n.I18n;
import com.jfinal.kit.PathKit;
import com.jfinal.log.Logger;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.druid.DruidPlugin;
import com.jfinal.plugin.druid.DruidStatViewHandler;
import com.jfinal.plugin.druid.IDruidStatViewAuth;
import com.jfinal.plugin.ehcache.EhCachePlugin;
import com.jfinal.render.CaptchaRender;
import com.jfinal.render.FreeMarkerRender;
import com.power.oj.admin.AdminController;
import com.power.oj.admin.ContestAdminController;
import com.power.oj.admin.ProblemAdminController;
import com.power.oj.admin.UserAdminController;
import com.power.oj.api.AdminApiController;
import com.power.oj.api.CommonApiController;
import com.power.oj.api.ContestApiController;
import com.power.oj.api.DiscussionApiController;
import com.power.oj.api.FriendApiController;
import com.power.oj.api.JudgeApiController;
import com.power.oj.api.MailApiController;
import com.power.oj.api.ProblemApiController;
import com.power.oj.api.UserApiController;
import com.power.oj.api.oauth.QQLoginApiController;
import com.power.oj.api.oauth.SinaLoginApiController;
import com.power.oj.api.oauth.WebLoginModel;
import com.power.oj.contest.ContestController;
import com.power.oj.contest.model.BoardModel;
import com.power.oj.contest.model.ContestClarifyModel;
import com.power.oj.contest.model.ContestModel;
import com.power.oj.contest.model.ContestProblemModel;
import com.power.oj.contest.model.ContestSolutionModel;
import com.power.oj.contest.model.FreezeBoardModel;
import com.power.oj.core.controller.MainController;
import com.power.oj.core.controller.UeditorController;
import com.power.oj.core.handler.BaseUrlHandler;
import com.power.oj.core.handler.SessionHandler;
import com.power.oj.core.handler.UrlFilterHandler;
import com.power.oj.core.interceptor.AccessLogInterceptor;
import com.power.oj.core.interceptor.GlobalInterceptor;
import com.power.oj.core.interceptor.OjVariableInterceptor;
import com.power.oj.core.interceptor.SessionAttrInterceptor;
import com.power.oj.core.interceptor.TimingInterceptor;
import com.power.oj.core.model.ProgramLanguageModel;
import com.power.oj.core.model.SessionModel;
import com.power.oj.core.model.VariableModel;
import com.power.oj.discussion.CommentModel;
import com.power.oj.discussion.DiscussionController;
import com.power.oj.discussion.TopicModel;
import com.power.oj.honor.HonorController;
import com.power.oj.honor.HonorModel;
import com.power.oj.mail.MailContentModel;
import com.power.oj.mail.MailController;
import com.power.oj.mail.MailModel;
import com.power.oj.news.NewsController;
import com.power.oj.news.NewsModel;
import com.power.oj.notice.NoticeController;
import com.power.oj.notice.NoticeModel;
import com.power.oj.problem.ProblemController;
import com.power.oj.problem.ProblemModel;
import com.power.oj.service.EhcacheService;
import com.power.oj.social.FriendGroupModel;
import com.power.oj.social.FriendModel;
import com.power.oj.solution.SolutionController;
import com.power.oj.solution.SolutionModel;
import com.power.oj.user.RoleModel;
import com.power.oj.user.UserController;
import com.power.oj.user.UserExtModel;
import com.power.oj.user.UserInterceptor;
import com.power.oj.user.UserModel;
import com.power.oj.util.freemarker.BlockDirective;
import com.power.oj.util.freemarker.ExtendsDirective;
import com.power.oj.util.freemarker.Num2AlphaMethod;
import com.power.oj.util.freemarker.OverrideDirective;
import jodd.util.SystemUtil;
import org.apache.shiro.SecurityUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Locale;

public class AppConfig extends JFinalConfig {
    private static final String BASE_VIEW_PATH = "/WEB-INF/view";
    private static final Logger log = Logger.getLogger(AppConfig.class);
    private Routes routes;

    public static String getBaseViewPath() {
        return BASE_VIEW_PATH;
    }

    /**
     * 建议使用 JFinal 手册推荐的方式启动项目 运行此 main 方法可以启动项目，此main方法可以放置在任意的Class类定义中，不一定要放于此
     * 使用内置的Jetty容器， 基于Tomcat开发，需要将jetty.jar删除
     */
    public static void main(String[] args) {
        JFinal.start("WebRoot", 8000, "/", 5);
    }

    /**
     * 配置常量
     */
    public void configConstant(Constants me) {
        loadPropertyFile("oj.properties");

        FreeMarkerRender.getConfiguration().setSharedVariable("block", new BlockDirective());
        FreeMarkerRender.getConfiguration().setSharedVariable("override", new OverrideDirective());
        FreeMarkerRender.getConfiguration().setSharedVariable("extends", new ExtendsDirective());
        FreeMarkerRender.getConfiguration().setSharedVariable("num2alpha", new Num2AlphaMethod());

        me.setDevMode(getPropertyToBoolean("devMode", false));
        me.setBaseViewPath(BASE_VIEW_PATH);
        me.setError401View(BASE_VIEW_PATH + "/error/401.html");
        me.setError403View(BASE_VIEW_PATH + "/error/403.html");
        me.setError404View(BASE_VIEW_PATH + "/error/404.html");
        me.setError500View(BASE_VIEW_PATH + "/error/500.html");

        log.debug("configConstant finished.");
    }

    /**
     * 配置路由
     */
    public void configRoute(Routes me) {
        this.routes = me;

        me.add("/", MainController.class, "/common/");
        me.add("/admin", AdminController.class);
        me.add("/admin/contest", ContestAdminController.class);
        me.add("/admin/problem", ProblemAdminController.class);
        me.add("/admin/user", UserAdminController.class);
        me.add("/api", CommonApiController.class, "/common/");
        me.add("/api/admin", AdminApiController.class, "/admin/");
        me.add("/api/contest", ContestApiController.class, "/contest/");
        me.add("/api/discuss", DiscussionApiController.class, "/discuss/");
        me.add("/api/friend", FriendApiController.class, "/user/");
        me.add("/api/mail", MailApiController.class, "/mail/");
        me.add("/api/oauth/qq", QQLoginApiController.class, "/user/");
        me.add("/api/oauth/sina", SinaLoginApiController.class, "/user/");
        me.add("/api/problem", ProblemApiController.class, "/problem/");
        me.add("/api/user", UserApiController.class, "/user/");
        me.add("/api/judge", JudgeApiController.class, "/solution/");
        me.add("/contest", ContestController.class);
        me.add("/discuss", DiscussionController.class);
        me.add("/mail", MailController.class);
        me.add("/notice", NoticeController.class);
        me.add("/honor", HonorController.class);
        me.add("/news", NewsController.class);
        me.add("/problem", ProblemController.class);
        me.add("/solution", SolutionController.class);
        me.add("/ueditor", UeditorController.class, "/common/");
        me.add("/user", UserController.class);

        log.debug("configRoute finished.");
    }

    /**
     * 配置插件
     */
    public void configPlugin(Plugins me) {
        DruidPlugin druidPlugin;
        if (isAceMode()) {
            druidPlugin = new DruidPlugin(getProperty("ace.jdbcUrl"), getProperty("ace.user"),
                getProperty("ace.password").trim());
        } else {
            druidPlugin = new DruidPlugin(getProperty("dev.jdbcUrl"), getProperty("dev.user"),
                getProperty("dev.password").trim());
        }
        druidPlugin.addFilter(new StatFilter());
        WallFilter wall = new WallFilter();
        wall.setDbType("mysql");
        druidPlugin.addFilter(wall);
        me.add(druidPlugin);

        // 配置ActiveRecord插件
        ActiveRecordPlugin arp = new ActiveRecordPlugin(druidPlugin);
        arp.setShowSql(getPropertyToBoolean("devMode", false));
        arp.addMapping("board", BoardModel.class);
        arp.addMapping("comment", CommentModel.class);
        arp.addMapping("contest", "cid", ContestModel.class);
        arp.addMapping("contest_clarify", ContestClarifyModel.class);
        arp.addMapping("contest_problem", ContestProblemModel.class);
        arp.addMapping("contest_solution", "sid", ContestSolutionModel.class);
        arp.addMapping(FreezeBoardModel.TABLE_NAME, FreezeBoardModel.class);
        arp.addMapping("friend", FriendModel.class);
        arp.addMapping("friend_group", FriendGroupModel.class);
        arp.addMapping("mail", MailModel.class);
        arp.addMapping("mail_content", MailContentModel.class);
        arp.addMapping("notice", NoticeModel.class);
        arp.addMapping("honors", HonorModel.class);
        arp.addMapping("news", NewsModel.class);
        arp.addMapping("problem", "pid", ProblemModel.class);
        arp.addMapping("program_language", ProgramLanguageModel.class);
        arp.addMapping("role", RoleModel.class);
        arp.addMapping("session", "sessionId", SessionModel.class);
        arp.addMapping("solution", "sid", SolutionModel.class);
        arp.addMapping("topic", TopicModel.class);
        arp.addMapping("user", "uid", UserModel.class); // 映射user表到 User模型,主键是uid
        arp.addMapping("user_ext", "uid", UserExtModel.class);
        arp.addMapping("variable", VariableModel.class);
        arp.addMapping("web_login", WebLoginModel.class);
        me.add(arp);

        me.add(new EhCachePlugin());
        me.add(new ShiroPlugin(routes));

        log.debug("configPlugin finished.");
    }

    /**
     * 配置全局拦截器
     */
    public void configInterceptor(Interceptors me) {
        me.add(new TimingInterceptor());
        me.add(new GlobalInterceptor());
        me.add(new OjVariableInterceptor());
        me.add(new SessionAttrInterceptor());
        me.add(new AccessLogInterceptor());
        me.add(new UserInterceptor());
        me.add(new ShiroInterceptor());

        log.debug("configInterceptor finished.");
    }

    /**
     * 配置处理器
     */
    public void configHandler(Handlers me) {
        me.add(new SessionHandler());
        me.add(new BaseUrlHandler());
        me.add(new UrlFilterHandler());
        if (OjConfig.isDevMode()) {
            me.add(new DruidStatViewHandler("/admin/druid", new IDruidStatViewAuth() {
                public boolean isPermitted(HttpServletRequest request) {
                    return SecurityUtils.getSubject().isPermitted("system");
                }
            }));
        }

        log.debug("configHandler finished.");
    }

    /**
     * 初始化常量
     */
    @Override
    public void afterJFinalStart() {
        OjConfig.initJudgeResult();
        OjConfig.loadConfig();

        CaptchaRender.setCaptchaName("PowerOJCaptcha");
        I18n.setDefaultBaseName("ojText");
        // TODO:
        I18n.setDefaultLocale(Locale.ENGLISH.toString());
        if (!OjConfig.isDevMode()) {
            EhcacheService.start();

            loadPropertyFile("oj.properties");
            OjConfig.setBaseURL(getProperty("baseURL"));
        }
        OjConfig.judgeVersion = getProperty("judge.version", "v1.0");

        log.info(PathKit.getWebRootPath());
        log.debug("afterJFinalStart finished.");
    }

    @Override
    public void beforeJFinalStop() {
        //SessionService.me().deleteAllSession();
        EhcacheService.destroy();
        log.debug("beforeJFinalStop");
    }

    private boolean isAceMode() {
        return "/home/usera".equals(SystemUtil.getUserHome());
    }

}
