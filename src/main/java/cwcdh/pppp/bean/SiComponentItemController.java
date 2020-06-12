package cwcdh.pppp.bean;

import cwcdh.pppp.entity.SiComponentItem;
import cwcdh.pppp.bean.util.JsfUtil;
import cwcdh.pppp.bean.util.JsfUtil.PersistAction;
import cwcdh.pppp.facade.ClientEncounterComponentItemFacade;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;
import cwcdh.pppp.entity.SiComponentForm;
import cwcdh.pppp.entity.SiFormSet;
import cwcdh.pppp.pojcs.Replaceable;
import javax.script.ScriptEngineManager;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import cwcdh.pppp.entity.Solution;
import cwcdh.pppp.entity.Component;
import cwcdh.pppp.entity.Person;

@Named
@SessionScoped
public class SiComponentItemController implements Serializable {

    @EJB
    private cwcdh.pppp.facade.ClientEncounterComponentItemFacade ejbFacade;
    @Inject
    private WebUserController webUserController;

    @Inject
    private CommonController commonController;
    @Inject
    private ItemController itemController;

    private List<SiComponentItem> items = null;
    private List<SiComponentItem> formsetItems = null;
    private SiComponentItem selected;

    private Long searchId;

    public void searchById() {
        //System.out.println("searchById");
        //System.out.println("searchId = " + searchId);
        selected = getFacade().find(searchId);
    }

    public void findClientEncounterComponentItemOfAFormset(SiFormSet fs) {
        // //System.out.println("findSolutionItems = ");
        // //System.out.println("fs = " + fs);
        String j = "select f from SiComponentItem f "
                + " where f.retired=false "
                + " and f.parentComponent.parentComponent=:p "
                + " order by f.orderNo";
        Map m = new HashMap();
        m.put("p", fs);
        formsetItems = getFacade().findByJpql(j, m);
    }
    
    public List<SiComponentItem> findSolutionItems(Solution item) {
        String j = "select f from SiComponentItem f "
                + " where f.retired=false "
                + " and f.solution=:p "
                + " order by f.orderNo";
        Map m = new HashMap();
        m.put("p", item);

        List<SiComponentItem> t = getFacade().findByJpql(j, m);
    
        if (t == null) {
            t = new ArrayList<>();
        }
        Double td = 0.0;
        for(SiComponentItem ci:t){
            ci.setOrderNo(td);
            td++;
            getFacade().edit(ci);
        }
        return t;
    }

    public List<SiComponentItem> findSolutionItems(SiComponentForm fs) {
        String j = "select f from SiComponentItem f "
                + " where f.retired=false "
                + " and f.parentComponent=:p "
                + " order by f.orderNo";
        Map m = new HashMap();
        m.put("p", fs);
        List<SiComponentItem> t = getFacade().findByJpql(j, m);
        if (t == null) {
            t = new ArrayList<>();
        }
        return t;
    }

    public SiComponentItemController() {
    }

    public SiComponentItem getSelected() {
        return selected;
    }

    public void setSelected(SiComponentItem selected) {
        this.selected = selected;
    }

    protected void setEmbeddableKeys() {
    }

    protected void initializeEmbeddableKey() {
    }

    private ClientEncounterComponentItemFacade getFacade() {
        return ejbFacade;
    }

    public SiComponentItem prepareCreate() {
        selected = new SiComponentItem();
        initializeEmbeddableKey();
        return selected;
    }

    public void save() {
        save(selected);
    }

