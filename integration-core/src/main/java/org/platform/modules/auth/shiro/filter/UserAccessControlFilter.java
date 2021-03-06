package org.platform.modules.auth.shiro.filter;

import java.io.IOException;

import javax.annotation.Resource;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.AccessControlFilter;
import org.apache.shiro.web.util.WebUtils;
import org.platform.modules.auth.biz.IUserBusiness;
import org.platform.modules.auth.common.Constants;
import org.platform.modules.auth.entity.User;

/**
 * 验证用户过滤器
 * 1、用户是否删除
 * 2、用户是否锁定
 */
public class UserAccessControlFilter extends AccessControlFilter {

    @Resource(name = "userBusiness")
    private IUserBusiness userBusiness = null;

    /**
     * 用户删除了后重定向的地址
     */
    private String userNotfoundUrl;
    /**
     * 用户锁定后重定向的地址
     */
    private String userBlockedUrl;
    /**
     * 未知错误
     */
    private String userUnknownErrorUrl;

    public String getUserNotfoundUrl() {
        return userNotfoundUrl;
    }

    public void setUserNotfoundUrl(String userNotfoundUrl) {
        this.userNotfoundUrl = userNotfoundUrl;
    }

    public String getUserBlockedUrl() {
        return userBlockedUrl;
    }

    public void setUserBlockedUrl(String userBlockedUrl) {
        this.userBlockedUrl = userBlockedUrl;
    }

    public String getUserUnknownErrorUrl() {
        return userUnknownErrorUrl;
    }

    public void setUserUnknownErrorUrl(String userUnknownErrorUrl) {
        this.userUnknownErrorUrl = userUnknownErrorUrl;
    }

    @Override
    protected boolean preHandle(ServletRequest request, ServletResponse response) throws Exception {
        Subject subject = getSubject(request, response);
        if (subject == null) {
            return true;
        }
        String username = (String) subject.getPrincipal();
        //此处注意缓存 防止大量的查询db
        User user = userBusiness.readDataByName(username);
        //把当前用户放到session中
        request.setAttribute(Constants.CURRENT_USER, user);
        ((HttpServletRequest)request).getSession().setAttribute(Constants.CURRENT_USER, user);
        ((HttpServletRequest)request).getSession().setAttribute(Constants.CURRENT_NICKNAME, user.getNickName());
        ((HttpServletRequest)request).getSession().setAttribute(Constants.CURRENT_USERNAME, username);
        return true;
    }


    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) throws Exception {
        User user = (User) request.getAttribute(Constants.CURRENT_USER);
        if (user == null) {
            return true;
        }

        if (Boolean.TRUE.equals(user.getAvailan())) {
            getSubject(request, response).logout();
            saveRequestAndRedirectToLogin(request, response);
            return false;
        }
        return true;
    }

    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        getSubject(request, response).logout();
        saveRequestAndRedirectToLogin(request, response);
        return true;
    }

    protected void redirectToLogin(ServletRequest request, ServletResponse response) throws IOException {
        User user = (User) request.getAttribute(Constants.CURRENT_USER);
        String url = null;
        if (Boolean.TRUE.equals(user.getAvailan())) {
            url = getUserNotfoundUrl();
        } else {
            url = getUserUnknownErrorUrl();
        }
        WebUtils.issueRedirect(request, response, url);
    }

}
