public class Member {
    private String name;
    private String phone;
    private String altPhone;
    private String address;

    public Member(String name, String phone, String altPhone, String address) {
        this.name = name;
        this.phone = phone;
        this.altPhone = altPhone;
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getAltPhone() {
        return altPhone;
    }

    public String getAddress() {
        return address;
    }
}