    public void calculate(SiComponentItem i) {
        
        if (i == null) {
            return;
        }

        if (i.getCalculationScript() == null || i.getCalculationScript().trim().equals("")) {
            System.out.println("Calculation script is null. Quitting");
            return;
        }
        if (i.getParentComponent() == null || i.getParentComponent().getParentComponent() == null) {
            System.out.println("Parent Component is null. Quitting");
            return;
        }
        if (!(i.getParentComponent().getParentComponent() instanceof SiFormSet)) {
            System.out.println("Formset is null. Quitting");
            return;
        }

        if (i.getCalculationScript().trim().equalsIgnoreCase("#{client_current_age_in_years}")) {
            SiFormSet s = (SiFormSet) i.getParentComponent().getParentComponent();
            Person p = s.getEncounter().getClient().getPerson();
            i.setShortTextValue(p.getAgeYears() + "");
            i.setRealNumberValue(Double.valueOf(p.getAgeYears()));
            i.setIntegerNumberValue(p.getAgeYears());
            getFacade().edit(i);
            System.out.println("Age is calculated. Quitting");
            return;
        } else {
        }

        List<Replaceable> replacingBlocks = findReplaceblesInCalculationString(i.getCalculationScript());
        System.out.println("replacingBlocks Size = " + replacingBlocks.size());

        for (Replaceable r : replacingBlocks) {
           
            if (r.getPef().equalsIgnoreCase("f")) {
                if (r.getSm().equalsIgnoreCase("s")) {
                    r.setClientEncounterComponentItem(findFormsetValue(i, r.getVariableCode()));
                } else {
                    r.setClientEncounterComponentItem(findFormsetValue(i, r.getVariableCode(), r.getValueCode()));
                }
            } else if (r.getPef().equalsIgnoreCase("p")) {
                r.setClientEncounterComponentItem(findClientValue(i, r.getVariableCode()));
            }
            if (r.getClientEncounterComponentItem() != null) {
                SiComponentItem c = r.getClientEncounterComponentItem();
                
                if(c.getItem()==null){
                    System.out.println("Item is Null. ID of SiComponentItem is " + c.getId());
                    continue;
                }else{
                    if(c.getItem().getDataType()==null){
                        System.out.println("Items data type is null.");
                        continue;
                    }
                }
                
                System.out.println("c.getBooleanValue() = " + c.getBooleanValue());
                System.out.println("c.getRealNumberValue() = " + c.getRealNumberValue());
                System.out.println("c.getLongNumberValue() = " + c.getLongNumberValue());
                System.out.println("c.getItemValue().getItemValue() = " + c.getItemValue());
                System.out.println("c.getItem().getDataType() = " + c.getItem().getDataType());

                switch (c.getItem().getDataType()) {
                    case Short_Text:
                        if (c.getShortTextValue() != null) {
                            r.setSelectedValue(c.getShortTextValue());
                        }
                        break;
                    case Boolean:
                        if (c.getBooleanValue() != null) {
                            r.setSelectedValue(c.getBooleanValue().toString());
                        }
                        break;
                    case Real_Number:
                        if (c.getRealNumberValue() != null) {
                            r.setSelectedValue(c.getRealNumberValue().toString());
                        }
                        break;
                    case Integer_Number:
                        if (c.getIntegerNumberValue() != null) {
                            r.setSelectedValue(c.getIntegerNumberValue().toString());
                        }
                        break;
                    case Item_Reference:
                        if (c.getItemValue() != null) {
                            r.setSelectedValue(c.getItemValue().getCode());
                        }
                        break;
                }
                System.out.println("Found Value is r.getSelectedValue() = " + r.getSelectedValue());
            } else {
                r.setSelectedValue(r.getDefaultValue());
                System.out.println("No Value Found - Default Value is r.getSelectedValue() = " + r.getSelectedValue());
            }
        }

        String javaStringToEvaluate = addTemplateToReport(i.getCalculationScript().trim(), replacingBlocks);
        System.out.println("javaString To Evaluate = \n" + javaStringToEvaluate);

        String result = evaluateScript(javaStringToEvaluate);

        System.out.println("Assigning Found Calculation Results");
        System.out.println("i.getId() = " + i.getId());
        System.out.println("result = " + result);
        System.out.println("i.getSelectionDataType() = " + i.getSelectionDataType());

        if (null == i.getItem().getDataType()) {
            i.setShortTextValue(result);
        } else {
            switch (i.getItem().getDataType()) {
                case Real_Number:
                    i.setRealNumberValue(commonController.getDoubleValue(result));
                    System.out.println("i.getRealNumberValue() = " + i.getRealNumberValue());
                    getFacade().edit(i);
                    break;
                case Integer_Number:
                    i.setIntegerNumberValue(commonController.getIntegerValue(result));
                    System.out.println("i.getIntegerNumberValue() = " + i.getIntegerNumberValue());
                    getFacade().edit(i);
                    break;
                case Short_Text:
                    i.setShortTextValue(result);
                    System.out.println("i.getShortTextValue() = " + i.getShortTextValue());
                    getFacade().edit(i);
                    break;
                case Long_Text:
                    System.out.println("i.getLongTextValue() = " + i.getLongTextValue());
                    i.setLongTextValue(result);
                    getFacade().edit(i);
                    break;
                default:
                    break;
            }
            getFacade().edit(i);
        }

        System.out.println("javaStringToEvaluate = " + javaStringToEvaluate);
    }

