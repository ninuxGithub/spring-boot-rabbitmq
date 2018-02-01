package com.example.demo.bean;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "product")
public class Order implements Serializable {

	private static final long serialVersionUID = -1835021583969266196L;

	// @GenericGenerator(name = "system-uuid", strategy = "uuid")//
	// 声明一个策略通用生成器，name为"system-uuid",策略strategy为"uuid"。
	// @GeneratedValue(generator = "system-uuid")// 用generator属性指定要使用的策略生成器。
	// 适合String 类型的ID

	/**
	 * 订单ID
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long orderId;

	private String produceName;

	private Date productDate;

	private Date qualityGuaranteePeriod;

	private Integer stockAmount;

	private Double price;

	private String headPortrait;

	private String imageName;

	public Long getOrderId() {
		return orderId;
	}

	public void setOrderId(Long orderId) {
		this.orderId = orderId;
	}

	public String getProduceName() {
		return produceName;
	}

	public void setProduceName(String produceName) {
		this.produceName = produceName;
	}

	public Date getProductDate() {
		return productDate;
	}

	public void setProductDate(Date productDate) {
		this.productDate = productDate;
	}

	public Date getQualityGuaranteePeriod() {
		return qualityGuaranteePeriod;
	}

	public void setQualityGuaranteePeriod(Date qualityGuaranteePeriod) {
		this.qualityGuaranteePeriod = qualityGuaranteePeriod;
	}

	public Integer getStockAmount() {
		return stockAmount;
	}

	public void setStockAmount(Integer stockAmount) {
		this.stockAmount = stockAmount;
	}

	public Double getPrice() {
		return price;
	}

	public void setPrice(Double price) {
		this.price = price;
	}

	public String getHeadPortrait() {
		return headPortrait;
	}

	public void setHeadPortrait(String headPortrait) {
		this.headPortrait = headPortrait;
	}

	public String getImageName() {
		return imageName;
	}

	public void setImageName(String imageName) {
		this.imageName = imageName;
	}

}
