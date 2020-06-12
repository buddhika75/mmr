package cwcdh.pppp.bean;

import cwcdh.pppp.EmailManagerEjb;
import cwcdh.pppp.bean.util.JsfUtil;
import cwcdh.pppp.entity.Message;
import cwcdh.pppp.enums.MessageType;
import cwcdh.pppp.facade.MessageFacade;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;
import javax.inject.Named;

@Named
@SessionScoped
public class MessageController implements Serializable {

    // <editor-fold defaultstate="collapsed" desc="EJBs">
    @EJB
    private MessageFacade facade;
    //</editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Controllers">
    @Inject
    private WebUserController webUserController;
    @Inject
    private CommonController commonController;
    //</editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Class Variables">
    @EJB
    private EmailManagerEjb emailManagerEjb;
    private List<Message> items = null;
    private Message selected;
    private Message subscribing;
    private String username;
    private String password;
    private String ptId;
    private String recipient;
    private boolean hasAccess = false;
    private boolean sentSuccessfully = false;
    private boolean sentFailed = false;
    private String errorMessage;
    private boolean completed;

    //</editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Navigator Methods">
    public String toSubmitProject() {
        selected = new Message();
        selected.setMessageType(MessageType.Project_Submission);
        return "/upload";
    }

    public String toContact() {
        selected = new Message();
        selected.setMessageType(MessageType.Contact_us);
        return "/contact";
    }

    public String toViewCaseStudyForUsers() {
        if (selected == null) {
            return "";
        }
        return "/messages/casestudy";
    }

    public String toViewCaseStudyForPublic() {
        if (selected == null) {
            return "";
        }
        return "/casestudy";
    }

    public String toViewCaseStudiesForUsers() {
        items = listMessages(MessageType.Cas_Study);
        return "/messages/casestudies";
    }

    public String toViewCaseStudiesForPublic() {
        items = listMessages(MessageType.Cas_Study);
        return "/casestudies";
    }

    public String toCreateNewCaseStudy() {
        selected = new Message();
        selected.setMessageType(MessageType.Cas_Study);
        selected.setCreatedAt(new Date());
        selected.setCreatedBy(webUserController.getLoggedUser());
        return "/messages/casestudy";
    }

    //</editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Methods">
    public void saveSelected() {
        saveSelected(selected);
    }
    
    public void reverseComplete() {
        prepareForRecordUpdate();
    }

    public void saveSelectedAndEmail() {
        completed=true;
        saveSelected(selected);
        String emailUserName = "arogyafirst";
        String emailPassword = "arogya123@";
        String emailHtmlMessage = emailMessageBody(selected);
        String emailSubject = "My Medical Record - Update Request";
        String emailSender = "arogyafirst@gmail.com";
        boolean sent = emailManagerEjb.sendEmail(
                emailUserName,
                emailPassword,
                emailSender,
                recipient,
                emailSubject,
                emailHtmlMessage,
                null);
        if (sent) {
            sentFailed = false;
            sentSuccessfully = true;
        } else {
            sentFailed = false;
            sentSuccessfully = true;
        }

    }

    public String emailMessageBody(Message r) {
        String b = "<!DOCTYPE html>"
                + "<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\" lang=\"en\">"
                + "<head>"
                + "<title>"
                + r.getMessageType()
                + "</title>"
                + "</head>"
                + "<body>";
        b += "<p>THere is a request for a patient report update.</p>";
        b += "<table>"
                + "<tr>"
                + "    <th>"
                + "        Patient ID"
                + "    </th>"
                + "    <th>"
                + "        " + ptId
                + "    </th>"
                + "</tr>"
                + "<tr>"
                + "    <th>"
                + "        Request Type"
                + "    </th>"
                + "    <th>"
                + "        " + selected.getItem().getName()
                + "    </th>"
                + "    </tr>"
                + "</tr>"
                + "<tr>"
                + "    <th>"
                + "        Request Details"
                + "    </th>"
                + "    <th>"
                + "        " + selected.getMessage()
                + "    </th>"
                + "    </tr>"
                + "</tr>"
                + "";

        b += "</table>";
        b += "</body></html>";
        return b;
    }

    public void prepareForRecordUpdate() {
        errorMessage = "";
        hasAccess = false;
        sentFailed = false;
        sentSuccessfully = false;
        completed=false;
        if (!username.equalsIgnoreCase("mmr")) {
            hasAccess = false;
            errorMessage = "Wrong Username.";
            return;
        }
        if (!password.equals("mmr123@")) {
            hasAccess = false;
            errorMessage = "Wrong Password.";
            return;
        }
        try {
            Long tptid = Long.parseLong(ptId);
        } catch (Exception e) {
            errorMessage = "Wrong Patient ID.";
            hasAccess = false;
            return;
        }
        if (recipient == null || recipient.trim().equals("")) {
            errorMessage = "No Receipient email";
            hasAccess = false;
        }
        hasAccess = true;
        selected = new Message();
        selected.setMessageType(MessageType.Record_Update_Request);

    }