    public String evaluateScript(String script) {
        ScriptEngineManager mgr = new ScriptEngineManager();
        ScriptEngine engine = mgr.getEngineByName("JavaScript");
        try {
            return engine.eval(script) + "";
        } catch (ScriptException ex) {
            //System.out.println("ex = " + ex.getMessage());
            return null;
        }
    }

    public SiComponentItem findFormsetValue(SiComponentItem i, String code) {
        if (i == null) {
            return null;
        }
        if (i.getParentComponent() == null) {
            return null;
        }
        if (i.getParentComponent().getParentComponent() == null) {
            return null;
        }
        if (code == null) {
            return null;
        }
        if (code.trim().equals("")) {
            return null;
        }
        String j = "select i from SiComponentItem i where i.retired=:r "
                + " and i.parentComponent.parentComponent.id=:pc "
                + " and i.item.code=:c";
        Map m = new HashMap();
        m.put("pc", i.getParentComponent().getParentComponent().getId());
        m.put("r", false);
        m.put("c", code);
        //System.out.println("m = " + m);
        //System.out.println("j = " + j);
        SiComponentItem temc = getFacade().findFirstByJpql(j, m);
        if (temc == null) {
            //System.out.println("Single Match NOT Found ");
        } else {
            //System.out.println("Single Match Found. ID is " + temc.getId());
        }
        return temc;
    }

    public SiComponentItem findFormsetValue(SiComponentItem i, String variableCode, String valueCode) {
        //System.out.println("findFormsetValue");
        //System.out.println("i = " + i);
        //System.out.println("valueCode = " + valueCode);
        //System.out.println("variableCode = " + variableCode);
        if (i == null) {
            return null;
        }
        if (i.getParentComponent() == null) {
            return null;
        }
        if (i.getParentComponent().getParentComponent() == null) {
            return null;
        }
        if (variableCode == null) {
            return null;
        }
        if (variableCode.trim().equals("")) {
            return null;
        }
        String j = "select i from SiComponentItem i where i.retired=:r "
                + " and i.parentComponent.parentComponent.id=:pc "
                + " and i.item.code=:c "
                + " and i.itemValue.code=:vc";
        Map m = new HashMap();
        m.put("pc", i.getParentComponent().getParentComponent().getId());
        m.put("c", variableCode.toLowerCase());
        m.put("vc", valueCode.toLowerCase());
        m.put("r", false);
        SiComponentItem ti = getFacade().findFirstByJpql(j, m);
        if (ti == null) {
            //System.out.println("Multiple Match NOT Found.");
        } else {
            //System.out.println("Multiple Match Found. ID is " + ti.getId());
        }
        return ti;
    }

    public String addTemplateToReport(String calculationScript, List<Replaceable> selectables) {
        for (Replaceable s : selectables) {
            String patternStart = "#{";
            String patternEnd = "}";
            String toBeReplaced;
            toBeReplaced = patternStart + s.getFullText() + patternEnd;
            calculationScript = calculationScript.replace(toBeReplaced, s.getSelectedValue());
        }
        return calculationScript;
    }

