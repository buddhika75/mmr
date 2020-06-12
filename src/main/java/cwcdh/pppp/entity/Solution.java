package cwcdh.pppp.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.Temporal;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@XmlRootElement
public class Solution implements Serializable {

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "solution")
    @OrderBy("orderNo")
    private List<SiComponentItem> siComponentItems;

// <editor-fold defaultstate="collapsed" desc="Attributes">
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private static final long serialVersionUID = 1L;

    private String name;

    @Column(length = 45)
    private String sname;

    @Transient
    private String shortNameTmp;

    @Transient
    String solutionData;

    @Lob
    private String description;

    private long viewCount;

    @Deprecated
    @OneToOne(cascade = CascadeType.ALL)
    private Person person;

    private String phn;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    private byte[] baImage = new byte[1];
    private String fileName;
    private String fileType;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    private byte[] baImageIcon = new byte[1];
    private String fileNameIcon;
    private String fileTypeIcon;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    private byte[] baImageThumb = new byte[1];
    private String fileNameThumb;
    private String fileTypeThumb;

    private boolean featured;

    /*
    Create Properties
     */
    @ManyToOne
    private WebUser createdBy;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date createdAt;
    @ManyToOne
    private Institution createInstitution;
    /*
    Last Edit Properties
     */
    @ManyToOne
    private WebUser lastEditBy;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date lastEditeAt;
    /*
    Retire Reversal Properties
     */
    @ManyToOne
    private WebUser retiredReversedBy;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date retiredReversedAt;
    /*
    Retire Properties
     */
    private boolean retired;
    @ManyToOne
    private WebUser retiredBy;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date retiredAt;
    private String retireComments;

    // </editor-fold>   
// <editor-fold defaultstate="collapsed" desc="Overrides">
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {

        if (!(object instanceof Solution)) {
            return false;
        }
        Solution other = (Solution) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Solution{" + "phn=" + phn + '}';
    }

// </editor-fold>
// <editor-fold defaultstate="collapsed" desc="Getters & Setters">
    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Person getPerson() {
        if (person == null) {
            person = new Person();
        }
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public WebUser getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(WebUser createdBy) {
        this.createdBy = createdBy;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public WebUser getLastEditBy() {
        return lastEditBy;
    }

    public void setLastEditBy(WebUser lastEditBy) {
        this.lastEditBy = lastEditBy;
    }

    public Date getLastEditeAt() {
        return lastEditeAt;
    }

    public void setLastEditeAt(Date lastEditeAt) {
        this.lastEditeAt = lastEditeAt;
    }

    public boolean isRetired() {
        return retired;
    }

    public void setRetired(boolean retired) {
        this.retired = retired;
    }

    public WebUser getRetiredBy() {
        return retiredBy;
    }

    public void setRetiredBy(WebUser retiredBy) {
        this.retiredBy = retiredBy;
    }

    public Date getRetiredAt() {
        return retiredAt;
    }

    public void setRetiredAt(Date retiredAt) {
        this.retiredAt = retiredAt;
    }

    public String getRetireComments() {
        return retireComments;
    }

    public void setRetireComments(String retireComments) {
        this.retireComments = retireComments;
    }

    public WebUser getRetiredReversedBy() {
        return retiredReversedBy;
    }

    public void setRetiredReversedBy(WebUser retiredReversedBy) {
        this.retiredReversedBy = retiredReversedBy;
    }

    public Date getRetiredReversedAt() {
        return retiredReversedAt;
    }

    public void setRetiredReversedAt(Date retiredReversedAt) {
        this.retiredReversedAt = retiredReversedAt;
    }

    public String getPhn() {
        return phn;
    }

    public void setPhn(String phn) {
        this.phn = phn;
    }

// </editor-fold>
    public List<SiComponentItem> getSiComponentItems() {
        if (siComponentItems == null) {
            siComponentItems = new ArrayList<>();
        }
        return siComponentItems;
    }

    public void setSiComponentItems(List<SiComponentItem> siComponentItems) {
        this.siComponentItems = siComponentItems;
    }

    public Institution getCreateInstitution() {
        return createInstitution;
    }

    public void setCreateInstitution(Institution createInstitution) {
        this.createInstitution = createInstitution;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getViewCount() {
        return viewCount;
    }

    public void setViewCount(long viewCount) {
        this.viewCount = viewCount;
    }

    public byte[] getBaImage() {
        return baImage;
    }

    public void setBaImage(byte[] baImage) {
        this.baImage = baImage;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public boolean isFeatured() {
        return featured;
    }

    public void setFeatured(boolean featured) {
        this.featured = featured;
    }

    public byte[] getBaImageIcon() {
        return baImageIcon;
    }

    public void setBaImageIcon(byte[] baImageIcon) {
        this.baImageIcon = baImageIcon;
    }

    public String getFileNameIcon() {
        return fileNameIcon;
    }

    public void setFileNameIcon(String fileNameIcon) {
        this.fileNameIcon = fileNameIcon;
    }

    public String getFileTypeIcon() {
        return fileTypeIcon;
    }

    public void setFileTypeIcon(String fileTypeIcon) {
        this.fileTypeIcon = fileTypeIcon;
    }

    public byte[] getBaImageThumb() {
        return baImageThumb;
    }

    public void setBaImageThumb(byte[] baImageThumb) {
        this.baImageThumb = baImageThumb;
    }

    public String getFileNameThumb() {
        return fileNameThumb;
    }

    public void setFileNameThumb(String fileNameThumb) {
        this.fileNameThumb = fileNameThumb;
    }

    public String getFileTypeThumb() {
        return fileTypeThumb;
    }

    public void setFileTypeThumb(String fileTypeThumb) {
        this.fileTypeThumb = fileTypeThumb;
    }

    public String getShortNameTmp() {
        if (name == null) {
            return "";
        }
        String tn = name + "                                              ";
        return tn.substring(0, 35);
    }

    public String getSolutionData() {
        return solutionData;
    }

    public String findSlutionData(String code) {
        solutionData = "";
        for (SiComponentItem sici : getSiComponentItems()) {
            if (sici.getItem().getCode().equals(code) && !sici.isRetired()) {
                solutionData += sici.getValueAsString() + " ";
            };
        }
        if (solutionData == null) {
            solutionData = "";
        }
        solutionData = solutionData.trim();
        return solutionData;
    }

    public String getSname() {
        if (sname == null || sname.trim().equals("")) {
            String tm;
            if (name == null) {
                tm = "                                                                     ";
            } else {
                tm = name + "                                                                     ";
            }
            sname = tm.substring(0, 26);
        }
        if (sname.length() > 26) {
            sname = sname.substring(0, 26);
        }
        return sname;
    }

    public void setSname(String sname) {
        this.sname = sname;
    }

}
