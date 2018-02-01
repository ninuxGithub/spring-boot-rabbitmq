package com.example.demo.bean;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "user_table")
public class UserEntity implements Serializable {
	private static final long serialVersionUID = 5954252966400830349L;

	 @Id
	// @GeneratedValue(strategy = GenerationType.AUTO)
	@GenericGenerator(name = "system-uuid", strategy = "uuid") //
	@GeneratedValue(generator = "system-uuid") // 用generator属性指定要使用的策略生成器。
	private String id;
	private String username;
	private String name;
	private String phone;
	private String email;
	@Column(name = "create_time")
	private Date createTime;

	private boolean is_active;
	private String checkpass;

	public UserEntity() {
	}

	public UserEntity(String id, String username, String name, String phone, String email, Date create_time,
			boolean is_active, String checkpass) {
		this.id = id;
		this.username = username;
		this.name = name;
		this.phone = phone;
		this.email = email;
		this.createTime = create_time;
		this.is_active = is_active;
		this.checkpass = checkpass;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setIs_active(boolean is_active) {
		this.is_active = is_active;
	}

	public void setCheckpass(String checkpass) {
		this.checkpass = checkpass;
	}

	public String getId() {
		return id;
	}

	public boolean getIs_active() {
		return is_active;
	}

	public String getCheckpass() {
		return checkpass;
	}

	@Override
	public String toString() {
		return "UserEntity [id=" + id + ", username=" + username + ", name=" + name + ", phone=" + phone + ", email="
				+ email + ", createTime=" + createTime + ", is_active=" + is_active + ", checkpass=" + checkpass + "]";
	}

}
