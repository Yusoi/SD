package Client;

public class User {

    private String email;
    private String password;
    private Float funds;

    public User(String em, String pass, Float fund){
        this.email = em;
        this.password = pass;
        this.funds = fund;

    }

    public Float getFunds() {
        return funds;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setFunds(Float funds) {
        this.funds = funds;
    }

    public void setPassword(String password) {
        this.password = password;
    }



}
