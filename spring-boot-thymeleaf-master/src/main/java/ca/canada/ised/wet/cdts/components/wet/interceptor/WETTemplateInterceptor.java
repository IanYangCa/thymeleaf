package ca.canada.ised.wet.cdts.components.wet.interceptor;

import java.io.File;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import ca.canada.ised.wet.cdts.components.wet.breadcrumbs.BreadCrumbs;
import ca.canada.ised.wet.cdts.components.wet.breadcrumbs.BreadcrumbService;
import ca.canada.ised.wet.cdts.components.wet.config.WETModelKey;
import ca.canada.ised.wet.cdts.components.wet.config.WETSettings;
import ca.canada.ised.wet.cdts.components.wet.exit.ExitScript;
import ca.canada.ised.wet.cdts.components.wet.exit.ExitTransaction;
import ca.canada.ised.wet.cdts.components.wet.footer.ContactInformation;
import hp.hpfb.web.handler.SideMenuHandler;
import hp.hpfb.web.service.utils.Utilities;

/**
 * The Class WETTemplateInterceptor populates the Spring Thymeleaf WET Template with properties from the cdn.properties
 * file or any page level overrides.
 *
 * @author Frank Giusto
 */
@Component
public class WETTemplateInterceptor extends HandlerInterceptorAdapter {

    /** Logging instance. */
    private static final Logger LOG = LogManager.getLogger(WETTemplateInterceptor.class);

//    @Bean
//    public MessageSource messageSource() {
//        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
//        messageSource.setBasename("cdn/common_messages");
//        messageSource.setDefaultEncoding("UTF-8");
//        return messageSource;
//    }
    /** The default cdn settings. */
    @Autowired
    @Qualifier("WETSettings")
    private WETSettings defaultCdnSettings;

    /** The side menu config. */
//    @Autowired
//    private SideMenuConfig sideMenuConfig;
    @Autowired
    private SideMenuHandler sideMenuHandler;

    /**
     * The show exit transaction link. This can be turned off in your application.properties
     */
    @Value("${show.wet.exit.transaction:true}")
    private Boolean showExitTransaction;

    /**
     * If you want to disable these breadcrumbs and use your own, set the property
     * <code>cdts.include.breadcrumbs=false</code> in <code>application.properties</code>.
     */
    @Value("${cdts.include.breadcrumbs:true}")
    private Boolean includeBreadCrumbs;

    /** Message source. */
    @Autowired
    private MessageSource applicationMessageSource;

    /** The breadcrumbs service. */
    @Autowired
    private BreadcrumbService breadcrumbsService;
    
    @Autowired
    private Utilities utilities;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
        throws Exception {
    	if(request.getRequestURI().endsWith("xmlHtml")) {
    		System.out.println("uri: " + request.getRequestURI());
    		Path path = Paths.get(utilities.UPLOADED_FOLDER, request.getSession().getId(), Utilities.FILE_SEPARATOR,"temp.htm");
    		File f = path.toFile();
    		if(f != null & f.length() > 0) {
    			response.setContentType(MediaType.TEXT_HTML.toString());
    			response.setCharacterEncoding("UTF-8");
    			PrintWriter writer = response.getWriter();
    			Files.lines(path, StandardCharsets.UTF_8).forEach(writer:: print);
    			writer.flush();
    			writer.close();
    			return false;
    		}
    	}
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           ModelAndView modelAndView) {

        if (null != modelAndView && StringUtils.isNotBlank(modelAndView.getViewName())) {

            LOG.debug("---Post Method Execution---postHandle() " + request.getServletPath() + ", "
                + modelAndView.getViewName());

            // Set any session timeout properties
            setSessionTimeoutProperties(defaultCdnSettings, modelAndView);

            // Set the Locale Toggle
            setLanguageRequestUrl(modelAndView, request);

            // Set Side Menu
            setSideMenu(modelAndView);

            // Breadcrumbs
            if (includeBreadCrumbs) {
                setBreadCrumbs(modelAndView, request);
            }

            // Leaving Secure Site
            getLeavingSecureSiteWarning(defaultCdnSettings, modelAndView);

            // Set Exit Transaction
            setExitTransaction(defaultCdnSettings, modelAndView, request);

            // Set Footer Links
            setFooterLinks(defaultCdnSettings, modelAndView, request);

            // Put CDN object in model view.
            modelAndView.addObject(WETModelKey.CDN.wetAttributeName(), defaultCdnSettings);
        }
    }

    /**
     * Sets the footer links if the user wishes to override the WET4 defaults.
     *
     * @param cdnSettings the cdn settings
     * @param modelAndView the model and view
     * @param request the request
     */
    private void setFooterLinks(WETSettings cdnSettings, ModelAndView modelAndView, HttpServletRequest request) {
        setContactInformation(cdnSettings, modelAndView, request);

        String privacyUrl = cdnSettings.getPrivacy().getUrl();
        String termsUrl = cdnSettings.getTerms().getUrl();

        if (StringUtils.isNotBlank(privacyUrl)) {
            modelAndView.addObject(WETModelKey.PRIVACY_LINK.wetAttributeName(), privacyUrl);
        }
        if (StringUtils.isNotBlank(termsUrl)) {
            modelAndView.addObject(WETModelKey.TERMS_LINK.wetAttributeName(), termsUrl);
        }

    }