    public void saveSelected(Message msg) {
        if (msg == null) {
            JsfUtil.addErrorMessage("Nothing selected to save.");
            return;
        }
        if (msg.getId() == null) {
            msg.setCreatedAt(new Date());
            msg.setCreatedBy(webUserController.getLoggedUser());
            getFacade().create(msg);
        } else {
            msg.setLastEditBy(webUserController.getLoggedUser());
            msg.setCreatedAt(new Date());
            getFacade().edit(msg);
        }
    }

    public String uploadProject() {
        saveSelected();
        JsfUtil.addSuccessMessage("Project Submitted.");
        return "/uploaded";
    }

    public String submitContact() {
        saveSelected();
        JsfUtil.addSuccessMessage("Submitted.");
        return "/contacted";
    }

    public String submitSubscribed() {
        saveSelected(subscribing);
        JsfUtil.addSuccessMessage("Submitted.");
        return "/subscribed";
    }

    public List<Message> listMessages(MessageType type) {
        String j = "select m "
                + " from Message m "
                + " where m.retired <>:ret "
                + " and m.messageType=:type"
                + " order by m.id desc";
        Map m = new HashMap();
        m.put("ret", true);
        m.put("type", type);
        return getFacade().findByJpql(j, m);
    }

    //</editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Getters and Setters">
    public MessageFacade getFacade() {
        return facade;
    }

    public CommonController getCommonController() {
        return commonController;
    }

    public List<Message> getItems() {
        return items;
    }

    public void setItems(List<Message> items) {
        this.items = items;
    }

    public Message getSelected() {
        return selected;
    }

    public void setSelected(Message selected) {
        this.selected = selected;
    }

    public Message getSubscribing() {
        return subscribing;
    }

    public void setSubscribing(Message subscribing) {
        if (subscribing == null) {
            subscribing = new Message();
            subscribing.setMessageType(MessageType.Email_Subscreption);
        }
        this.subscribing = subscribing;
    }

    public WebUserController getWebUserController() {
        return webUserController;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPtId() {
        return ptId;
    }

    public void setPtId(String ptId) {
        this.ptId = ptId;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public boolean isHasAccess() {
        return hasAccess;
    }

    public void setHasAccess(boolean hasAccess) {
        this.hasAccess = hasAccess;
    }

    public EmailManagerEjb getEmailManagerEjb() {
        return emailManagerEjb;
    }

    public void setEmailManagerEjb(EmailManagerEjb emailManagerEjb) {
        this.emailManagerEjb = emailManagerEjb;
    }

    public boolean isSentSuccessfully() {
        return sentSuccessfully;
    }

    public void setSentSuccessfully(boolean sentSuccessfully) {
        this.sentSuccessfully = sentSuccessfully;
    }

    public boolean isSentFailed() {
        return sentFailed;
    }

    public void setSentFailed(boolean sentFailed) {
        this.sentFailed = sentFailed;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
    
    
    

    //</editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Converters">
    @FacesConverter(forClass = Message.class)
    public static class MessageControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            MessageController controller = (MessageController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "messageController");
            return controller.getFacade().find(getKey(value));
        }

        java.lang.Long getKey(String value) {
            java.lang.Long key;
            key = Long.valueOf(value);
            return key;
        }

        String getStringKey(java.lang.Long value) {
            StringBuilder sb = new StringBuilder();
            sb.append(value);
            return sb.toString();
        }

        @Override
        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof Message) {
                Message o = (Message) object;
                return getStringKey(o.getId());
            } else {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "object {0} is of type {1}; expected type: {2}", new Object[]{object, object.getClass().getName(), Message.class.getName()});
                return null;
            }
        }

    }

    @FacesConverter(value = "messageConverter")
    public static class MessageConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            MessageController controller = (MessageController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "messageController");
            return controller.getFacade().find(getKey(value));
        }

        java.lang.Long getKey(String value) {
            java.lang.Long key;
            key = Long.valueOf(value);
            return key;
        }

        String getStringKey(java.lang.Long value) {
            StringBuilder sb = new StringBuilder();
            sb.append(value);
            return sb.toString();
        }

        @Override
        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof Message) {
                Message o = (Message) object;
                return getStringKey(o.getId());
            } else {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "object {0} is of type {1}; expected type: {2}", new Object[]{object, object.getClass().getName(), Message.class.getName()});
                return null;
            }
        }

    }

    // </editor-fold>
}
