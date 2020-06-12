package cwcdh.pppp.facade;

import cwcdh.pppp.entity.DesignComponentForm;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author Dr M H B Ariyaratne, buddhika.ari@gmail.com
 */
@Stateless
public class ProjectSourceOfFundFacade extends AbstractFacade<DesignComponentForm> {

    @PersistenceContext(unitName = "hmisPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public ProjectSourceOfFundFacade() {
        super(DesignComponentForm.class);
    }

}
