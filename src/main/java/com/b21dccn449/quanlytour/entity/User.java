package com.b21dccn449.quanlytour.entity;

import com.b21dccn449.quanlytour.validation.OnCreate;
import com.b21dccn449.quanlytour.validation.OnUpdate;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Objects;


@Entity
@Table(name = "users",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "email"),
                @UniqueConstraint(columnNames = "username")
        })
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "user_type", discriminatorType = DiscriminatorType.STRING, length = 20)
public abstract class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "address_id", referencedColumnName = "id")
    @Valid
    private Address address;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "full_name_id", referencedColumnName = "id", nullable = false)
    @Valid
    private FullName fullName;

    private Integer age;

     @NotBlank(message = "Email không được để trống", groups = {OnCreate.class, OnUpdate.class})
     @Email(message = "Email không đúng định dạng", groups = {OnCreate.class, OnUpdate.class})
    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @NotBlank(message = "Mật khẩu không được để trống khi tạo mới.", groups = OnCreate.class)
    @Size(min = 8, message = "Mật khẩu khi tạo mới phải có ít nhất 8 ký tự.", groups = OnCreate.class)
    @Pattern(regexp = "^$|^.{8,}$", message = "Mật khẩu khi cập nhật phải để trống (để giữ nguyên) hoặc có ít nhất 8 ký tự.", groups = OnUpdate.class)
    @Column(nullable = false, length = 255)
    private String password;

    @Column(name = "registration_date")
    private LocalDateTime registrationDate;

    @NotBlank(message = "Số điện thoại không được để trống", groups = {OnCreate.class, OnUpdate.class})
    @Column(nullable = false, length = 20)
    private String tel;

    @NotBlank(message = "Tên đăng nhập không được để trống", groups = {OnCreate.class, OnUpdate.class})
    @Column(nullable = false, unique = true, length = 100)
    private String username;

    // --- Constructors ---
    public User() {
    }

    public User(Address address, FullName fullName, Integer age, String email, Boolean isActive, String password, LocalDateTime registrationDate, String tel, String username) {
        this.address = address;
        this.fullName = fullName;
        this.age = age;
        this.email = email;
        this.isActive = isActive;
        this.password = password;
        this.registrationDate = registrationDate;
        this.tel = tel;
        this.username = username;
    }


    public Long getId() {
        return id;
    }

    // THÊM SETTER CHO ID
    public void setId(Long id) {
        this.id = id;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public FullName getFullName() {
        return fullName;
    }

    public void setFullName(FullName fullName) {
        this.fullName = fullName;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @JsonProperty("isActive")
    public Boolean isActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDateTime registrationDate) {
        this.registrationDate = registrationDate;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @PrePersist
    protected void onCreate() {
        if (registrationDate == null) {
            registrationDate = LocalDateTime.now();
        }
        if (isActive == null) {
            isActive = true;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        if (id != null && user.id != null) {
            return id.equals(user.id);
        }
        return this == o; // Fallback to object identity if one or both IDs are null
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }

    @Override
    public String toString() {
        String fullNameStr = "null";
        if (fullName != null) {
            String lastName = fullName.getLastName() != null ? fullName.getLastName() : "";
            String firstName = fullName.getFirstName() != null ? fullName.getFirstName() : "";
            fullNameStr = lastName + " " + firstName;
        }
        return "User{" +
                "id=" + id +
                ", fullName=" + fullNameStr +
                ", age=" + age +
                ", email='" + email + '\'' +
                ", isActive=" + isActive +
                ", registrationDate=" + registrationDate +
                ", tel='" + tel + '\'' +
                ", username='" + username + '\'' +
                '}';
    }
}
