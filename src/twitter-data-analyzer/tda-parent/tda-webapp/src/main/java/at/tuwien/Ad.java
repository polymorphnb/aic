package at.tuwien;

public class Ad {

	private String user;
	private String brand;
	private String product;
	private String website;
	
	public Ad(String user, String brand, String product, String website){
		this.user = user;
		this.brand = brand;
		this.product = product;
		this.website = website;
	}
	
	public String getWebsite() {
		return website;
	}
	public void setWebsite(String website) {
		this.website = website;
	}
	public String getProduct() {
		return product;
	}
	public void setProduct(String product) {
		this.product = product;
	}
	public String getBrand() {
		return brand;
	}
	public void setBrand(String brand) {
		this.brand = brand;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}
}
