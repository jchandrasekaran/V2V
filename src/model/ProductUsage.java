package model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.hibernate.annotations.Type;

@Entity
public class ProductUsage {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long usageId;
    private String productNumber;
    private Date dateUsed;
    private String hospital;
    private String ward;
    private String useIndication;
    private String comment;

    @Type(type = "org.hibernate.type.NumericBooleanType")
    private Boolean isDeleted;

    public ProductUsage() {
    }

    public ProductUsage(String productNumber, Date dateUsed, String hospital, String ward, String useIndication, String comment, Boolean deleted) {
        this.productNumber = productNumber;
        this.dateUsed = dateUsed;
        this.hospital = hospital;
        this.ward = ward;
        isDeleted = deleted;
        this.useIndication = useIndication;
        this.comment = comment;
    }

    public void copy(ProductUsage productUsage) {
        this.productNumber = productUsage.productNumber;
        this.dateUsed = productUsage.dateUsed;
        this.hospital = productUsage.hospital;
        this.ward = productUsage.ward;
        isDeleted = productUsage.isDeleted;
        this.useIndication = productUsage.useIndication;
        this.comment = productUsage.comment;
    }

    public Long getUsageId() {
        return usageId;
    }

    public String getProductNumber() {
        return productNumber;
    }

    public Date getDateUsed() {
        return dateUsed;
    }

    public String getDateUsedString() {
        DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
        return formatter.format(dateUsed);
    }

    public String getHospital() {
        return hospital;
    }

    public String getWard() {
        return ward;
    }

    public Boolean getDeleted() {
        return isDeleted;
    }

    public String getUseIndication() {
        return useIndication;
    }

    public String getComment() {
        return comment;
    }

    public void setProductNumber(String productNumber) {
        this.productNumber = productNumber;
    }

    public void setDateUsed(Date dateUsed) {
        this.dateUsed = dateUsed;
    }

    public void setHospital(String hospital) {
        this.hospital = hospital;
    }

    public void setWard(String ward) {
        this.ward = ward;
    }

    public void setDeleted(Boolean deleted) {
        isDeleted = deleted;
    }

    public void setUseIndication(String useIndication) {
        this.useIndication = useIndication;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