    public List<Replaceable> findReplaceblesInCalculationString(String text) {
        // //System.out.println("findReplaceblesInCalculationString");
        // //System.out.println("text = " + text);

        List<Replaceable> ss = new ArrayList<>();

        String patternStart = "#{";
        String patternEnd = "}";
        String regexString = Pattern.quote(patternStart) + "(.*?)" + Pattern.quote(patternEnd);

        Pattern p = Pattern.compile(regexString);
        Matcher m = p.matcher(text);

        while (m.find()) {
            String block = m.group(1);
            if (!block.trim().equals("")) {
                Replaceable s = new Replaceable();
                s.setFullText(block);
                if (block.contains("|")) {
                    String[] blockParts = block.split("\\|");
                    for (int i = 0; i < blockParts.length; i++) {
                        switch (i) {
                            case 0:
                                s.setPef(blockParts[0]);
                                break;
                            case 1:
                                s.setFl(blockParts[1]);
                                break;
                            case 2:
                                s.setSm(blockParts[2]);
                                break;
                            case 3:
                                s.setVariableCode(blockParts[3]);
                                break;
                            case 4:
                                s.setValueCode(blockParts[4]);
                                break;
                            case 5:
                                s.setDefaultValue(blockParts[5]);
                                break;
                            default:
                                break;
                        }
                    }
                    s.setInputText(false);
                    s.setFormulaEvaluation(true);
                } else {
                    return ss;
                }
                ss.add(s);
            }
        }

        return ss;

    }

    public void save(SiComponentItem i) {
        // //System.out.println("save");
        // //System.out.println("i = " + i);
        if (i == null) {
            return;
        }
        // //System.out.println("i.getId() = " + i.getId());
        // //System.out.println("i.getShortTextValue() = " + i.getShortTextValue());
        if (i.getId() == null) {
            i.setCreatedAt(new Date());
            i.setCreatedBy(webUserController.getLoggedUser());
            getFacade().create(i);
        } else {
            i.setLastEditBy(webUserController.getLoggedUser());
            i.setLastEditeAt(new Date());
            getFacade().edit(i);
        }
    }

