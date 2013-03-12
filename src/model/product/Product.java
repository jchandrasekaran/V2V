package model.product;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.Valid;

import model.collectedsample.CollectedSample;
import model.collectedsample.CollectedSampleExists;
import model.compatibility.CompatibilityTest;
import model.modificationtracker.ModificationTracker;
import model.modificationtracker.RowModificationTracker;
import model.productmovement.ProductStatusChange;
import model.producttype.ProductType;
import model.producttype.ProductTypeExists;
import model.reasons.ProductStatusChangeReason;
import model.request.Request;
import model.user.User;
import model.util.BloodAbo;
import model.util.BloodRhd;

import org.hibernate.annotations.Index;

@Entity
public class Product implements ModificationTracker {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(nullable = false)
  private Long id;

  // A product may not have a corresponding sample. Some products may be
  // imported from another location. In such a case the corresponding collection
  // field is allowed to be null.
  @CollectedSampleExists
  @ManyToOne(optional=true, fetch=FetchType.LAZY)
  private CollectedSample collectedSample;

  // not all products are subdivided into small packs. Just store the
  // extra information about subdivided products in a separate table. 
  @OneToMany(mappedBy="parentProduct")
  private List<SubdividedProduct> subdividedProducts;

  @ProductTypeExists
  @ManyToOne
  private ProductType productType;

  @Temporal(TemporalType.TIMESTAMP)
  private Date createdOn;

  @Temporal(TemporalType.TIMESTAMP)
  @Index(name="product_expiresOn_index")
  private Date expiresOn;

  @Enumerated(EnumType.STRING)
  @Column(length=30)
  @Index(name="donor_bloodAbo_index")
  private BloodAbo bloodAbo;

  @Enumerated(EnumType.STRING)
  @Column(length=30)
  @Index(name="donor_bloodRhd_index")
  private BloodRhd bloodRhd;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(columnDefinition="DATETIME")
  private Date discardedOn;

  @ManyToOne
  private ProductStatusChangeReason discardReason;

  @ManyToOne
  private Request issuedTo;

  @Temporal(TemporalType.TIMESTAMP)
  private Date issuedOn;

  @Enumerated(EnumType.STRING)
  @Column(length=30)
  private ProductStatus status;

  @OneToMany(mappedBy="testedProduct", fetch=FetchType.LAZY)
  private List<CompatibilityTest> compatibilityTests;

  @OneToMany(mappedBy="product", fetch=FetchType.LAZY)
  private List<ProductStatusChange> statusChanges;

  @Lob
  private String notes;

  private Boolean isDeleted;

  @Valid
  private RowModificationTracker modificationTracker;

  public Product() {
    modificationTracker = new RowModificationTracker();
  }

  public void copy(Product product) {
    assert (this.getId().equals(product.getId()));
    this.collectedSample = product.collectedSample;
    this.productType = product.productType;
    this.createdOn = product.createdOn;
    this.expiresOn = product.expiresOn;
    this.notes = product.notes;
  }

  public Long getId() {
    return id;
  }

  public CollectedSample getCollectedSample() {
    return collectedSample;
  }

  public ProductType getProductType() {
    return productType;
  }

  public Date getExpiresOn() {
    return expiresOn;
  }

  public String getNotes() {
    return notes;
  }

  public Boolean getIsDeleted() {
    return isDeleted;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public void setCollectedSample(CollectedSample collectedSample) {
    this.collectedSample = collectedSample;
  }

  public void setProductType(ProductType productType) {
    this.productType = productType;
  }

  public void setExpiresOn(Date expiresOn) {
    this.expiresOn = expiresOn;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }

  public void setIsDeleted(Boolean isDeleted) {
    this.isDeleted = isDeleted;
  }

  public Date getCreatedOn() {
    return createdOn;
  }

  public void setCreatedOn(Date createdOn) {
    this.createdOn = createdOn;
  }

  public BloodAbo getBloodAbo() {
    return bloodAbo;
  }

  public void setBloodAbo(BloodAbo bloodAbo) {
    this.bloodAbo = bloodAbo;
  }

  public BloodRhd getBloodRhd() {
    return bloodRhd;
  }

  public void setBloodRhd(BloodRhd bloodRhd) {
    this.bloodRhd = bloodRhd;
  }

  public Date getLastUpdated() {
    return modificationTracker.getLastUpdated();
  }

  public Date getCreatedDate() {
    return modificationTracker.getCreatedDate();
  }

  public User getCreatedBy() {
    return modificationTracker.getCreatedBy();
  }

  public User getLastUpdatedBy() {
    return modificationTracker.getLastUpdatedBy();
  }

  public void setLastUpdated(Date lastUpdated) {
    modificationTracker.setLastUpdated(lastUpdated);
  }

  public void setCreatedDate(Date createdDate) {
    modificationTracker.setCreatedDate(createdDate);
  }

  public void setCreatedBy(User createdBy) {
    modificationTracker.setCreatedBy(createdBy);
  }

  public void setLastUpdatedBy(User lastUpdatedBy) {
    modificationTracker.setLastUpdatedBy(lastUpdatedBy);
  }

  public String getCollectionNumber() {
    if (collectedSample == null)
      return null;
    return collectedSample.getCollectionNumber();
  }

  public ProductStatus getStatus() {
    return status;
  }

  public void setStatus(ProductStatus status) {
    this.status = status;
  }

  public List<CompatibilityTest> getCompatibilityTests() {
    return compatibilityTests;
  }

  public void setCompatibilityTests(List<CompatibilityTest> compatibilityTests) {
    this.compatibilityTests = compatibilityTests;
  }

  public Date getDiscardedOn() {
    return discardedOn;
  }

  public void setDiscardedOn(Date discardedOn) {
    this.discardedOn = discardedOn;
  }

  public Request getIssuedTo() {
    return issuedTo;
  }

  public void setIssuedTo(Request issuedTo) {
    this.issuedTo = issuedTo;
  }

  public Date getIssuedOn() {
    return issuedOn;
  }

  public void setIssuedOn(Date issuedOn) {
    this.issuedOn = issuedOn;
  }

  public ProductStatusChangeReason getDiscardReason() {
    return discardReason;
  }

  public void setDiscardReason(ProductStatusChangeReason discardReason) {
    this.discardReason = discardReason;
  }

  public List<ProductStatusChange> getStatusChanges() {
    return statusChanges;
  }

  public void setStatusChanges(List<ProductStatusChange> statusChanges) {
    this.statusChanges = statusChanges;
  }

  public List<SubdividedProduct> getSubdividedProducts() {
    return subdividedProducts;
  }

  public void setSubdividedProducts(List<SubdividedProduct> subdividedProducts) {
    this.subdividedProducts = subdividedProducts;
  }
}
