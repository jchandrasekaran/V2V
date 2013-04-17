package model.producttype;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;

import org.hibernate.envers.Audited;

@Entity
@Audited
public class ProductType {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(nullable=false, insertable=false, updatable=false, columnDefinition="SMALLINT")
  private Integer id;

  @Column(length=30)
  private String productTypeShortName;

  @Column(length=50)
  private String productType;

  private Integer expiresAfter;

  @Enumerated(EnumType.STRING)
  @Column(length=30)
  private ProductTypeTimeUnits expiresAfterUnits;

  private Boolean hasBloodGroup;

  private Boolean canSubdivide;

  private Boolean canPool;

  @Lob
  private String description;
  
  private Boolean isDeleted;
  
  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  @Override
  public String toString() {
    return productType;
  }

  public Boolean getIsDeleted() {
    return isDeleted;
  }

  public void setIsDeleted(Boolean isDeleted) {
    this.isDeleted = isDeleted;
  }

  public String getProductType() {
    return productType;
  }

  public void setProductType(String productType) {
    this.productType = productType;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public boolean equals(ProductType pt) {
    return this.id == pt.id;
  }

  public String getProductTypeShortName() {
    return productTypeShortName;
  }

  public void setProductTypeShortName(String productTypeShortName) {
    this.productTypeShortName = productTypeShortName;
  }

  public ProductTypeTimeUnits getExpiresAfterUnits() {
    return expiresAfterUnits;
  }

  public void setExpiresAfterUnits(ProductTypeTimeUnits expiresAfterUnits) {
    this.expiresAfterUnits = expiresAfterUnits;
  }

  public Integer getExpiresAfter() {
    return expiresAfter;
  }

  public void setExpiresAfter(Integer expiresAfter) {
    this.expiresAfter = expiresAfter;
  }

  public Boolean getHasBloodGroup() {
    return hasBloodGroup;
  }

  public void setHasBloodGroup(Boolean hasBloodGroup) {
    this.hasBloodGroup = hasBloodGroup;
  }

  public Boolean getCanSubdivide() {
    return canSubdivide;
  }

  public void setCanSubdivide(Boolean canSubdivide) {
    this.canSubdivide = canSubdivide;
  }

  public Boolean getCanPool() {
    return canPool;
  }

  public void setCanPool(Boolean canPool) {
    this.canPool = canPool;
  }

  public int getExpiryIntervalMinutes() {
    int factor = 1;
    switch (expiresAfterUnits) {
    case HOURS: factor = 60;
                break;
    case DAYS:  factor = 24 * 60;
                break;
    case YEARS: factor = 365 * 24 * 60;
                break;
    }
    return expiresAfter * factor;
  }
}
