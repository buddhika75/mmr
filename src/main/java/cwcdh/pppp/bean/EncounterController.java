package cwcdh.pppp.bean;

import cwcdh.pppp.entity.Implementation;
import cwcdh.pppp.bean.util.JsfUtil;
import cwcdh.pppp.bean.util.JsfUtil.PersistAction;
import cwcdh.pppp.facade.ImplementationFacade;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;
import cwcdh.pppp.entity.Solution;
import cwcdh.pppp.entity.Institution;
import cwcdh.pppp.enums.EncounterType;

@Named("encounterController")
@SessionScoped
public class EncounterController implements Serializable {

    @EJB
    private cwcdh.pppp.facade.ImplementationFacade ejbFacade;
    private List<Implementation> items = null;
    private Implementation selected;
    @Inject
    private WebUserController webUserController;

    public EncounterController() {
    }

  

    public String createClinicEnrollNumber(Institution clinic) {
        String j = "select count(e) from Implementation e "
                + " where e.institution=:ins "
                + " and e.encounterType=:ec "
                + " and e.createdAt>:d";
//        j = "select count(e) from Implementation e ";
        Map m = new HashMap();
        m.put("d", CommonController.startOfTheYear());
        m.put("ec", EncounterType.Clinic_Enroll);
        m.put("ins", clinic);
        Long c = getFacade().findLongByJpql(j, m);
        if (c == null) {
            c = 1l;
        } else {
            c += 1;
        }
        SimpleDateFormat format = new SimpleDateFormat("yy");
        String yy = format.format(new Date());
        return clinic.getCode() + "/" + yy + "/" + c;
    }

    public Long countOfEncounters(List<Institution> clinics, EncounterType ec) {
        if(clinics==null || clinics.isEmpty()){
            return 0l;
        }
        String j = "select count(e) from Implementation e "
                + " where e.retired=:ret "
                + " and e.institution in :ins "
                + " and e.encounterType=:ec "
                + " and e.createdAt>:d";
        Map m = new HashMap();
        m.put("d", CommonController.startOfTheYear());
        m.put("ec", ec);
        m.put("ret", false);
        m.put("ins", clinics);
        Long c = getFacade().findLongByJpql(j, m);
        if (c == null) {
            c = 0l;
        }
        return c;
    }
    
    public void retireSelectedEncounter(){
        if(selected==null){
            JsfUtil.addErrorMessage("Nothing Selected");
            return;
        }
        selected.setRetired(true);
        selected.setRetiredAt(new Date());
        selected.setRetiredBy(webUserController.getLoggedUser());
        JsfUtil.addSuccessMessage("Retired Successfully");
        selected=null;
    }

    public boolean clinicEnrolmentExists(Institution i, Solution c) {
        String j = "select e from Implementation e "
                + " where e.institution=:i "
                + " and e.solution=:c"
                + " and e.completed=:com"
                + " and e.encounterType=:et";
        Map m = new HashMap();
        m.put("i", i);
        m.put("c", c);
        m.put("com", false);
        m.put("et", EncounterType.Clinic_Enroll);
        Implementation e = getFacade().findFirstByJpql(j, m);
        if (e == null) {
            return false;
        }
        if (e.getCompleted()) {
            return false;
        } else {
            return true;
        }
    }

    public Implementation getSelected() {
        return selected;
    }

    public void setSelected(Implementation selected) {
        this.selected = selected;
    }

    protected void setEmbeddableKeys() {
    }

    protected void initializeEmbeddableKey() {
    }

    private ImplementationFacade getFacade() {
        return ejbFacade;
    }

    public Implementation prepareCreate() {
        selected = new Implementation();
        initializeEmbeddableKey();
        return selected;
    }

    public void save() {
        save(selected);
    }

    public void save(Implementation e) {
        if (e == null) {
            return;
        }
        if (e.getId() == null) {
            e.setCreatedAt(new Date());
            e.setCreatedBy(webUserController.getLoggedUser());
            getFacade().create(e);
        } else {
            e.setLastEditBy(webUserController.getLoggedUser());
            e.setLastEditeAt(new Date());
            getFacade().edit(e);
        }
    }

    public void create() {
        persist(PersistAction.CREATE, ResourceBundle.getBundle("/BundleClinical").getString("EncounterCreated"));
        if (!JsfUtil.isValidationFailed()) {
            items = null;    // Invalidate list of items to trigger re-query.
        }
    }

    public void update() {
        persist(PersistAction.UPDATE, ResourceBundle.getBundle("/BundleClinical").getString("EncounterUpdated"));
    }

    public void destroy() {
        persist(PersistAction.DELETE, ResourceBundle.getBundle("/BundleClinical").getString("EncounterDeleted"));
        if (!JsfUtil.isValidationFailed()) {
            selected = null; // Remove selection
            items = null;    // Invalidate list of items to trigger re-query.
        }
    }

    public List<Implementation> getItems(String jpql, Map m) {
        return getFacade().findByJpql(jpql, m);
    }

    private void persist(PersistAction persistAction, String successMessage) {
        if (selected != null) {
            setEmbeddableKeys();
            try {
                if (persistAction != PersistAction.DELETE) {
                    getFacade().edit(selected);
                } else {
                    getFacade().remove(selected);
                }
                JsfUtil.addSuccessMessage(successMessage);
            } catch (EJBException ex) {
                String msg = "";
                Throwable cause = ex.getCause();
                if (cause != null) {
                    msg = cause.getLocalizedMessage();
                }
                if (msg.length() > 0) {
                    JsfUtil.addErrorMessage(msg);
                } else {
                    JsfUtil.addErrorMessage(ex, ResourceBundle.getBundle("/BundleClinical").getString("PersistenceErrorOccured"));
                }
            } catch (Exception ex) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
                JsfUtil.addErrorMessage(ex, ResourceBundle.getBundle("/BundleClinical").getString("PersistenceErrorOccured"));
            }
        }
    }

    public Implementation getEncounter(java.lang.Long id) {
        return getFacade().find(id);
    }

    public List<Implementation> getItemsAvailableSelectMany() {
        return getFacade().findAll();
    }

    public List<Implementation> getItemsAvailableSelectOne() {
        return getFacade().findAll();
    }

    public WebUserController getWebUserController() {
        return webUserController;
    }

    public cwcdh.pppp.facade.ImplementationFacade getEjbFacade() {
        return ejbFacade;
    }

    @FacesConverter(forClass = Implementation.class)
    public static class EncounterControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            EncounterController controller = (EncounterController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "encounterController");
            return controller.getEncounter(getKey(value));
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
            if (object instanceof Implementation) {
                Implementation o = (Implementation) object;
                return getStringKey(o.getId());
            } else {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "object {0} is of type {1}; expected type: {2}", new Object[]{object, object.getClass().getName(), Implementation.class.getName()});
                return null;
            }
        }

    }

}