    /**
     * Sets the contact information if the user wishes to override the WET4 defaults.
     *
     * @param cdnSettings the cdn settings
     * @param modelAndView the model and view
     * @param request the request
     */
    private void setContactInformation(WETSettings cdnSettings, ModelAndView modelAndView, HttpServletRequest request) {
        String contactHref = cdnSettings.getContact().getUrl();
        if (StringUtils.isNotBlank(contactHref)) {
            List<ContactInformation> contactInformationList = new ArrayList<>();
            ContactInformation contactInformation = new ContactInformation();

            contactInformation.setHref(contactHref);
            contactInformation.setText(
                applicationMessageSource.getMessage("cdn.contact.link.text", null, LocaleContextHolder.getLocale()));

            contactInformationList.add(contactInformation);
            modelAndView.addObject(WETModelKey.CONTACT_LINK.wetAttributeName(), contactInformationList);
        }
    }

    /**
     * Sets the exit transaction.
     *
     * @param cdnSettings the cdn settings
     * @param modelAndView the model and view
     * @param request the request
     */
    private void setExitTransaction(WETSettings cdnSettings, ModelAndView modelAndView, HttpServletRequest request) {
        // TODO It would be nice to do this only for pages using Transaction
        // layouts.
        if (showExitTransaction) {
            List<ExitTransaction> exitTransactionList = new ArrayList<>();
            ExitTransaction exitTransaction = new ExitTransaction();
            exitTransaction.setHref(request.getContextPath() + cdnSettings.getExit().getUrl());
            exitTransaction.setTitle(applicationMessageSource.getMessage("cdn.exit.transaction.link.label", null,
                LocaleContextHolder.getLocale()));

            exitTransactionList.add(exitTransaction);
            modelAndView.addObject(WETModelKey.EXIT_TRANSACTION.wetAttributeName(), exitTransactionList);
        } else {
            modelAndView.addObject(WETModelKey.EXIT_TRANSACTION.wetAttributeName(), "");
        }
    }

    /**
     * Sets the bread crumbs for the requested view.
     *
     * @param modelAndView the model and view
     * @param request the request
     */
    private void setBreadCrumbs(ModelAndView modelAndView, HttpServletRequest request) {

        // Get view breadCrumbs
        StringBuilder viewParameters = new StringBuilder();
        viewParameters.append(request.getRequestURI()).append(getRequestParameters(request).toString());
        breadcrumbsService.buildBreadCrumbs(modelAndView.getViewName(), viewParameters.toString());
        List<Object> viewBreadCrumbs = breadcrumbsService.getBreadCrumbList();
        String breadCrumbsKey = WETModelKey.BREADCRUMBS.wetAttributeName();

        ModelMap modelMap = modelAndView.getModelMap();
        Object breadCrumbList = modelMap.get(breadCrumbsKey);
        // if list is provided by the BreadCrumbs key in the ModelMap then add
        // to the BreadCrumbs list
        if (breadCrumbList != null && breadCrumbList instanceof BreadCrumbs) {
            BreadCrumbs breadCrumbs = (BreadCrumbs) breadCrumbList;
            // list should not be null but check for null anyway
            if (breadCrumbs.getList() != null && !breadCrumbs.getList().isEmpty()) {
                viewBreadCrumbs.addAll(breadCrumbs.getList());
            }
        }
        modelAndView.addObject(breadCrumbsKey, viewBreadCrumbs);
    }

    /**
     * Sets the side menu.
     *
     * @param modelAndView the new side menu
     */
    private void setSideMenu(ModelAndView modelAndView) {
        // Side Menu
        if (modelAndView.getModelMap().containsAttribute(WETModelKey.PAGE_SECTION_MENU.wetAttributeName())) {
            modelAndView.addObject(WETModelKey.SECTIONS.wetAttributeName(),
                modelAndView.getModelMap().get(WETModelKey.PAGE_SECTION_MENU.wetAttributeName()));
        } else {
//            modelAndView.addObject(WETModelKey.SECTIONS.wetAttributeName(), sideMenuConfig.getSectionMenuList());
            modelAndView.addObject(WETModelKey.SECTIONS.wetAttributeName(), sideMenuHandler.getSectionMenuList());
        }
    }

    /**
     * Sets the language request url.
     *
     * @param modelAndView the model and view
     * @param request the request
     */
    private void setLanguageRequestUrl(ModelAndView modelAndView, HttpServletRequest request) {

        StringBuilder requestParameters = getRequestParameters(request);

        setLanguageToggle(request, requestParameters);

        modelAndView.addObject(WETModelKey.LANGUAGE_URL.wetAttributeName(), requestParameters.toString());
    }