    public void addAnother(SiComponentItem i) {
        // //System.out.println("addAnother");
        // //System.out.println("i = " + i);
        if (i == null) {
            return;
        }
        if (i.getId() == null) {
            i.setCreatedAt(new Date());
            i.setCreatedBy(webUserController.getLoggedUser());
            getFacade().create(i);
        } else {
            i.setLastEditBy(webUserController.getLoggedUser());
            i.setLastEditeAt(new Date());
            getFacade().edit(i);
        }

        Long temporaryFormSetStartTimeInLong;
        Long temporaryCurrentTimeInLong;

        if (i.getParentComponent() == null && i.getParentComponent().getParentComponent() == null && i.getParentComponent().getParentComponent().getCreatedAt() == null) {
            temporaryFormSetStartTimeInLong = i.getParentComponent().getParentComponent().getCreatedAt().getTime();
        } else {
            temporaryFormSetStartTimeInLong = (new Date()).getTime();
        }

        SiComponentItem ci = new SiComponentItem();

        ci.setParentComponent(i.getParentComponent());
        ci.setReferenceComponent(i.getReferenceComponent());

        ci.setEncounter(i.getEncounter());
        ci.setInstitution(i.getInstitution());

        ci.setItem(i.getItem());
        ci.setDescreption(i.getDescreption());

        ci.setRequired(i.isRequired());
        ci.setRequiredErrorMessage(i.getRequiredErrorMessage());
        ci.setRegexValidationString(i.getRegexValidationString());
        ci.setRegexValidationFailedMessage(i.getRegexValidationFailedMessage());

        ci.setName(i.getName());
        ci.setRenderType(i.getRenderType());
        ci.setMimeType(i.getMimeType());
//        ci.setSelectionDataType(i.getSelectionDataType());
        ci.setTopPercent(i.getTopPercent());
        ci.setLeftPercent(i.getLeftPercent());
        ci.setWidthPercent(i.getWidthPercent());
        ci.setHeightPercent(i.getHeightPercent());
        ci.setCategoryOfAvailableItems(i.getCategoryOfAvailableItems());

        ci.setDataPopulationStrategy(i.getDataPopulationStrategy());
        ci.setDataModificationStrategy(i.getDataModificationStrategy());
        ci.setDataCompletionStrategy(i.getDataCompletionStrategy());
        ci.setIntHtmlColor(i.getIntHtmlColor());
        ci.setHexHtmlColour(i.getHexHtmlColour());

        ci.setForegroundColour(i.getForegroundColour());
        ci.setBackgroundColour(i.getBackgroundColour());
        ci.setBorderColour(i.getBorderColour());

        ci.setCalculateOnFocus(i.isCalculateOnFocus());
        ci.setCalculationScript(i.getCalculationScript());

        ci.setCalculateButton(i.isCalculateButton());
        ci.setCalculationScriptForColour(i.getCalculationScriptForColour());
        ci.setDisplayDetailsBox(i.isDisplayDetailsBox());
        ci.setDiscreptionAsAToolTip(i.isDiscreptionAsAToolTip());
        ci.setDiscreptionAsASideLabel(i.isDiscreptionAsASideLabel());
        ci.setCalculationScriptForBackgroundColour(i.getCalculationScriptForBackgroundColour());
        ci.setMultipleEntiesPerForm(i.isMultipleEntiesPerForm());

        // //System.out.println("getParentComponent = " + ci.getParentComponent());
        // //System.out.println("getReferenceComponent = " + ci.getReferenceComponent());
        ci.setParentComponent(i.getParentComponent());
        ci.setReferenceComponent(i.getReferenceComponent());
        // //System.out.println("ni = " + ci);
        // //System.out.println("ni = " + ci.getBackgroundColour());
        // //System.out.println("ni = " + ci.getDescreption());
        // //System.out.println("ni = " + ci.getAreaValue());
        // //System.out.println("ni = " + ci.getRealNumberValue());
        // //System.out.println("ni = " + ci.getLongNumberValue());
        // //System.out.println("ni = " + ci.getIntegerNumberValue());
        // //System.out.println("ni = " + ci.getItemValue());
        // //System.out.println("ni = " + ci.getPrescriptionValue());
        // //System.out.println("ni = " + ci.getInstitutionValue());
        // //System.out.println("getParentComponent = " + ci.getParentComponent());
        // //System.out.println("getReferenceComponent = " + ci.getReferenceComponent());

        temporaryCurrentTimeInLong = (new Date()).getTime();

        ci.setOrderNo(i.getOrderNo() + ((temporaryCurrentTimeInLong - temporaryFormSetStartTimeInLong) / temporaryFormSetStartTimeInLong));

        ci.setCreatedAt(new Date());
        ci.setCreatedBy(webUserController.getLoggedUser());

        getFacade().create(ci);
        // //System.out.println("ni = " + ci);
        // //System.out.println("ni = " + ci.getId());

        findSolutionItems((SiComponentForm) i.getParentComponent());

    }

    public void create() {
        persist(PersistAction.CREATE, ResourceBundle.getBundle("/BundleClinical").getString("ClientEncounterComponentItemCreated"));
        if (!JsfUtil.isValidationFailed()) {
            items = null;    // Invalidate list of items to trigger re-query.
        }
    }

    public void update() {
        persist(PersistAction.UPDATE, ResourceBundle.getBundle("/BundleClinical").getString("ClientEncounterComponentItemUpdated"));
    }

    public void destroy() {
        persist(PersistAction.DELETE, ResourceBundle.getBundle("/BundleClinical").getString("ClientEncounterComponentItemDeleted"));
        if (!JsfUtil.isValidationFailed()) {
            selected = null; // Remove selection
            items = null;    // Invalidate list of items to trigger re-query.
        }
    }

