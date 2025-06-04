package task.manager.backend.entity;

public class UserEntity {
    private Integer id;
    private String username;
    private String password;
    
    // Default constructor
    public UserEntity() {
    }
    
    // Parameterized constructor
    public UserEntity(Integer id, String username, String password) {
        this.id = id;
        this.username = username;
        this.password = password;
    }
    
    // Getters and Setters
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
}