    /**
     * Gets the request parameters.
     *
     * @param request the request
     * @return the request parameters
     */
    private StringBuilder getRequestParameters(HttpServletRequest request) {
        StringBuilder requestParameters = new StringBuilder();

        Map<String, String[]> requestParams = request.getParameterMap();
        Set<Entry<String, String[]>> entrySet = requestParams.entrySet();

        int element = 0;
        for (Entry<String, String[]> entry : entrySet) {
            String key = entry.getKey(); // parameter name
            String[] value = entry.getValue(); // parameter value
            for (String val : value) {
                if (!"lang".equals(key) && !WETModelKey.LANGUAGE_URL.wetAttributeName().equals(key)) {
                    getParameterIdentifier(element == 0, requestParameters);
                    requestParameters.append(key).append("=").append(val);
                    element++;
                }
            }
        }
        return requestParameters;
    }

    /**
     * Gets the parameter identifier.
     *
     * @param firstParameter the first parameter
     * @param requestParameters the request parameters
     * @return the parameter identifier
     */
    private void getParameterIdentifier(boolean firstParameter, StringBuilder requestParameters) {
        if (firstParameter) {
            requestParameters.append("?");
        } else {
            requestParameters.append("&");
        }
    }

    /**
     * Sets the language toggle.
     *
     * @param request the request
     * @param langUrl the lang url
     */
    private void setLanguageToggle(HttpServletRequest request, StringBuilder langUrl) {
        if (langUrl.length() > 0) {
            langUrl.append("&");
        } else {
            langUrl.append("?");
        }
        langUrl.append("lang=");
        langUrl.append(applicationMessageSource.getMessage("locale.other", null, LocaleContextHolder.getLocale()));
    }

    /**
     * Sets the session timeout properties in the layout if provided.
     *
     * @param cdnSettings the cdn settings
     * @param modelAndView the model and view
     */
    private void setSessionTimeoutProperties(WETSettings cdnSettings, ModelAndView modelAndView) {
        if (cdnSettings.getSession().getTimeout().getEnabled()) {
            // Build data-wb-sessto
            StringBuilder dataWbSessionTo = new StringBuilder();
            dataWbSessionTo.append("<span class=\"wb-sessto\" data-wb-sessto='{");
            dataWbSessionTo.append("\"inactivity\": ").append(cdnSettings.getSession().getInactivity().getValue());
            dataWbSessionTo.append(",").append("\"reactionTime\": ")
                .append(cdnSettings.getSession().getReactiontime().getValue());
            dataWbSessionTo.append(",").append("\"sessionalive\":")
                .append(cdnSettings.getSession().getSessionalive().getValue());
            if (StringUtils.isNotBlank(cdnSettings.getSession().getRefreshcallbackurl())) {
                dataWbSessionTo.append(",").append("\"refreshCallbackUrl\": ").append("\"")
                    .append(cdnSettings.getSession().getRefreshcallbackurl()).append("\"");
            }
            if (null != cdnSettings.getSession().getRefreshonclick()) {
                dataWbSessionTo.append(",").append("\"refreshOnClick\": ")
                    .append(cdnSettings.getSession().getRefreshonclick());
            }
            if (null != cdnSettings.getSession().getRefreshlimit().getValue()) {
                dataWbSessionTo.append(",").append("\"refreshLimit\": ")
                    .append(cdnSettings.getSession().getRefreshlimit().getValue());
            }
            if (StringUtils.isNotBlank(cdnSettings.getSession().getMethod())) {
                dataWbSessionTo.append(",").append("\"method\":").append("\"")
                    .append(cdnSettings.getSession().getMethod()).append("\"");
            }
            if (StringUtils.isNotBlank(cdnSettings.getSession().getAdditionaldata())) {
                dataWbSessionTo.append(",").append("\"additionalData\":").append("\"")
                    .append(cdnSettings.getSession().getAdditionaldata()).append("\"");
            }
            dataWbSessionTo.append(",").append("\"logouturl\":").append("\"")
                .append(cdnSettings.getSession().getLogouturl()).append("\"");
            dataWbSessionTo.append("}'></span>");
            modelAndView.addObject(WETModelKey.SESSION_TIMEOUT.wetAttributeName(), dataWbSessionTo.toString());
        }
    }

    /**
     * Gets the leaving secure site warning.
     *
     * @param cdnSettings the cdn settings
     * @param modelAndView the model and view
     * @return the leaving secure site warning
     */
    private void getLeavingSecureSiteWarning(WETSettings cdnSettings, ModelAndView modelAndView) {

        ExitScript exitScript = new ExitScript();
        exitScript.setExitScriptEnabled(cdnSettings.getLeavingsecuresitewarning().getEnabled());
        exitScript.setExitUrl(cdnSettings.getLeavingsecuresitewarning().getRedirecturl());
        exitScript.setExitMessage((applicationMessageSource.getMessage("cdn.leavingsecuresitewarning.message", null,
            LocaleContextHolder.getLocale())));

        exitScript.setExitExcludedDomains(cdnSettings.getLeavingsecuresitewarning().getExcludeddomains());

        modelAndView.addObject(WETModelKey.LEAVING_SECURE_SITE.wetAttributeName(), exitScript);
    }

}