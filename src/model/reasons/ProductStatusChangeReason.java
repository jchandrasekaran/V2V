package model.reasons;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import model.productmovement.ProductStatusChangeReasonCategory;

@Entity
public class ProductStatusChangeReason {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(nullable = false)
  private Long id;

  @Column(length=100)
  private String statusChangeReason;

  @Column(length=20)
  private ProductStatusChangeReasonCategory category; 

  public ProductStatusChangeReason() {
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getStatusChangeReason() {
    return statusChangeReason;
  }

  public void setStatusChangeReason(String statusChangeReason) {
    this.statusChangeReason = statusChangeReason;
  }

  public ProductStatusChangeReasonCategory getCategory() {
    return category;
  }

  public void setCategory(ProductStatusChangeReasonCategory category) {
    this.category = category;
  }
}