    public List<SiComponentItem> getItems() {
        if (items == null) {
            items = getFacade().findAll();
        }
        return items;
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

    public SiComponentItem getClientEncounterComponentItem(java.lang.Long id) {
        return getFacade().find(id);
    }

    public List<SiComponentItem> getItemsAvailableSelectMany() {
        return getFacade().findAll();
    }

    public List<SiComponentItem> getItemsAvailableSelectOne() {
        return getFacade().findAll();
    }

    public WebUserController getWebUserController() {
        return webUserController;
    }

    public cwcdh.pppp.facade.ClientEncounterComponentItemFacade getEjbFacade() {
        return ejbFacade;
    }

    public CommonController getCommonController() {
        return commonController;
    }

    private SiComponentItem findClientValue(SiComponentItem i, String code) {
        System.out.println("findClientValue");
        System.out.println("code = " + code);
        System.out.println("i = " + i);
        if (i == null) {
            return null;
        }
        if (i.getParentComponent() == null) {
            return null;
        }
        if (i.getParentComponent().getParentComponent() == null) {
            return null;
        }
        Component c = i.getParentComponent().getParentComponent();
        SiFormSet s;
        if (c instanceof SiFormSet) {
            s = (SiFormSet) c;
        } else {
            return null;
        }
        Solution solution;
        if (s.getEncounter() == null && s.getClient() == null) {
            return null;
        } else if (s.getClient() != null) {
            solution = s.getClient();
        } else if (s.getEncounter().getClient() != null) {
            solution = s.getEncounter().getClient();
        } else {
            return null;
        }

        if (code == null) {
            return null;
        }
        if (code.trim().equals("")) {
            return null;
        }

        String j = "select i from SiComponentItem i where i.retired=false "
                + " and i.solution=:solution "
                + " and lower(i.item.code)=:c";
        Map m = new HashMap();
        m.put("solution", solution);
        m.put("c", code.toLowerCase());
        System.out.println("m = " + m);
        System.out.println("j = " + j);
        SiComponentItem fountVal = getFacade().findFirstByJpql(j, m);
        if (fountVal != null) {
            if (code.equalsIgnoreCase("client_current_age_in_years")) {
                Person p = solution.getPerson();
                fountVal.setLastEditeAt(new Date());
                fountVal.setLastEditBy(webUserController.getLoggedUser());
                fountVal.setShortTextValue(p.getAgeYears() + "");
                fountVal.setRealNumberValue(Double.valueOf(p.getAgeYears()));
                fountVal.setIntegerNumberValue(p.getAgeYears());
                getFacade().edit(fountVal);
                System.out.println("Patient Age Item Updated. ID is " + fountVal);
            }
            System.out.println("Patient Value Found. ID is " + fountVal);
        } else {
            if (code.equalsIgnoreCase("client_current_age_in_years")) {
                SiComponentItem ageItem = new SiComponentItem();
                ageItem.setClient(solution);
                ageItem.setCreatedAt(new Date());
                ageItem.setCreatedBy(webUserController.getLoggedUser());
                ageItem.setItem(itemController.findItemByCode("client_current_age_in_years"));
                Person p = solution.getPerson();
                ageItem.setShortTextValue(p.getAgeYears() + "");
                ageItem.setRealNumberValue(Double.valueOf(p.getAgeYears()));
                ageItem.setIntegerNumberValue(p.getAgeYears());
                getFacade().create(ageItem);
                fountVal = ageItem;
                System.out.println("Patient age created. ID is " + fountVal);
            }
            
        }
        return fountVal;

    }

    public Long getSearchId() {
        return searchId;
    }

    public void setSearchId(Long searchId) {
        this.searchId = searchId;
    }

    public List<SiComponentItem> getFormsetItems() {
        return formsetItems;
    }

    public void setFormsetItems(List<SiComponentItem> formsetItems) {
        this.formsetItems = formsetItems;
    }

    public ItemController getItemController() {
        return itemController;
    }
    
    

    @FacesConverter(forClass = SiComponentItem.class)
    public static class ClientEncounterComponentItemControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            SiComponentItemController controller = (SiComponentItemController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "clientEncounterComponentItemController");
            return controller.getClientEncounterComponentItem(getKey(value));
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
            if (object instanceof SiComponentItem) {
                SiComponentItem o = (SiComponentItem) object;
                return getStringKey(o.getId());
            } else {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "object {0} is of type {1}; expected type: {2}", new Object[]{object, object.getClass().getName(), SiComponentItem.class.getName()});
                return null;
            }
        }

    }

}
