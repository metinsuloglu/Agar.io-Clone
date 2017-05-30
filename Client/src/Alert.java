import java.io.Serializable;

public class Alert implements Serializable {

	private String type;
	private Object data;
	
	public Alert(String type, Object data) {
		this.type = type;
		this.data = data;
	}
	
	public Object getData() {
		return data;
	}
	public void setData(Object data) {
		this.data = data;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
}
